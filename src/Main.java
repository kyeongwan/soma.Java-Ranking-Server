import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by lk on 2015. 9. 18..
 */
public class Main {

    private HashMap<String, User> userMap;  // Key = User ID, Value = User Object
    private List<User> rankedUserList;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private List<Client> connections = new LinkedList<>();
    private HashMap<String, Client> loginList = new HashMap<>();

    public static void main(String argp[]) {
        new Main();
    }

    public Main() {

        userMap = new HashMap<>();
        rankedUserList = new ArrayList<>();
        User user;
        for (int i = 0; i < 1000000; i++) {
            user = new User("u" + i, 10, 0);
            userMap.put(i + "", user);
            rankedUserList.add(user);
        }
        Collections.sort(rankedUserList);

        startServer();
    }

    void startServer() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);   // set to non-block server
            serverSocketChannel.bind(new InetSocketAddress(5001));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            if (serverSocketChannel.isOpen())
                stopServer();
            e.printStackTrace();
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int keyCount = selector.select();
                        if (keyCount == 0)
                            continue;
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = selectedKeys.iterator();
                        while (iterator.hasNext()) {
                            SelectionKey selectionKey = iterator.next();
                            if (selectionKey.isAcceptable())
                                accept(selectionKey);
                            else if (selectionKey.isReadable()) {
                                Client client = (Client) selectionKey.attachment();
                                client.receive(selectionKey);
                            } else if (selectionKey.isWritable()) {
                                Client client = (Client) selectionKey.attachment();
                                client.send(selectionKey);
                            }
                            iterator.remove();
                        }
                    } catch (IOException e) {
                        if (serverSocketChannel.isOpen())
                            stopServer();
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        System.out.println("LOG : Server Start!!");
    }

    void stopServer() {
        try {
            Iterator<Client> iterator = connections.iterator();
            while (iterator.hasNext()) {
                Client client = iterator.next();
                client.socketChannel.close();
                iterator.remove();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen())
                serverSocketChannel.close();
            if (selector != null && selector.isOpen())
                selector.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("LOG : Stop Server");
        }
    }

    void accept(SelectionKey selectionKey) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();

            Client client = new Client(socketChannel);

            connections.add(client);
        } catch (IOException e) {
            if (serverSocketChannel.isOpen())
                stopServer();
            e.printStackTrace();
        }
    }


    class CalcClass {

        public void updateUser(String id, int score) {
            User user = userMap.get(id);
            user.setScore(score);
            Collections.sort(rankedUserList);
            notifyAll();
        }

        public List<User> getMyRank(String id) {
            User user = userMap.get(id);
            int i = 0;
            while (true) {
                User target = rankedUserList.get(i);
                if (target.equals(user))
                    break;
                i++;
            }
            int start = i - 10;
            int end = i + 11;
            if (i < 10)
                start = 0;
            if (rankedUserList.size() - 10 < i)
                end = rankedUserList.size();
            return rankedUserList.subList(start, end);
        }

        public List<User> getTopRank() {
            return rankedUserList.subList(0, 10);
        }
    }


    class Client {
        SocketChannel socketChannel;
        String sendData;
        User user;
        CalcClass calc;

        Client(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
            calc = new CalcClass();
            try {
                socketChannel.configureBlocking(false);
                SelectionKey selectionKey = null;
                selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
                selectionKey.attach(this);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void receive(SelectionKey selectionKey) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                int byteCount = socketChannel.read(byteBuffer);

                if (byteCount == -1) {
                    return;
                }

                byteBuffer.flip();
                Charset charset = Charset.forName("UTF-8");
                String request = "";
                request = charset.decode(byteBuffer).toString();
                System.out.println("Get Data " + request);

                String datamap[] = request.split(":");
                if ("login".equals(datamap[0])) {
                    System.out.println(datamap[1]);
                    user = userMap.get(datamap[1]);
                    if (!user.isSignedIn()) {
                        user.setIsLogined(true);
                    } else {
                        loginList.get(datamap[1]).socketChannel.close();
                        connections.remove(loginList.get(datamap[1]));
                    }
                    loginList.put(datamap[1], this);
                    this.sendData = "Login Success\r\n";
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if ("myRank".equals(datamap[0])) {
                    List sublist;
                    synchronized (calc) {
                        sublist = calc.getMyRank(datamap[1]);
                    }
                    this.sendData = sublist.toString() + "\r\n";
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if ("scoreUpdate".equals(datamap[0])) {
                    synchronized (calc) {
                        calc.updateUser(datamap[1], Integer.parseInt(datamap[2]));
                    }
                    this.sendData = "Update Complete" + "\r\n";
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if ("getTopRank".equals(datamap[0])) {
                    synchronized (calc) {
                        this.sendData = calc.getTopRank().toString() + "\r\n";
                    }
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }
                selector.wakeup();
            } catch (Exception e) {
                try {
                    connections.remove(this);
                    //user.setIsLogined(false);
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }

        void send(SelectionKey selectionKey) {
            try {
                Charset charset = Charset.forName("UTF-8");
                ByteBuffer byteBuffer = charset.encode(sendData);
                socketChannel.write(byteBuffer);
                System.out.println("LOG : Send OK");
                selectionKey.interestOps(SelectionKey.OP_READ);
                selector.wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

