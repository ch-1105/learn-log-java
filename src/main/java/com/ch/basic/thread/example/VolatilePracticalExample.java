package com.ch.basic.thread.example;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

/**
 * volatile关键字在实际工作中的常见使用场景
 */
@Slf4j
public class VolatilePracticalExample {

    /**
     * 场景1：配置刷新
     * 在分布式系统中，配置中心更新配置后通知各服务刷新配置
     */
    static class ConfigManager {
        private volatile String config = "default";
        private volatile boolean configChanged = false;
        
        // 配置监听线程
        public void startConfigListener() {
            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    if (configChanged) {
                        log.info("检测到配置变更，当前配置: {}", config);
                        // 重新加载配置
                        reloadConfig();
                        configChanged = false;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "ConfigListener").start();
        }
        
        // 模拟配置更新
        public void updateConfig(String newConfig) {
            this.config = newConfig;
            this.configChanged = true;
            log.info("配置已更新为: {}", newConfig);
        }
        
        private void reloadConfig() {
            // 模拟重新加载配置的操作
            log.info("重新加载配置...");
        }
    }

    /**
     * 场景2：服务状态监控
     * 监控服务的健康状态，当发现服务不健康时进行处理
     */
    static class ServiceHealthMonitor {
        private volatile boolean healthy = true;
        private final List<String> serviceUrls = new ArrayList<>();
        
        public ServiceHealthMonitor() {
            // 模拟添加服务地址
            serviceUrls.add("http://service1");
            serviceUrls.add("http://service2");
        }
        
        // 健康检查线程
        public void startHealthCheck() {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                boolean currentHealth = checkHealth();
                if (currentHealth != healthy) {
                    healthy = currentHealth;
                    if (!healthy) {
                        log.warn("服务不健康，触发告警...");
                        triggerAlert();
                    } else {
                        log.info("服务恢复健康");
                    }
                }
            }, 0, 5, TimeUnit.SECONDS);
        }
        
        private boolean checkHealth() {
            // 模拟健康检查
            return Math.random() > 0.1; // 90%概率健康
        }
        
        private void triggerAlert() {
            // 模拟告警
            log.warn("发送告警邮件...");
        }
        
        public boolean isHealthy() {
            return healthy;
        }
    }

    /**
     * 场景3：优雅关闭
     * 服务优雅关闭，等待当前请求处理完成
     */
    static class GracefulShutdown {
        private volatile boolean shutdownRequested = false;
        private final AtomicInteger activeRequests = new AtomicInteger(0);
        
        // 处理新请求
        public void handleRequest(String request) {
            if (shutdownRequested) {
                log.info("服务正在关闭，拒绝新请求: {}", request);
                return;
            }
            
            activeRequests.incrementAndGet();
            try {
                // 模拟请求处理
                log.info("处理请求: {}", request);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                activeRequests.decrementAndGet();
            }
        }
        
        // 触发优雅关闭
        public void initiateShutdown() {
            log.info("开始优雅关闭...");
            shutdownRequested = true;
            
            // 等待所有请求处理完成
            while (activeRequests.get() > 0) {
                log.info("等待{}个请求处理完成...", activeRequests.get());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            log.info("服务已完全关闭");
        }
    }

    /**
     * 场景4：缓存刷新
     * 本地缓存过期刷新机制
     */
    static class LocalCache<K, V> {
        private volatile ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
        private volatile boolean needRefresh = false;
        
        // 缓存刷新线程
        public void startRefreshMonitor() {
            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    if (needRefresh) {
                        refreshCache();
                        needRefresh = false;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "CacheRefresher").start();
        }
        
        public V get(K key) {
            return cache.get(key);
        }
        
        public void put(K key, V value) {
            cache.put(key, value);
        }
        
        public void triggerRefresh() {
            needRefresh = true;
        }
        
        private void refreshCache() {
            // 模拟从数据源刷新缓存
            log.info("刷新缓存...");
            ConcurrentHashMap<K, V> newCache = new ConcurrentHashMap<>(cache);
            // 可以在这里添加刷新逻辑
            cache = newCache;
            log.info("缓存刷新完成");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 测试配置管理
        log.info("=== 测试配置管理 ===");
        ConfigManager configManager = new ConfigManager();
        configManager.startConfigListener();
        Thread.sleep(1000);
        configManager.updateConfig("new-config-1");
        Thread.sleep(1000);
        configManager.updateConfig("new-config-2");
        
        // 测试服务健康监控
        log.info("\n=== 测试服务健康监控 ===");
        ServiceHealthMonitor healthMonitor = new ServiceHealthMonitor();
        healthMonitor.startHealthCheck();
        
        // 测试优雅关闭
        log.info("\n=== 测试优雅关闭 ===");
        GracefulShutdown gracefulShutdown = new GracefulShutdown();
        // 模拟处理多个请求
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            final int requestId = i;
            executor.submit(() -> gracefulShutdown.handleRequest("Request-" + requestId));
        }
        Thread.sleep(500);
        gracefulShutdown.initiateShutdown();
        
        // 测试缓存刷新
        log.info("\n=== 测试缓存刷新 ===");
        LocalCache<String, String> cache = new LocalCache<>();
        cache.startRefreshMonitor();
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        Thread.sleep(1000);
        cache.triggerRefresh();
        
        // 等待所有示例完成
        Thread.sleep(3000);
        executor.shutdown();
    }
}
