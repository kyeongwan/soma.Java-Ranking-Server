/**
 * Created by lk on 2015. 9. 18..
 */
public class ThreadPool extends ThreadGroup{


    private WorkQueue pool = new WorkQueue();

    private int minThreadCount;
    private int maxThreadCount;
    private int createdThreadCount = 0;
    private int idleThreadCount = 0;
    private int allowedldleCount = 0;
    private int workThreadCount = 0;

    private boolean closed = false;

    private static int groupId = 0;
    private static int threadId = 0;

    public ThreadPool(int initThreadCount, int maxThreadCount, int minThreadCount, int allowedldleCount) {
        super(ThreadPool.class.getName()+Integer.toString(groupId++));

        this.minThreadCount = minThreadCount;
        this.maxThreadCount = maxThreadCount;
        this.createdThreadCount = initThreadCount;
        this.idleThreadCount = initThreadCount;
        this.allowedldleCount = allowedldleCount;

        for(int i=0; i<this.createdThreadCount; i++){
            new PooledThread().start();
        }
    }

    public synchronized void execute(Runnable work) throws AleadyClosedException{
        if(closed) throw new AleadyClosedException();

        increasePooledThread();
        pool.enqueue(work);
    }

    private void increasePooledThread(){
        synchronized (pool){
            if(idleThreadCount == 0 && createdThreadCount < maxThreadCount){
                new PooledThread().start();
                createdThreadCount++;
                idleThreadCount++;
            }
        }
    }

    private void beginRun(){
        synchronized (pool){
            workThreadCount++;
            idleThreadCount--;
        }
    }

    private boolean terminate(){
        synchronized (pool){
            workThreadCount--;
            idleThreadCount++;

            if(idleThreadCount > allowedldleCount && createdThreadCount > minThreadCount){
                createdThreadCount--;
                idleThreadCount--;
                return true;
            }
            return false;
        }
    }

    private class PooledThread extends Thread{

        public PooledThread() {
            super(ThreadPool.this, "PooledThread #"+threadId++);
        }

        public void run() {
            try {
                while( !closed ) {
                    Runnable work = pool.dequeue();

                    beginRun();
                    work.run();
                    if (terminate() ) {
                        break;
                    }
                }
            } catch(AleadyClosedException ex) {
            } catch(InterruptedException ex) {
            }
        }
    } // end of PooledThread

    public void printStatus() {
        synchronized(pool) {
            System.out.println("Total Thread="+createdThreadCount);
            System.out.println("Idle  Thread="+idleThreadCount);
            System.out.println("Work  Thread="+workThreadCount);
        }
    }
}
