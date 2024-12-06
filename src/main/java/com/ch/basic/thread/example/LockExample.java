package com.ch.basic.thread.example;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Queue;
import java.util.LinkedList;

/**
 * 锁机制示例
 * 展示synchronized和ReentrantLock的使用场景和区别
 */
@Slf4j
public class LockExample {

    /**
     * 场景1：简单同步 - 使用synchronized
     * 适用于简单的同步场景，不需要高级特性
     */
    static class BankAccount {
        private double balance;
        
        public synchronized void deposit(double amount) {
            balance += amount;
            log.info("存款: {}，余额: {}", amount, balance);
        }
        
        public synchronized void withdraw(double amount) {
            if (balance >= amount) {
                balance -= amount;
                log.info("取款: {}，余额: {}", amount, balance);
            } else {
                log.info("余额不足，取款失败");
            }
        }
        
        public synchronized double getBalance() {
            return balance;
        }
    }

    /**
     * 场景2：条件变量 - 使用ReentrantLock
     * 适用于需要条件等待的场景
     */
    static class BoundedBuffer<T> {
        private final Queue<T> queue;
        private final int capacity;
        private final ReentrantLock lock;
        private final Condition notFull;
        private final Condition notEmpty;
        
        public BoundedBuffer(int capacity) {
            this.capacity = capacity;
            this.queue = new LinkedList<>();
            this.lock = new ReentrantLock();
            this.notFull = lock.newCondition();
            this.notEmpty = lock.newCondition();
        }
        
        public void put(T element) throws InterruptedException {
            lock.lock();
            try {
                while (queue.size() == capacity) {
                    log.info("缓冲区已满，等待空间...");
                    notFull.await();
                }
                queue.offer(element);
                log.info("添加元素: {}, 当前大小: {}", element, queue.size());
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }
        
        public T take() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    log.info("缓冲区为空，等待数据...");
                    notEmpty.await();
                }
                T element = queue.poll();
                log.info("取出元素: {}, 当前大小: {}", element, queue.size());
                notFull.signal();
                return element;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 场景3：超时锁 - 使用ReentrantLock的tryLock
     * 适用于需要超时机制的场景
     */
    static class ResourceManager {
        private final ReentrantLock lock = new ReentrantLock();
        private String resource = "初始资源";
        
        public boolean updateResource(String newValue, long timeout, TimeUnit unit) {
            try {
                if (lock.tryLock(timeout, unit)) {
                    try {
                        log.info("成功获取锁，更新资源");
                        Thread.sleep(500); // 模拟操作耗时
                        this.resource = newValue;
                        return true;
                    } finally {
                        lock.unlock();
                    }
                } else {
                    log.info("获取锁超时，更新失败");
                    return false;
                }
            } catch (InterruptedException e) {
                log.error("更新被中断", e);
                return false;
            }
        }
        
        public String getResource() {
            return resource;
        }
    }

    /**
     * 场景4：可中断锁 - 使用ReentrantLock的lockInterruptibly
     * 适用于需要响应中断的场景
     */
    static class InterruptibleTask {
        private final ReentrantLock lock = new ReentrantLock();
        
        public void execute() {
            try {
                log.info("尝试获取锁...");
                lock.lockInterruptibly();
                try {
                    log.info("获取到锁，执行任务");
                    Thread.sleep(5000); // 模拟长时间操作
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                log.info("任务被中断");
            }
        }
        
        public void executeWithLock() {
            lock.lock();
            try {
                log.info("持有锁，不响应中断");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.info("虽然被中断，但会继续执行直到完成");
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 测试场景1：简单同步
        log.info("=== 测试简单同步 ===");
        BankAccount account = new BankAccount();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> account.deposit(100));
            executor.submit(() -> account.withdraw(50));
        }
        
        // 测试场景2：条件变量
        log.info("\n=== 测试条件变量 ===");
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(3);
        
        executor.submit(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    buffer.put(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        executor.submit(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    buffer.take();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 测试场景3：超时锁
        log.info("\n=== 测试超时锁 ===");
        ResourceManager manager = new ResourceManager();
        
        executor.submit(() -> {
            boolean success = manager.updateResource("新资源1", 1, TimeUnit.SECONDS);
            log.info("更新结果: {}", success);
        });
        
        executor.submit(() -> {
            boolean success = manager.updateResource("新资源2", 1, TimeUnit.SECONDS);
            log.info("更新结果: {}", success);
        });
        
        // 测试场景4：可中断锁
        log.info("\n=== 测试可中断锁 ===");
        InterruptibleTask task = new InterruptibleTask();
        
        Thread t1 = new Thread(() -> task.executeWithLock());
        Thread t2 = new Thread(() -> task.execute());
        
        t1.start();
        Thread.sleep(1000);
        t2.start();
        Thread.sleep(1000);
        
        t1.interrupt();
        t2.interrupt();
        
        // 等待所有任务完成
        Thread.sleep(6000);
        executor.shutdown();
    }
}
