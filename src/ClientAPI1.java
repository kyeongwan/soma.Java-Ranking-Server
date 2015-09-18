import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Created by lk on 2015. 9. 18..
 */
public class ClientAPI1 {

    SocketChannel socketChannel;
    int id;

    public static void main(String arg[]){
        new ClientAPI1(1);
        Scanner scanner = new Scanner(System.in);
        //scanner.nextInt();
        //new ClientAPI1(1);
        //new ClientAPI1(2);
        //new ClientAPI1(3);
        //new ClientAPI1(4);
    }

    public ClientAPI1(int id){

        startClient(id);
        //startClient(4);
        //startClient(5);
        //startClient(6);
    }

    void startClient(final int id){
        Thread thread = new Thread(){
            @Override
            public void run(){
                try{
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress("localhost", 5001));

                } catch (IOException e) {
                    e.printStackTrace();
                    if(socketChannel.isOpen())
                        stopClient();
                    return;
                }
                login(id);
            }
        };
        thread.start();
    }
    void stopClient(){
        try{
            if(socketChannel != null && socketChannel.isOpen())
                socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void receive(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true)
                {
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(100);

                        int readByteCount = socketChannel.read(byteBuffer);

                        if (readByteCount == -1)
                            continue;//throw new IOException();

                        byteBuffer.flip();
                        Charset charset = Charset.forName("UTF-8");
                        String data = charset.decode(byteBuffer).toString();
                        System.out.println(data);
                        if(data.contains("\r\n")) {
                            send();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        stopClient();
                        break;
                    }
                }
            }
        };
        thread.start();

    }

    void login(final int id) {
        this.id = id;
        try{
            Charset charset = Charset.forName("UTF-8");
            ByteBuffer byteBuffer = charset.encode("login:" + id);
            socketChannel.write(byteBuffer);
            System.out.println("LOG : LogIn OK");
            receive();
        } catch (IOException e) {
            e.printStackTrace();
            stopClient();
        }

    }

    void send(){
        while(true)
            if(socketChannel != null && socketChannel.isOpen())
                break;
        try{
            Charset charset = Charset.forName("UTF-8");
            ByteBuffer byteBuffer = charset.encode("myRank:" + id);
            socketChannel.write(byteBuffer);
            System.out.println("LOG : Send OK");
            receive();
        } catch (IOException e) {
            e.printStackTrace();
            stopClient();
        }
    }
}
