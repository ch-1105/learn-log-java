package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 垃圾回收示例
 * 演示各种GC场景和内存泄漏情况
 */
@Slf4j
public class GCDemo {

    /**
     * 演示对象生命周期和GC
     */
    static class LifecycleDemo {
        private byte[] data = new byte[1024 * 1024]; // 1MB数据

        @Override
        protected void finalize() {
            log.info("对象被回收");
        }

        public static void testObjectLifecycle() {
            log.info("创建对象...");
            LifecycleDemo obj = new LifecycleDemo();
            log.info("将对象置为null");
            obj = null;
            log.info("手动触发GC");
            System.gc();
        }
    }

    /**
     * 演示新生代GC
     * -Xmx20m -Xms20m -Xmn10m -XX:+PrintGCDetails
     */
    static class YoungGCDemo {
        private static final int _1MB = 1024 * 1024;

        public static void testYoungGC() {
            log.info("开始分配内存...");
            byte[] array1 = new byte[2 * _1MB];
            byte[] array2 = new byte[2 * _1MB];
            byte[] array3 = new byte[2 * _1MB];
            byte[] array4 = new byte[2 * _1MB]; // 这里可能触发Young GC
            
            log.info("完成分配");
        }
    }

    /**
     * 演示老年代GC
     * -Xmx20m -Xms20m -Xmn10m -XX:+PrintGCDetails -XX:PretenureSizeThreshold=3M
     */
    static class OldGCDemo {
        private static final int _1MB = 1024 * 1024;
        
        public static void testOldGC() {
            log.info("开始分配大对象...");
            byte[] array = new byte[4 * _1MB];  // 大对象直接进入老年代
            log.info("完成分配");
        }
    }

    /**
     * 演示内存泄漏
     * -Xmx128m -Xms128m -XX:+PrintGCDetails
     */
    static class MemoryLeakDemo {
        static List<byte[]> list = new ArrayList<>();

        public static void testMemoryLeak() {
            try {
                while (true) {
                    list.add(new byte[1024 * 1024]); // 1MB
                    TimeUnit.MILLISECONDS.sleep(100);
                    log.info("当前列表大小: {} MB", list.size());
                }
            } catch (OutOfMemoryError e) {
                log.error("发生内存溢出: {}", e.getMessage());
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 演示弱引用与软引用
     */
    static class ReferenceDemo {
        private static final int _1MB = 1024 * 1024;

        /**
         * 演示软引用
         * 在内存不足时才会被回收
         */
        public static void testSoftReference() {
            // 创建一个软引用
            java.lang.ref.SoftReference<byte[]> softRef = 
                new java.lang.ref.SoftReference<>(new byte[10 * _1MB]);
            
            log.info("软引用对象是否存在: {}", softRef.get() != null);
            System.gc();
            log.info("GC后软引用对象是否存在: {}", softRef.get() != null);
            
            // 尝试分配大量内存，使内存紧张
            try {
                byte[] bytes = new byte[20 * _1MB];
            } catch (OutOfMemoryError e) {
                log.info("内存不足时软引用对象是否存在: {}", softRef.get() != null);
            }
        }

        /**
         * 演示弱引用
         * 在GC时一定会被回收
         */
        public static void testWeakReference() {
            // 创建一个弱引用
            java.lang.ref.WeakReference<byte[]> weakRef = 
                new java.lang.ref.WeakReference<>(new byte[_1MB]);
            
            log.info("弱引用对象是否存在: {}", weakRef.get() != null);
            System.gc();
            log.info("GC后弱引用对象是否存在: {}", weakRef.get() != null);
        }
    }

    public static void main(String[] args) {
        // 1. 测试对象生命周期
        log.info("=== 测试对象生命周期 ===");
        LifecycleDemo.testObjectLifecycle();

        // 2. 测试新生代GC
        log.info("\n=== 测试新生代GC ===");
        YoungGCDemo.testYoungGC();

        // 3. 测试老年代GC
        log.info("\n=== 测试老年代GC ===");
        OldGCDemo.testOldGC();

        // 4. 测试引用类型
        log.info("\n=== 测试软引用 ===");
        ReferenceDemo.testSoftReference();
        log.info("\n=== 测试弱引用 ===");
        ReferenceDemo.testWeakReference();

        // 5. 测试内存泄漏
        log.info("\n=== 测试内存泄漏 ===");
        try {
            MemoryLeakDemo.testMemoryLeak();
        } catch (OutOfMemoryError e) {
            log.error("内存泄漏测试完成");
        }
    }
}
