package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * 类加载机制示例
 * 展示类加载的各个阶段和特性
 */
@Slf4j
public class ClassLoadingDemo {

    /**
     * 演示类的初始化时机
     */
    static class InitializationDemo {
        // 静态变量，类初始化时赋值
        public static int value = 123;
        
        // 编译期常量，不会触发类初始化
        public static final String CONSTANT = "CONSTANT";
        
        // 静态代码块
        static {
            log.info("InitializationDemo类被初始化");
        }
        
        public static void method() {
            log.info("静态方法被调用");
        }
    }

    /**
     * 自定义类加载器示例
     */
    static class CustomClassLoader extends ClassLoader {
        private final String classPath;

        public CustomClassLoader(String classPath) {
            this.classPath = classPath;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                String fileName = name.replace('.', File.separatorChar) + ".class";
                Path classFile = Path.of(classPath, fileName);
                
                if (Files.exists(classFile)) {
                    byte[] classBytes = Files.readAllBytes(classFile);
                    return defineClass(name, classBytes, 0, classBytes.length);
                }
            } catch (IOException e) {
                log.error("加载类文件失败", e);
            }
            throw new ClassNotFoundException(name);
        }
    }

    /**
     * 演示类加载器层次结构
     */
    public static void demonstrateClassLoaderHierarchy() {
        log.info("=== 类加载器层次结构 ===");
        
        // 获取当前类的类加载器
        ClassLoader currentLoader = ClassLoadingDemo.class.getClassLoader();
        log.info("当前类的加载器: {}", currentLoader);
        
        // 获取父加载器
        ClassLoader parentLoader = currentLoader.getParent();
        log.info("父加载器: {}", parentLoader);
        
        // 获取顶层加载器
        ClassLoader bootstrapLoader = parentLoader.getParent();
        log.info("启动类加载器: {}", bootstrapLoader);  // 通常为null，因为是原生实现
    }

    /**
     * 演示类加载的过程
     */
    public static void demonstrateClassLoading() {
        log.info("=== 类加载过程演示 ===");
        
        try {
            // 1. 通过类名加载
            log.info("通过类名加载:");
            Class.forName("com.ch.basic.jvm.example.ClassLoadingDemo$InitializationDemo");
            
            // 2. 通过类名加载但不初始化
            log.info("\n通过类名加载但不初始化:");
            Class.forName("com.ch.basic.jvm.example.ClassLoadingDemo$InitializationDemo", 
                false, ClassLoadingDemo.class.getClassLoader());
            
            // 3. 访问静态变量
            log.info("\n访问静态变量:");
            int value = InitializationDemo.value;
            
            // 4. 访问常量（不会触发初始化）
            log.info("\n访问常量:");
            String constant = InitializationDemo.CONSTANT;
            
            // 5. 调用静态方法
            log.info("\n调用静态方法:");
            InitializationDemo.method();
            
        } catch (ClassNotFoundException e) {
            log.error("类加载失败", e);
        }
    }

    /**
     * 演示自定义类加载器
     */
    public static void demonstrateCustomClassLoader() {
        log.info("=== 自定义类加载器演示 ===");
        
        try {
            // 创建自定义类加载器
            CustomClassLoader loader = new CustomClassLoader("./classes");
            
            // 加载类
            Class<?> clazz = loader.loadClass("com.example.MyClass");
            log.info("成功加载类: {}", clazz.getName());
            
            // 创建实例
            Object instance = clazz.getDeclaredConstructor().newInstance();
            log.info("成功创建实例: {}", instance);
            
            // 调用方法
            Method method = clazz.getMethod("sayHello");
            method.invoke(instance);
            
        } catch (Exception e) {
            log.error("自定义类加载器演示失败", e);
        }
    }

    /**
     * 演示类卸载
     */
    public static void demonstrateClassUnloading() {
        log.info("=== 类卸载演示 ===");
        
        try {
            // 创建自定义类加载器
            CustomClassLoader loader = new CustomClassLoader("./classes");
            Class<?> clazz = loader.loadClass("com.example.MyClass");
            
            // 清除所有引用
            loader = null;
            clazz = null;
            System.gc();
            
            // 等待一段时间让GC生效
            TimeUnit.SECONDS.sleep(2);
            
            log.info("类可能已被卸载");
            
        } catch (Exception e) {
            log.error("类卸载演示失败", e);
        }
    }

    public static void main(String[] args) {
        // 1. 演示类加载器层次结构
        demonstrateClassLoaderHierarchy();

        // 2. 演示类加载过程
        demonstrateClassLoading();

        // 3. 演示自定义类加载器
        demonstrateCustomClassLoader();

        // 4. 演示类卸载
        demonstrateClassUnloading();
    }
}
