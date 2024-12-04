package com.ch.basic.thread;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

/**
 * 锁机制综合示例
 */
public class LockDemo {

    /**
     * synchronized基本用法示例
     */
    static class SynchronizedExample {
        private int count = 0;
        private final Object lock = new Object();

        // 同步方法
        public synchronized void increment() {
            count++;
        }

        // 同步代码块
        public void decrement() {
            synchronized(lock) {
                count--;
            }
        }

        // 类锁
        public static synchronized void staticMethod() {
            System.out.println("Static synchronized method");
        }
    }

    /**
     * ReentrantLock基本用法示例
     */
    static class ReentrantLockExample {
        private final ReentrantLock lock = new ReentrantLock(true); // 公平锁
        private int count = 0;

        public void increment() {
            // 可中断加锁
            try {
                if (lock.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        count++;
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void decrement() {
            // 普通加锁
            lock.lock();
            try {
                count--;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * ReadWriteLock示例
     */
    static class ReadWriteLockExample {
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock writeLock = rwLock.writeLock();
        private final Map<String, String> map = new HashMap<>();

        public String read(String key) {
            readLock.lock();
            try {
                return map.get(key);
            } finally {
                readLock.unlock();
            }
        }

        public void write(String key, String value) {
            writeLock.lock();
            try {
                map.put(key, value);
            } finally {
                writeLock.unlock();
            }
        }
    }

    /**
     * StampedLock示例
     */
    static class StampedLockExample {
        private final StampedLock sl = new StampedLock();
        private double x, y;

        public void move(double deltaX, double deltaY) {
            long stamp = sl.writeLock();
            try {
                x += deltaX;
                y += deltaY;
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        public double distanceFromOrigin() {
            long stamp = sl.tryOptimisticRead();
            double currentX = x, currentY = y;
            if (!sl.validate(stamp)) {
                stamp = sl.readLock();
                try {
                    currentX = x;
                    currentY = y;
                } finally {
                    sl.unlockRead(stamp);
                }
            }
            return Math.sqrt(currentX * currentX + currentY * currentY);
        }
    }

    /**
     * 条件变量示例
     */
    static class ConditionExample {
        private final Lock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();
        private final String[] items = new String[100];
        private int putptr, takeptr, count;

        public void put(String x) throws InterruptedException {
            lock.lock();
            try {
                while (count == items.length)
                    notFull.await();
                items[putptr] = x;
                if (++putptr == items.length) putptr = 0;
                ++count;
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        public String take() throws InterruptedException {
            lock.lock();
            try {
                while (count == 0)
                    notEmpty.await();
                String x = items[takeptr];
                if (++takeptr == items.length) takeptr = 0;
                --count;
                notFull.signal();
                return x;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 死锁示例
     */
    static class DeadlockExample {
        private final Object lockA = new Object();
        private final Object lockB = new Object();

        public void methodA() {
            synchronized(lockA) {
                System.out.println("Method A holding lock A...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized(lockB) {
                    System.out.println("Method A holding lock A & B");
                }
            }
        }

        public void methodB() {
            synchronized(lockB) {
                System.out.println("Method B holding lock B...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized(lockA) {
                    System.out.println("Method B holding lock B & A");
                }
            }
        }
    }

    /**
     * 性能测试
     */
    private static void performanceTest() throws InterruptedException {
        int threadCount = 1000;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 测试synchronized
        SynchronizedExample syncExample = new SynchronizedExample();
        long startTime = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    syncExample.increment();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long syncTime = System.nanoTime() - startTime;

        // 测试ReentrantLock
        executor = Executors.newFixedThreadPool(threadCount);
        ReentrantLockExample lockExample = new ReentrantLockExample();
        startTime = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    lockExample.increment();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long lockTime = System.nanoTime() - startTime;

        System.out.println("Synchronized time: " + syncTime/1000000 + "ms");
        System.out.println("ReentrantLock time: " + lockTime/1000000 + "ms");
    }

    /**
     * ReadWriteLock性能测试
     */
    private static void readWriteLockTest() throws InterruptedException {
        ReadWriteLockExample rwLock = new ReadWriteLockExample();
        int readThreads = 100;
        int writeThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(readThreads + writeThreads);

        // 写线程
        for (int i = 0; i < writeThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    rwLock.write("key" + index, "value" + j);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        // 读线程
        for (int i = 0; i < readThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    rwLock.read("key" + (j % writeThreads));
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    /**
     * StampedLock性能测试
     */
    private static void stampedLockTest() throws InterruptedException {
        StampedLockExample slLock = new StampedLockExample();
        int threads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    slLock.move(1.0, 1.0);
                    slLock.distanceFromOrigin();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws InterruptedException {
        // 性能测试
        System.out.println("=== Performance Test ===");
        performanceTest();

        // ReadWriteLock测试
        System.out.println("\n=== ReadWriteLock Test ===");
        readWriteLockTest();

        // StampedLock测试
        System.out.println("\n=== StampedLock Test ===");
        stampedLockTest();

        // 死锁示例
        System.out.println("\n=== Deadlock Example ===");
        DeadlockExample deadlock = new DeadlockExample();
        Thread t1 = new Thread(deadlock::methodA);
        Thread t2 = new Thread(deadlock::methodB);
        t1.start();
        t2.start();

        // 条件变量示例
        System.out.println("\n=== Condition Example ===");
        ConditionExample condition = new ConditionExample();
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    condition.put("Item " + i);
                    System.out.println("Produced: Item " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    String item = condition.take();
                    System.out.println("Consumed: " + item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
