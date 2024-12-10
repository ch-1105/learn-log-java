package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

/**
 * JVM各组件示例
 * 展示JVM中各个组成部分的作用
 */
@Slf4j
public class JVMComponentsDemo {

    // 类变量（方法区）
    private static int staticVar = 1;
    
    // 常量（方法区）
    private static final String CONSTANT = "Hello JVM";
    
    // 实例变量（堆）
    private List<String> instanceVar = new ArrayList<>();

    /**
     * 演示Java虚拟机栈的使用
     * 通过递归调用展示栈的深度
     */
    public static void stackDemo(int depth) {
        // 局部变量（栈帧中的局部变量表）
        long localVar = depth;
        
        log.info("当前栈深度: {}", depth);
        
        // 如果栈太深，会抛出StackOverflowError
        if (depth > 0) {
            stackDemo(depth - 1);
        }
    }

    /**
     * 演示堆内存的使用
     * 通过不断创建对象占用堆空间
     */
    public void heapDemo() {
        List<byte[]> list = new ArrayList<>();
        int index = 1;
        
        try {
            while (true) {
                // 每次分配1MB空间
                byte[] bytes = new byte[1024 * 1024];
                list.add(bytes);
                log.info("已分配 {} MB空间", index++);
                
                // 稍作延时，方便观察
                Thread.sleep(100);
            }
        } catch (OutOfMemoryError e) {
            log.error("堆内存溢出: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 演示方法区的使用
     * 加载类信息到方法区
     */
    public void methodAreaDemo() {
        // 访问静态变量（方法区）
        log.info("静态变量值: {}", staticVar);
        
        // 访问常量（方法区）
        log.info("常量值: {}", CONSTANT);
        
        // 创建新的类加载器
        ClassLoader customLoader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                return super.findClass(name);
            }
        };
        
        log.info("自定义类加载器: {}", customLoader);
    }

    /**
     * 演示本地方法栈的使用
     */
    public native void nativeMethodDemo();

    /**
     * 演示程序计数器的作用
     * 通过多线程展示程序计数器的工作
     */
    public void pcRegisterDemo() {
        // 创建多个线程执行不同的任务
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                log.info("线程1执行第{}次", i + 1);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                log.info("线程2执行第{}次", i + 1);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        t1.start();
        t2.start();
    }

    public static void main(String[] args) {
        JVMComponentsDemo demo = new JVMComponentsDemo();

        // 1. 演示Java虚拟机栈
        log.info("=== 测试Java虚拟机栈 ===");
        try {
            stackDemo(10);  // 尝试10层栈深度
        } catch (StackOverflowError e) {
            log.error("栈溢出: {}", e.getMessage());
        }

        // 2. 演示程序计数器
        log.info("\n=== 测试程序计数器 ===");
        demo.pcRegisterDemo();

        // 3. 演示方法区
        log.info("\n=== 测试方法区 ===");
        demo.methodAreaDemo();

        // 4. 演示堆
        log.info("\n=== 测试堆内存 ===");
        demo.heapDemo();
    }
}
