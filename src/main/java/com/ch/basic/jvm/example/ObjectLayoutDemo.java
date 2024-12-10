package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

/**
 * 对象内存布局示例
 * 使用JOL工具查看对象的内存布局
 */
@Slf4j
public class ObjectLayoutDemo {

   /**
    * 使用JOL工具查看对象的内存布局 需要添加依赖，使用jvm参数 java -XX:+UseCompressedOops ObjectLayoutDemo
    * <dependency>
    * <groupId>org.openjdk.jol</groupId>
    * <artifactId>jol-core</artifactId>
    * <version>0.16</version>
    * </dependency>
    */

    /**
     * 基础对象布局示例
     */
    static class SimpleObject {
        private boolean flag;    // 1 byte
        private byte b;          // 1 byte
        private char c;          // 2 bytes
        private short s;         // 2 bytes
        private int i;           // 4 bytes
        private long l;          // 8 bytes
        private float f;         // 4 bytes
        private double d;        // 8 bytes
        private String str;      // 4/8 bytes (reference)
    }

    /**
     * 演示字段重排序优化
     */
    static class OptimizedObject {
        private long l1;         // 8 bytes
        private long l2;         // 8 bytes
        private int i1;          // 4 bytes
        private int i2;          // 4 bytes
        private short s1;        // 2 bytes
        private short s2;        // 2 bytes
        private byte b1;         // 1 byte
        private byte b2;         // 1 byte
    }

    /**
     * 演示继承关系中的对象布局
     */
    static class Parent {
        private int parentValue;     // 4 bytes
        private long parentData;     // 8 bytes
    }

    static class Child extends Parent {
        private int childValue;      // 4 bytes
        private long childData;      // 8 bytes
    }

    /**
     * 演示数组对象布局
     */
    public static void printArrayLayout() {
        log.info("=== 数组对象布局 ===");
        // 基本类型数组
        int[] intArray = new int[3];
        log.info("\nint[]数组布局:");
        log.info(ClassLayout.parseInstance(intArray).toPrintable());

        // 对象数组
        String[] strArray = new String[3];
        log.info("\nString[]数组布局:");
        log.info(ClassLayout.parseInstance(strArray).toPrintable());
    }

    /**
     * 演示对象头的变化
     */
    public static void printObjectHeader() throws InterruptedException {
        log.info("=== 对象头状态变化 ===");
        Object obj = new Object();

        // 1. 查看新创建的对象
        log.info("\n新创建的对象:");
        log.info(ClassLayout.parseInstance(obj).toPrintable());

        // 2. 对象进入偏向锁状态
        Thread.sleep(4000); // 等待偏向锁延迟
        Object biasedObj = new Object();
        log.info("\n偏向锁状态的对象:");
        log.info(ClassLayout.parseInstance(biasedObj).toPrintable());

        // 3. 加锁后的对象
        synchronized (biasedObj) {
            log.info("\n加锁后的对象:");
            log.info(ClassLayout.parseInstance(biasedObj).toPrintable());
        }
    }

    /**
     * 演示不同类型的对象布局
     */
    public static void printObjectLayout() {
        log.info("=== 不同对象的内存布局 ===");
        
        // 1. 简单对象布局
        SimpleObject simpleObj = new SimpleObject();
        log.info("\nSimpleObject布局:");
        log.info(ClassLayout.parseInstance(simpleObj).toPrintable());

        // 2. 优化后的对象布局
        OptimizedObject optimizedObj = new OptimizedObject();
        log.info("\nOptimizedObject布局:");
        log.info(ClassLayout.parseInstance(optimizedObj).toPrintable());

        // 3. 继承关系中的对象布局
        Child child = new Child();
        log.info("\nChild对象布局(包含父类):");
        log.info(ClassLayout.parseInstance(child).toPrintable());
    }

    public static void main(String[] args) throws InterruptedException {
        // 打印JVM信息
        log.info("=== JVM信息 ===");
        log.info(VM.current().details());

        // 打印对象布局
        printObjectLayout();

        // 打印数组布局
        printArrayLayout();

        // 打印对象头的变化
        printObjectHeader();
    }
}
