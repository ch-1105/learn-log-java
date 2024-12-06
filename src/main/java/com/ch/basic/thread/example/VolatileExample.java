package com.ch.basic.thread.example;

import lombok.extern.slf4j.Slf4j;

/**
 * volatile关键字示例
 * 演示volatile的可见性、有序性，以及不能保证原子性
 */
@Slf4j
public class VolatileExample {

    // 演示可见性
    private static volatile boolean flag = false;
    
    // 演示非原子性
    private static volatile int counter = 0;
    
    /**
     * 演示volatile的可见性
     */
    public static void testVisibility() throws InterruptedException {
        Thread writerThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                log.info("Writer thread setting flag to true");
                flag = true;  // 修改volatile变量
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread readerThread = new Thread(() -> {
            while (!flag) {  // 读取volatile变量
                // 等待flag变为true
            }
            log.info("Reader thread detected flag change");
        });

        log.info("Starting visibility test...");
        readerThread.start();
        writerThread.start();
        
        writerThread.join();
        readerThread.join();
    }

    /**
     * 演示volatile不保证原子性
     */
    public static void testNonAtomicity() throws InterruptedException {
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter++;  // 非原子操作
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        log.info("Counter final value: " + counter);
        log.info("Expected value: " + (5 * 1000));
    }

    /**
     * 演示正确的volatile使用场景：状态标志
     */
    static class TaskManager {
        private volatile boolean isRunning = true;
        
        public void shutdown() {
            log.info("Shutting down task manager");
            isRunning = false;
        }
        
        public void doWork() {
            while (isRunning) {
                // 模拟工作
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            log.info("Task manager stopped");
        }
    }

    /**
     * 演示volatile在双重检查锁定中的应用
     */
    static class Singleton {
        private static volatile Singleton instance;
        
        private Singleton() {}
        
        public static Singleton getInstance() {
            if (instance == null) {
                synchronized (Singleton.class) {
                    if (instance == null) {
                        instance = new Singleton();
                    }
                }
            }
            return instance;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 1. 测试可见性
        log.info("=== Testing Volatile Visibility ===");
        testVisibility();

        // 2. 测试非原子性
        log.info("\n=== Testing Volatile Non-Atomicity ===");
        testNonAtomicity();

        // 3. 测试状态标志
        log.info("\n=== Testing Status Flag Usage ===");
        TaskManager taskManager = new TaskManager();
        Thread workThread = new Thread(taskManager::doWork);
        workThread.start();
        
        Thread.sleep(500);  // 让任务运行一会儿
        taskManager.shutdown();
        workThread.join();

        // 4. 测试单例模式
        log.info("\n=== Testing Singleton Pattern ===");
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                Singleton singleton = Singleton.getInstance();
                log.info("Thread {} got singleton instance: {}", 
                    Thread.currentThread().getName(), singleton);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
}
