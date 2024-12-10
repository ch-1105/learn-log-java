package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import java.lang.instrument.Instrumentation;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 类加载监控工具
 * 用于监控和分析类加载行为
 */
@Slf4j
public class ClassLoadingMonitor {
    
    // 记录类加载时间
    private static final ConcurrentHashMap<String, Long> loadingTimes = new ConcurrentHashMap<>();
    // 记录类加载次数
    private static final AtomicLong loadedClassCount = new AtomicLong(0);
    
    /**
     * JMX监控类加载统计
     */
    public static void monitorClassLoading() {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        
        log.info("=== 类加载统计信息 ===");
        log.info("当前加载类数量: {}", classLoadingMXBean.getLoadedClassCount());
        log.info("总共加载类数量: {}", classLoadingMXBean.getTotalLoadedClassCount());
        log.info("已卸载类数量: {}", classLoadingMXBean.getUnloadedClassCount());
        
        // 开启详细类加载信息
        if (classLoadingMXBean.isVerbose()) {
            log.info("类加载详细信息已开启");
        } else {
            classLoadingMXBean.setVerbose(true);
            log.info("已开启类加载详细信息");
        }
    }
    
    /**
     * 类加载计时器
     */
    public static class LoadingTimer {
        private final String className;
        private final long startTime;
        
        public LoadingTimer(String className) {
            this.className = className;
            this.startTime = System.nanoTime();
            loadedClassCount.incrementAndGet();
        }
        
        public void end() {
            long duration = System.nanoTime() - startTime;
            loadingTimes.put(className, duration);
            log.debug("类 {} 加载耗时: {} 纳秒", className, duration);
        }
    }
    
    /**
     * 打印类加载统计信息
     */
    public static void printStatistics() {
        log.info("=== 类加载详细统计 ===");
        log.info("总共加载类数量: {}", loadedClassCount.get());
        log.info("平均加载时间: {} 纳秒", calculateAverageLoadingTime());
        
        // 打印加载时间最长的前10个类
        log.info("加载时间最长的类:");
        loadingTimes.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(10)
            .forEach(e -> log.info("{}: {} 纳秒", e.getKey(), e.getValue()));
    }
    
    private static long calculateAverageLoadingTime() {
        if (loadingTimes.isEmpty()) {
            return 0;
        }
        long total = loadingTimes.values().stream()
            .mapToLong(Long::longValue)
            .sum();
        return total / loadingTimes.size();
    }
    
    /**
     * 类加载分析工具
     */
    public static class ClassLoadingAnalyzer {
        private final ClassLoader targetLoader;
        
        public ClassLoadingAnalyzer(ClassLoader loader) {
            this.targetLoader = loader;
        }
        
        /**
         * 分析类加载器层次结构
         */
        public void analyzeClassLoaderHierarchy() {
            log.info("=== 类加载器层次结构分析 ===");
            ClassLoader current = targetLoader;
            int level = 0;
            
            while (current != null) {
                log.info("Level {}: {}", level++, current.getClass().getName());
                current = current.getParent();
            }
            log.info("Level {}: Bootstrap ClassLoader", level);
        }
        
        /**
         * 分析已加载的类
         */
        public void analyzeLoadedClasses() {
            log.info("=== 已加载类分析 ===");
            
            // 获取系统属性中的java.class.path
            String classPath = System.getProperty("java.class.path");
            log.info("ClassPath: {}", classPath);
            
            // 分析类加载位置
            if (targetLoader instanceof java.net.URLClassLoader) {
                java.net.URLClassLoader urlLoader = (java.net.URLClassLoader) targetLoader;
                log.info("类加载路径:");
                for (java.net.URL url : urlLoader.getURLs()) {
                    log.info(" - {}", url);
                }
            }
        }
    }
    
    /**
     * 演示使用监控工具
     */
    public static void main(String[] args) {
        // 1. 开启JMX监控
        monitorClassLoading();
        
        // 2. 使用加载计时器
        LoadingTimer timer = new LoadingTimer("com.example.TestClass");
        try {
            // 模拟类加载
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        timer.end();
        
        // 3. 分析类加载器
        ClassLoadingAnalyzer analyzer = new ClassLoadingAnalyzer(
            ClassLoadingMonitor.class.getClassLoader());
        analyzer.analyzeClassLoaderHierarchy();
        analyzer.analyzeLoadedClasses();
        
        // 4. 打印统计信息
        printStatistics();
    }
}
