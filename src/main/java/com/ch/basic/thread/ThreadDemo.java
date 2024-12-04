package com.ch.basic.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * 线程基础示例
 */
public class ThreadDemo {

    /**
     * 继承Thread类创建线程
     */
    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("Thread " + getName() + " is running");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.out.println("Thread " + getName() + " interrupted");
                Thread.currentThread().interrupt();
            }
            System.out.println("Thread " + getName() + " finished");
        }
    }

    /**
     * 实现Runnable接口创建线程
     */
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("Runnable thread " + Thread.currentThread().getName() + " is running");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.out.println("Runnable thread " + Thread.currentThread().getName() + " interrupted");
                Thread.currentThread().interrupt();
            }
            System.out.println("Runnable thread " + Thread.currentThread().getName() + " finished");
        }
    }

    /**
     * 实现Callable接口创建线程
     */
    static class MyCallable implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("Callable thread " + Thread.currentThread().getName() + " is running");
            TimeUnit.SECONDS.sleep(1);
            return "Callable result";
        }
    }

    /**
     * 演示线程状态
     */
    private static void demonstrateThreadStates() throws InterruptedException {
        final Object lock = new Object();

        Thread thread = new Thread(() -> {
            try {
                // TIMED_WAITING状态
                TimeUnit.SECONDS.sleep(1);

                synchronized (lock) {
                    // WAITING状态
                    lock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // NEW状态
        System.out.println("Thread state after creation: " + thread.getState());

        thread.start();
        // RUNNABLE状态
        System.out.println("Thread state after start: " + thread.getState());

        TimeUnit.MILLISECONDS.sleep(500);
        // TIMED_WAITING状态
        System.out.println("Thread state during sleep: " + thread.getState());

        TimeUnit.SECONDS.sleep(1);
        synchronized (lock) {
            // WAITING状态
            System.out.println("Thread state during wait: " + thread.getState());
            lock.notify();
        }

        thread.join();
        // TERMINATED状态
        System.out.println("Thread state after completion: " + thread.getState());
    }

    /**
     * 演示线程优先级
     */
    private static void demonstratePriority() {
        Thread highPriority = new Thread(() -> {
            int count = 0;
            while (count < 1000000 && !Thread.currentThread().isInterrupted()) {
                count++;
            }
            System.out.println("High priority thread count: " + count);
        });

        Thread lowPriority = new Thread(() -> {
            int count = 0;
            while (count < 1000000 && !Thread.currentThread().isInterrupted()) {
                count++;
            }
            System.out.println("Low priority thread count: " + count);
        });

        highPriority.setPriority(Thread.MAX_PRIORITY);
        lowPriority.setPriority(Thread.MIN_PRIORITY);

        lowPriority.start();
        highPriority.start();
    }

    /**
     * 演示线程中断
     */
    private static void demonstrateInterruption() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread is running...");
                    TimeUnit.MILLISECONDS.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted while sleeping");
                Thread.currentThread().interrupt();
            }
            System.out.println("Thread stopped");
        });

        thread.start();
        TimeUnit.SECONDS.sleep(2);
        thread.interrupt();
        thread.join();
    }

    /**
     * 演示守护线程
     */
    private static void demonstrateDaemonThread() throws InterruptedException {
        Thread daemon = new Thread(() -> {
            try {
                while (true) {
                    System.out.println("Daemon thread is running...");
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        daemon.setDaemon(true);
        daemon.start();

        // 主线程睡眠3秒后退出，守护线程也会随之退出
        TimeUnit.SECONDS.sleep(3);
        System.out.println("Main thread finished, daemon thread will be terminated");
    }

    /**
     * 演示线程组
     */
    private static void demonstrateThreadGroup() {
        ThreadGroup group = new ThreadGroup("MyThreadGroup");

        Thread thread1 = new Thread(group, () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Thread-1");

        Thread thread2 = new Thread(group, () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();

        System.out.println("Active threads in group: " + group.activeCount());
        System.out.println("Thread group name: " + group.getName());

        // 中断线程组中的所有线程
        group.interrupt();
    }

    /**
     * 演示异常处理
     */
    private static void demonstrateExceptionHandling() {
        Thread thread = new Thread(() -> {
            throw new RuntimeException("Deliberate exception");
        });

        thread.setUncaughtExceptionHandler((t, e) -> {
            System.out.println("Thread " + t.getName() + " threw exception: " + e.getMessage());
        });

        thread.start();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Thread Creation Examples ===");
        // 使用Thread类
        MyThread thread1 = new MyThread();
        thread1.start();
        thread1.join();

        // 使用Runnable接口
        Thread thread2 = new Thread(new MyRunnable());
        thread2.start();
        thread2.join();

        // 使用Callable接口
        FutureTask<String> futureTask = new FutureTask<>(new MyCallable());
        Thread thread3 = new Thread(futureTask);
        thread3.start();
        System.out.println("Callable result: " + futureTask.get());

        System.out.println("\n=== Thread States Example ===");
        demonstrateThreadStates();

        System.out.println("\n=== Thread Priority Example ===");
        demonstratePriority();
        TimeUnit.SECONDS.sleep(1);

        System.out.println("\n=== Thread Interruption Example ===");
        demonstrateInterruption();

        System.out.println("\n=== Daemon Thread Example ===");
        demonstrateDaemonThread();

        System.out.println("\n=== Thread Group Example ===");
        demonstrateThreadGroup();

        System.out.println("\n=== Exception Handling Example ===");
        demonstrateExceptionHandling();
        TimeUnit.SECONDS.sleep(1);
    }
}
