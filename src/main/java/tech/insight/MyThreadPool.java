package tech.insight;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Desc * @param
 * @return
 * @date:
 * @Author
 */
public class MyThreadPool {
    BlockingQueue<Runnable> commandList;

    private int corePoolSize;

    private int maxSize;

    private int timeout;

    private TimeUnit timeUnit;

    public MyThreadPool(int corePoolSize, int maxSize, int timeout, TimeUnit timeUnit, BlockingQueue<Runnable> commandList) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.commandList = commandList;
    }

    List<Thread> coreList = new ArrayList<>();

    List<Thread> supportList = new ArrayList<>();

    void execute(Runnable command) {
        if(coreList.size() < corePoolSize) {
            Thread thread = new coreThread();
            coreList.add(thread);
            thread.start();
        }
        if (commandList.offer(command)) {
            return ;
        }
        if (coreList.size() + supportList.size() < maxSize) {
            Thread thread = new supportThread();
            supportList.add(thread);
            thread.start();
        }
        if (!commandList.offer(command)) {
            throw new RuntimeException("阻塞队列满了");
        }
    }

    class coreThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (!commandList.isEmpty()) {
                    try {
                        Runnable command = commandList.take();
                        command.run();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
    class supportThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (!commandList.isEmpty()) {
                    try {
                        Runnable command = commandList.poll(timeout, timeUnit);
                        if (command == null) {
                            break;
                        }
                        command.run();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(Thread.currentThread().getName() + "辅助线程结束了");
                }
            }
        }
    }
}
