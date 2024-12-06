package com.ch.basic.thread.example;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;

/**
 * 自定义AQS示例
 * 通过简单的例子来理解AQS的工作原理
 */
@Slf4j
public class CustomAQSExample {

    /**
     * 场景1：简单的互斥锁
     * 就像一个单人会议室，同一时间只能有一个人使用
     */
    static class SimpleLock implements Lock {
        // 自定义同步器
        private static class Sync extends AbstractQueuedSynchronizer {
            // 尝试获取锁
            protected boolean tryAcquire(int acquires) {
                // 如果状态为0，说明没人使用，尝试获取
                if (compareAndSetState(0, 1)) {
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            }
            
            // 尝试释放锁
            protected boolean tryRelease(int releases) {
                if (getState() == 0) {
                    throw new IllegalMonitorStateException();
                }
                setExclusiveOwnerThread(null);
                setState(0);
                return true;
            }
            
            // 是否持有锁
            protected boolean isHeldExclusively() {
                return getState() == 1;
            }
        }
        
        private final Sync sync = new Sync();
        
        @Override
        public void lock() {
            sync.acquire(1);
        }
        
        @Override
        public boolean tryLock() {
            return sync.tryAcquire(1);
        }
        
        @Override
        public void unlock() {
            sync.release(1);
        }
        
        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }
        
        @Override
        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }
    }

    /**
     * 场景2：简单的信号量
     * 就像一个有限容量的会议室，可以同时容纳N个人
     */
    static class SimplePermit {
        private static class Sync extends AbstractQueuedSynchronizer {
            Sync(int permits) {
                setState(permits);
            }
            
            // 尝试获取许可
            protected int tryAcquireShared(int acquires) {
                for (;;) {
                    int available = getState();
                    int remaining = available - acquires;
                    if (remaining < 0 || compareAndSetState(available, remaining)) {
                        return remaining;
                    }
                }
            }
            
            // 释放许可
            protected boolean tryReleaseShared(int releases) {
                for (;;) {
                    int current = getState();
                    int next = current + releases;
                    if (next < current) { // 溢出检查
                        throw new Error("Maximum permit count exceeded");
                    }
                    if (compareAndSetState(current, next)) {
                        return true;
                    }
                }
            }
        }
        
        private final Sync sync;
        
        public SimplePermit(int permits) {
            sync = new Sync(permits);
        }
        
        public void acquire() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }
        
        public void release() {
            sync.releaseShared(1);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 测试场景1：互斥锁
        log.info("=== 测试互斥锁 ===");
        SimpleLock lock = new SimpleLock();
        
        // 创建多个线程竞争锁
        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    log.info("线程 {} 尝试获取锁", threadId);
                    lock.lock();
                    log.info("线程 {} 获取到锁", threadId);
                    Thread.sleep(1000); // 模拟工作
                    log.info("线程 {} 释放锁", threadId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }).start();
        }
        
        // 测试场景2：信号量
        log.info("\n=== 测试信号量 ===");
        SimplePermit permit = new SimplePermit(2); // 允许2个并发
        
        // 创建5个线程竞争2个许可
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    log.info("线程 {} 尝试获取许可", threadId);
                    permit.acquire();
                    log.info("线程 {} 获取到许可", threadId);
                    Thread.sleep(1000); // 模拟工作
                    log.info("线程 {} 释放许可", threadId);
                    permit.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        
        // 等待所有线程完成
        Thread.sleep(5000);
    }
}
