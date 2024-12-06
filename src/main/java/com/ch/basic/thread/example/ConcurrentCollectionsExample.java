package com.ch.basic.thread.example;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * 线程安全集合示例
 * 展示ConcurrentHashMap、CopyOnWriteArrayList等在实际场景中的应用
 */
@Slf4j
public class ConcurrentCollectionsExample {

    /**
     * 场景1：缓存系统 - 使用ConcurrentHashMap
     * 模拟一个简单的缓存系统，支持并发的读写操作
     */
    static class SimpleCache<K, V> {
        private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<K, Long> expiryMap = new ConcurrentHashMap<>();
        private final long defaultExpiryMs;
        
        public SimpleCache(long defaultExpiryMs) {
            this.defaultExpiryMs = defaultExpiryMs;
            // 启动清理过期缓存的线程
            scheduleCacheCleanup();
        }
        
        public V get(K key) {
            if (isExpired(key)) {
                cache.remove(key);
                expiryMap.remove(key);
                return null;
            }
            return cache.get(key);
        }
        
        public void put(K key, V value) {
            cache.put(key, value);
            expiryMap.put(key, System.currentTimeMillis() + defaultExpiryMs);
        }
        
        public V putIfAbsent(K key, V value) {
            V existing = cache.putIfAbsent(key, value);
            if (existing == null) {
                expiryMap.put(key, System.currentTimeMillis() + defaultExpiryMs);
            }
            return existing;
        }
        
        private boolean isExpired(K key) {
            Long expiryTime = expiryMap.get(key);
            return expiryTime != null && System.currentTimeMillis() > expiryTime;
        }
        
        private void scheduleCacheCleanup() {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                Set<K> keys = new ConcurrentSkipListSet<>(cache.keySet());
                for (K key : keys) {
                    if (isExpired(key)) {
                        cache.remove(key);
                        expiryMap.remove(key);
                        log.info("已清理过期缓存: {}", key);
                    }
                }
            }, defaultExpiryMs/2, defaultExpiryMs/2, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 场景2：事件监听系统 - 使用CopyOnWriteArrayList
     * 模拟一个事件监听系统，支持动态添加和移除监听器
     */
    static class EventSystem {
        private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
        
        public void addEventListener(EventListener listener) {
            listeners.add(listener);
            log.info("添加监听器: {}", listener);
        }
        
        public void removeEventListener(EventListener listener) {
            listeners.remove(listener);
            log.info("移除监听器: {}", listener);
        }
        
        public void fireEvent(String event) {
            for (EventListener listener : listeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    log.error("处理事件异常", e);
                }
            }
        }
        
        interface EventListener {
            void onEvent(String event);
        }
    }

    /**
     * 场景3：生产者-消费者队列 - 使用BlockingQueue
     * 模拟一个工作队列系统
     */
    static class WorkQueue {
        private final BlockingQueue<String> queue;
        private final AtomicInteger completedTasks = new AtomicInteger();
        private volatile boolean isRunning = true;
        
        public WorkQueue(int capacity) {
            // 使用ArrayBlockingQueue作为有界队列
            this.queue = new ArrayBlockingQueue<>(capacity);
            // 启动消费者线程
            startConsumers(3);
        }
        
        public void submitTask(String task) throws InterruptedException {
            queue.put(task);
            log.info("提交任务: {}, 当前队列大小: {}", task, queue.size());
        }
        
        public boolean trySubmitTask(String task, long timeout, TimeUnit unit) throws InterruptedException {
            boolean success = queue.offer(task, timeout, unit);
            if (success) {
                log.info("成功提交任务: {}", task);
            } else {
                log.info("提交任务超时: {}", task);
            }
            return success;
        }
        
        private void startConsumers(int consumerCount) {
            for (int i = 0; i < consumerCount; i++) {
                final int consumerId = i;
                new Thread(() -> {
                    while (isRunning) {
                        try {
                            String task = queue.poll(1, TimeUnit.SECONDS);
                            if (task != null) {
                                processTask(consumerId, task);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }, "Consumer-" + i).start();
            }
        }
        
        private void processTask(int consumerId, String task) {
            try {
                // 模拟任务处理
                Thread.sleep(500);
                log.info("消费者-{} 完成任务: {}", consumerId, task);
                completedTasks.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public void shutdown() {
            isRunning = false;
        }
        
        public int getCompletedTaskCount() {
            return completedTasks.get();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 测试场景1：缓存系统
        log.info("=== 测试缓存系统 ===");
        SimpleCache<String, String> cache = new SimpleCache<>(5000); // 5秒过期
        
        // 并发添加缓存
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            final String key = "key" + i;
            final String value = "value" + i;
            executor.submit(() -> cache.put(key, value));
        }
        
        Thread.sleep(1000);
        
        // 并发读取缓存
        for (int i = 0; i < 5; i++) {
            final String key = "key" + i;
            executor.submit(() -> log.info("读取缓存 {}: {}", key, cache.get(key)));
        }
        
        // 测试场景2：事件监听系统
        log.info("\n=== 测试事件监听系统 ===");
        EventSystem eventSystem = new EventSystem();
        
        // 添加监听器
        EventSystem.EventListener listener1 = event -> log.info("监听器1收到事件: {}", event);
        EventSystem.EventListener listener2 = event -> log.info("监听器2收到事件: {}", event);
        
        eventSystem.addEventListener(listener1);
        eventSystem.addEventListener(listener2);
        
        // 触发事件
        executor.submit(() -> eventSystem.fireEvent("事件1"));
        executor.submit(() -> eventSystem.fireEvent("事件2"));
        
        // 移除监听器
        Thread.sleep(1000);
        eventSystem.removeEventListener(listener1);
        eventSystem.fireEvent("事件3");
        
        // 测试场景3：工作队列
        log.info("\n=== 测试工作队列 ===");
        WorkQueue workQueue = new WorkQueue(5);
        
        // 提交任务
        for (int i = 0; i < 10; i++) {
            final String task = "Task-" + i;
            executor.submit(() -> {
                try {
                    workQueue.trySubmitTask(task, 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // 等待任务处理
        Thread.sleep(5000);
        log.info("完成任务数: {}", workQueue.getCompletedTaskCount());
        
        // 清理资源
        workQueue.shutdown();
        executor.shutdown();
    }
}
