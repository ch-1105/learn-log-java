package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 类加载实际案例分析
 * 展示常见的类加载问题和解决方案
 */
@Slf4j
public class ClassLoadingCases {

    /**
     * 案例1：类加载死锁
     * 问题：两个类相互依赖，导致类加载死锁
     */
    public static class DeadlockCase {
        static class ClassA {
            static ClassB b = new ClassB();
            static {
                log.info("ClassA 初始化");
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        static class ClassB {
            static ClassA a = new ClassA();
            static {
                log.info("ClassB 初始化");
            }
        }

        public static void demonstrate() {
            log.info("=== 类加载死锁案例 ===");
            Thread t1 = new Thread(() -> {
                log.info("线程1开始加载ClassA");
                ClassA a = new ClassA();
            }, "Thread-1");

            Thread t2 = new Thread(() -> {
                log.info("线程2开始加载ClassB");
                ClassB b = new ClassB();
            }, "Thread-2");

            t1.start();
            t2.start();
        }
    }

    /**
     * 案例2：内存泄漏
     * 问题：类加载器未被正确释放导致内存泄漏
     */
    public static class MemoryLeakCase {
        private static List<ClassLoader> loaders = new ArrayList<>();

        public static void demonstrate() {
            log.info("=== 类加载器内存泄漏案例 ===");
            
            try {
                while (true) {
                    // 创建新的类加载器
                    URLClassLoader loader = new URLClassLoader(
                        new URL[]{new File("./lib").toURI().toURL()},
                        ClassLoadingCases.class.getClassLoader()
                    );
                    
                    // 加载类
                    Class<?> clazz = loader.loadClass("com.example.SampleClass");
                    
                    // 错误：保持对类加载器的引用
                    loaders.add(loader);
                    
                    log.info("已加载类加载器数量: {}", loaders.size());
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            } catch (Exception e) {
                log.error("内存泄漏案例执行失败", e);
            }
        }

        // 解决方案
        public static void solution() {
            log.info("=== 内存泄漏解决方案 ===");
            
            try {
                URLClassLoader loader = new URLClassLoader(
                    new URL[]{new File("./lib").toURI().toURL()},
                    ClassLoadingCases.class.getClassLoader()
                );
                
                try {
                    Class<?> clazz = loader.loadClass("com.example.SampleClass");
                    // 使用完类后
                } finally {
                    // 确保关闭类加载器
                    loader.close();
                }
            } catch (Exception e) {
                log.error("解决方案执行失败", e);
            }
        }
    }

    /**
     * 案例3：版本冲突
     * 问题：同一个类的不同版本导致冲突
     */
    public static class VersionConflictCase {
        public static void demonstrate() {
            log.info("=== 类版本冲突案例 ===");
            
            try {
                // 创建两个不同的类加载器，加载不同版本的类
                URLClassLoader loader1 = new URLClassLoader(
                    new URL[]{new File("./lib/v1").toURI().toURL()}
                );
                
                URLClassLoader loader2 = new URLClassLoader(
                    new URL[]{new File("./lib/v2").toURI().toURL()}
                );
                
                // 加载两个版本的类
                Class<?> class1 = loader1.loadClass("com.example.VersionedClass");
                Class<?> class2 = loader2.loadClass("com.example.VersionedClass");
                
                // 验证类是否相同
                log.info("类是否相同: {}", class1.equals(class2));
                log.info("类加载器1: {}", class1.getClassLoader());
                log.info("类加载器2: {}", class2.getClassLoader());
                
            } catch (Exception e) {
                log.error("版本冲突案例执行失败", e);
            }
        }

        // 解决方案：使用自定义类加载器隔离不同版本
        public static class VersionIsolationClassLoader extends URLClassLoader {
            private final String version;

            public VersionIsolationClassLoader(String version, URL[] urls) {
                super(urls);
                this.version = version;
            }

            @Override
            protected Class<?> loadClass(String name, boolean resolve) 
                throws ClassNotFoundException {
                // 对特定包使用自定义加载逻辑
                if (name.startsWith("com.example")) {
                    Class<?> loadedClass = findLoadedClass(name);
                    if (loadedClass == null) {
                        return findClass(name);
                    }
                    return loadedClass;
                }
                // 其他类使用父加载器
                return super.loadClass(name, resolve);
            }
        }
    }

    /**
     * 案例4：动态类加载性能问题
     * 问题：频繁的类加载导致性能下降
     */
    public static class PerformanceCase {
        public static void demonstrate() {
            log.info("=== 类加载性能问题案例 ===");
            
            // 使用监控工具
            ClassLoadingMonitor.monitorClassLoading();
            
            try {
                for (int i = 0; i < 1000; i++) {
                    // 记录加载时间
                    ClassLoadingMonitor.LoadingTimer timer = 
                        new ClassLoadingMonitor.LoadingTimer("DynamicClass" + i);
                    
                    // 动态生成和加载类
                    generateAndLoadClass("DynamicClass" + i);
                    
                    timer.end();
                }
                
                // 打印统计信息
                ClassLoadingMonitor.printStatistics();
                
            } catch (Exception e) {
                log.error("性能问题案例执行失败", e);
            }
        }

        private static void generateAndLoadClass(String className) {
            // 模拟类生成和加载
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 解决方案：类缓存
        public static class ClassCache {
            private static final ConcurrentHashMap<String, Class<?>> cache = 
                new ConcurrentHashMap<>();

            public static Class<?> loadClass(String name) 
                throws ClassNotFoundException {
                return cache.computeIfAbsent(name, k -> {
                    try {
                        return generateAndLoadClass(k);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    public static void main(String[] args) {
        // 1. 演示类加载死锁
        DeadlockCase.demonstrate();

        // 2. 演示内存泄漏
        MemoryLeakCase.demonstrate();
        MemoryLeakCase.solution();

        // 3. 演示版本冲突
        VersionConflictCase.demonstrate();

        // 4. 演示性能问题
        PerformanceCase.demonstrate();
    }
}
