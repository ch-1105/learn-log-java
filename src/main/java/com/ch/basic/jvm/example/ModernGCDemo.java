package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 现代JVM（JDK 17+）垃圾回收示例
 * 
 * 运行参数示例：
 * 1. 使用G1（默认）:
 *    java -XX:+UseG1GC -Xmx512m -Xms512m -XX:MaxGCPauseMillis=200
 * 
 * 2. 使用ZGC:
 *    java -XX:+UseZGC -Xmx512m -Xms512m
 * 
 * 3. 开启JFR监控:
 *    java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=recording.jfr
 */
@Slf4j
public class ModernGCDemo {

    /**
     * 演示内存分配和GC行为
     */
    static class MemoryAllocationDemo {
        private static final int ALLOCATION_SIZE = 1024 * 1024; // 1MB

        public static void testAllocation() {
            log.info("开始内存分配测试...");
            List<byte[]> allocations = new ArrayList<>();

            try {
                // 模拟内存分配
                for (int i = 0; i < 100; i++) {
                    allocations.add(new byte[ALLOCATION_SIZE]);
                    if (i % 10 == 0) {
                        log.info("已分配 {} MB内存", i + 1);
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // 清理引用，使对象可被回收
                allocations.clear();
                System.gc(); // 建议进行GC
            }
        }
    }

    /**
     * 监控GC活动
     */
    static class GCMonitor {
        public static void printGCStats() {
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            
            log.info("GC统计信息:");
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                String gcName = gcBean.getName();
                long gcCount = gcBean.getCollectionCount();
                long gcTime = gcBean.getCollectionTime();
                
                log.info("{}: 执行次数={}, 总时间={}ms", 
                    gcName, gcCount, gcTime);
            }
        }
    }

    /**
     * 演示大对象分配
     * 在G1中会直接进入humongous区域
     */
    static class LargeObjectAllocationDemo {
        public static void testLargeAllocation() {
            log.info("开始大对象分配测试...");
            
            // 分配一个32MB的大对象
            byte[] largeObject = new byte[32 * 1024 * 1024];
            log.info("大对象分配完成");
            
            // 清理引用
            largeObject = null;
            System.gc();
        }
    }

    /**
     * 演示内存压力下的GC行为
     */
    static class MemoryPressureDemo {
        private static final int PRESSURE_DURATION_SECONDS = 10;

        public static void createMemoryPressure() {
            log.info("开始施加内存压力，持续{}秒...", PRESSURE_DURATION_SECONDS);
            List<byte[]> objects = new ArrayList<>();
            
            try {
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < PRESSURE_DURATION_SECONDS * 1000) {
                    // 快速分配和释放内存
                    objects.add(new byte[1024 * 1024]); // 1MB
                    if (objects.size() > 10) {
                        objects.subList(0, 5).clear();
                    }
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                objects.clear();
            }
            
            log.info("内存压力测试完成");
        }
    }

    public static void main(String[] args) {
        // 打印JVM版本和GC信息
        log.info("JVM版本: {}", System.getProperty("java.version"));
        log.info("运行时信息: {}", ManagementFactory.getRuntimeMXBean().getInputArguments());

        // 1. 测试普通内存分配
        log.info("\n=== 测试内存分配 ===");
        MemoryAllocationDemo.testAllocation();
        GCMonitor.printGCStats();

        // 2. 测试大对象分配
        log.info("\n=== 测试大对象分配 ===");
        LargeObjectAllocationDemo.testLargeAllocation();
        GCMonitor.printGCStats();

        // 3. 测试内存压力
        log.info("\n=== 测试内存压力 ===");
        MemoryPressureDemo.createMemoryPressure();
        GCMonitor.printGCStats();
    }
}
