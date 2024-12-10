package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 运行时数据区示例
 * 展示JVM各个内存区域的特性
 */
@Slf4j
public class RuntimeDataAreaDemo {

    /**
     * 演示Java虚拟机栈的特性
     * 1. 局部变量表
     * 2. 操作数栈
     * 3. 栈溢出
     */
    public static class StackDemo {
        private int stackLength = 1;

        public void testStackOverflow() {
            stackLength++;
            try {
                // 局部变量，存储在局部变量表中
                long a = 1;
                long b = 2;
                // 操作数栈演示
                long c = a + b;
                
                log.info("当前栈深度：{}, 计算结果：{}", stackLength, c);
                
                // 递归调用，不断加深栈深度
                testStackOverflow();
            } catch (StackOverflowError e) {
                log.error("栈溢出在深度：{}", stackLength);
                throw e;
            }
        }
    }

    /**
     * 演示Java堆的特性
     * 1. 对象实例的分配
     * 2. 堆内存溢出
     */
    public static class HeapDemo {
        static class OOMObject {
            // 1MB数组
            private byte[] memory = new byte[1024 * 1024];
        }

        public void testHeapOOM() {
            List<OOMObject> list = new ArrayList<>();
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                    list.add(new OOMObject());
                } catch (OutOfMemoryError e) {
                    log.error("堆内存溢出，当前对象数量：{}", list.size());
                    throw e;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 演示方法区（元空间）的特性
     * 1. 类信息
     * 2. 运行时常量池
     * 3. 字符串常量池
     */
    public static class MethodAreaDemo {
        public void testMethodArea() {
            // 字符串常量池测试
            String str1 = "hello";
            String str2 = "hello";
            String str3 = new String("hello");
            
            log.info("str1 == str2: {}", str1 == str2);  // true
            log.info("str1 == str3: {}", str1 == str3);  // false
            log.info("str1 == str3.intern(): {}", str1 == str3.intern());  // true
            
            // 运行时常量池测试
            Integer i1 = Integer.valueOf(127);
            Integer i2 = Integer.valueOf(127);
            Integer i3 = Integer.valueOf(128);
            Integer i4 = Integer.valueOf(128);
            
            log.info("i1 == i2: {}", i1 == i2);  // true（-128到127缓存）
            log.info("i3 == i4: {}", i3 == i4);  // false
        }
    }

    /**
     * 演示直接内存的特性
     * 1. DirectByteBuffer的使用
     * 2. 直接内存溢出
     */
    public static class DirectMemoryDemo {
        public void testDirectMemory() {
            List<ByteBuffer> buffers = new ArrayList<>();
            int capacity = 1024 * 1024 * 1024;  // 1GB
            
            try {
                while (true) {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
                    buffers.add(buffer);
                    log.info("已分配直接内存：{}GB", buffers.size());
                }
            } catch (OutOfMemoryError e) {
                log.error("直接内存溢出，已分配：{}GB", buffers.size());
                throw e;
            }
        }
    }

    public static void main(String[] args) {
        // 1. 测试栈
        log.info("=== 测试虚拟机栈 ===");
        try {
            new StackDemo().testStackOverflow();
        } catch (StackOverflowError e) {
            log.error("栈溢出测试完成");
        }

        // 2. 测试方法区
        log.info("\n=== 测试方法区 ===");
        new MethodAreaDemo().testMethodArea();

        // 3. 测试堆
        log.info("\n=== 测试堆内存 ===");
        try {
            new HeapDemo().testHeapOOM();
        } catch (OutOfMemoryError e) {
            log.error("堆内存溢出测试完成");
        }

        // 4. 测试直接内存
        log.info("\n=== 测试直接内存 ===");
        try {
            new DirectMemoryDemo().testDirectMemory();
        } catch (OutOfMemoryError e) {
            log.error("直接内存溢出测试完成");
        }
    }
}
