package com.ch.basic.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

/**
 * 线程同步机制示例
 */
public class SynchronizationDemo {

    /**
     * synchronized关键字示例
     */
    static class SynchronizedCounter {
        private int count = 0;

        // 同步方法
        public synchronized void increment() {
            count++;
        }

        // 同步代码块
        public void decrement() {
            synchronized(this) {
                count--;
            }
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * volatile关键字示例
     */
    static class VolatileFlag {
        private volatile boolean flag = false;

        public void setFlag() {
            flag = true;
        }

        public boolean isFlag() {
            return flag;
        }
    }

    /**
     * 原子类示例
     */
    static class AtomicCounter {
        private AtomicInteger count = new AtomicInteger(0);

        public void increment() {
            count.incrementAndGet();
        }

        public void decrement() {
            count.decrementAndGet();
        }

        public int getCount() {
            return count.get();
        }
    }

    /**
     * ReentrantLock示例
     */
    static class ReentrantLockCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private int count = 0;

        public void increment() {
            lock.lock();
            try {
                count++;
            } finally {
                lock.unlock();
            }
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * ReadWriteLock示例
     */
    static class ReadWriteCounter {
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock writeLock = rwLock.writeLock();
        private int count = 0;

        public void increment() {
            writeLock.lock();
            try {
                count++;
            } finally {
                writeLock.unlock();
            }
        }

        public int getCount() {
            readLock.lock();
            try {
                return count;
            } finally {
                readLock.unlock();
            }
        }
    }

    /**
     * StampedLock示例
     */
    static class StampedLockCounter {
        private final StampedLock sl = new StampedLock();
        private int count = 0;

        public void increment() {
            long stamp = sl.writeLock();
            try {
                count++;
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        public int getCount() {
            long stamp = sl.tryOptimisticRead();
            int currentCount = count;
            if (!sl.validate(stamp)) {
                stamp = sl.readLock();
                try {
                    currentCount = count;
                } finally {
                    sl.unlockRead(stamp);
                }
            }
            return currentCount;
        }
    }

    /**
     * 演示死锁
     */
    static class DeadlockExample {
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();

        public void method1() {
            synchronized(lock1) {
                System.out.println("Method 1 holding lock 1...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized(lock2) {
                    System.out.println("Method 1 holding lock 1 & 2...");
                }
            }
        }

        public void method2() {
            synchronized(lock2) {
                System.out.println("Method 2 holding lock 2...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized(lock1) {
                    System.out.println("Method 2 holding lock 2 & 1...");
                }
            }
        }
    }

    /**
     * 演示同步工具类
     */
    private static void demonstrateSyncTools() throws InterruptedException {
        // Semaphore示例
        final Semaphore semaphore = new Semaphore(2);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                try {
                    semaphore.acquire();
                    System.out.println("Thread " + Thread.currentThread().getName() + " acquired semaphore");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release();
                    System.out.println("Thread " + Thread.currentThread().getName() + " released semaphore");
                }
            });
        }

        // CountDownLatch示例
        final CountDownLatch latch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println("Task " + taskId + " completed");
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.await();
        System.out.println("All tasks completed");

        // CyclicBarrier示例
        final CyclicBarrier barrier = new CyclicBarrier(3, () ->
            System.out.println("All threads reached barrier"));

        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Thread " + threadId + " waiting at barrier");
                    barrier.await();
                    System.out.println("Thread " + threadId + " crossed barrier");
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        // 测试synchronized
        SynchronizedCounter syncCounter = new SynchronizedCounter();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1000; i++) {
            executor.submit(syncCounter::increment);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("Synchronized Counter: " + syncCounter.getCount());

        // 测试volatile
        VolatileFlag flag = new VolatileFlag();
        new Thread(() -> {
            try {
                Thread.sleep(100);
                flag.setFlag();
                System.out.println("Flag set to true");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        while (!flag.isFlag()) {
            // 等待flag变为true
        }
        System.out.println("Flag detected as true");

        // 测试AtomicInteger
        AtomicCounter atomicCounter = new AtomicCounter();
        executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1000; i++) {
            executor.submit(atomicCounter::increment);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("Atomic Counter: " + atomicCounter.getCount());

        // 测试ReentrantLock
        ReentrantLockCounter lockCounter = new ReentrantLockCounter();
        executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1000; i++) {
            executor.submit(lockCounter::increment);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("ReentrantLock Counter: " + lockCounter.getCount());

        // 测试ReadWriteLock
        ReadWriteCounter rwCounter = new ReadWriteCounter();
        executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1000; i++) {
            executor.submit(rwCounter::increment);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("ReadWriteLock Counter: " + rwCounter.getCount());

        // 测试StampedLock
        StampedLockCounter slCounter = new StampedLockCounter();
        executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1000; i++) {
            executor.submit(slCounter::increment);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("StampedLock Counter: " + slCounter.getCount());

        // 测试死锁
        DeadlockExample deadlock = new DeadlockExample();
        Thread t1 = new Thread(deadlock::method1);
        Thread t2 = new Thread(deadlock::method2);
        t1.start();
        t2.start();

        // 测试同步工具类
        System.out.println("\nTesting Synchronization Tools:");
        demonstrateSyncTools();
    }
}
