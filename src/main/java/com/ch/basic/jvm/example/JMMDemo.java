package com.ch.basic.jvm.example;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Java内存模型示例
 * 展示JMM的特性：原子性、可见性、有序性
 */
@Slf4j
public class JMMDemo {

    /**
     * 可见性问题演示
     */
    public static class VisibilityDemo {
        // 不使用volatile，可能会造成可见性问题
        private boolean running = true;
        // 使用volatile，保证可见性
        private volatile boolean volatileRunning = true;

        /**
         * 演示没有volatile的情况
         */
        public void testWithoutVolatile() throws InterruptedException {
            Thread thread = new Thread(() -> {
                log.info("子线程开始运行");
                int i = 0;
                while (running) {
                    i++;
                    // 注意：由于JIT优化，这里需要打印日志才能看到效果
                    if (i % 1000000 == 0) {
                        log.info("running...");
                    }
                }
                log.info("子线程发现running为false，结束运行");
            });
            
            thread.start();
            TimeUnit.SECONDS.sleep(1);
            running = false;
            log.info("主线程已设置running为false");
            thread.join();
        }

        /**
         * 演示使用volatile的情况
         */
        public void testWithVolatile() throws InterruptedException {
            Thread thread = new Thread(() -> {
                log.info("子线程开始运行");
                int i = 0;
                while (volatileRunning) {
                    i++;
                    if (i % 1000000 == 0) {
                        log.info("running...");
                    }
                }
                log.info("子线程发现volatileRunning为false，结束运行");
            });
            
            thread.start();
            TimeUnit.SECONDS.sleep(1);
            volatileRunning = false;
            log.info("主线程已设置volatileRunning为false");
            thread.join();
        }
    }

    /**
     * 原子性问题演示
     */
    public static class AtomicityDemo {
        private int count = 0;
        private volatile int volatileCount = 0;
        private final Object lock = new Object();

        /**
         * 演示非原子性操作
         */
        public void testNonAtomic() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    for (int j = 0; j < 1000; j++) {
                        count++;  // 非原子操作
                        volatileCount++;  // volatile不保证原子性
                    }
                    latch.countDown();
                }).start();
            }
            
            latch.await();
            log.info("非同步count最终值：" + count);
            log.info("volatile count最终值：" + volatileCount);
        }

        /**
         * 演示使用synchronized保证原子性
         */
        public void testAtomic() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    for (int j = 0; j < 1000; j++) {
                        synchronized (lock) {
                            count++;  // 使用synchronized保证原子性
                        }
                    }
                    latch.countDown();
                }).start();
            }
            
            latch.await();
            log.info("同步count最终值：" + count);
        }
    }

    /**
     * 有序性问题演示
     */
    public static class OrderingDemo {
        private int a = 0;
        private boolean flag = false;
        private volatile boolean volatileFlag = false;

        /**
         * 演示重排序问题
         */
        public void testReordering() throws InterruptedException {
            for (int i = 0; i < 10000; i++) {
                a = 0;
                flag = false;
                volatileFlag = false;
                
                Thread t1 = new Thread(() -> {
                    a = 1;
                    flag = true;
                });

                Thread t2 = new Thread(() -> {
                    if (flag) {
                        if (a == 0) {
                            // 如果发生了重排序，这里可能会输出
                            log.info("发生重排序！flag为true但a为0");
                        }
                    }
                });

                t1.start();
                t2.start();
                t1.join();
                t2.join();
            }
        }

        /**
         * 演示volatile防止重排序
         */
        public void testVolatileOrdering() throws InterruptedException {
            for (int i = 0; i < 10000; i++) {
                a = 0;
                volatileFlag = false;
                
                Thread t1 = new Thread(() -> {
                    a = 1;
                    volatileFlag = true;  // volatile写
                });

                Thread t2 = new Thread(() -> {
                    if (volatileFlag) {   // volatile读
                        if (a == 0) {
                            // 由于volatile的happens-before规则，这里不会输出
                            log.info("不会发生重排序");
                        }
                    }
                });

                t1.start();
                t2.start();
                t1.join();
                t2.join();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 1. 测试可见性
        log.info("=== 测试可见性 ===");
        VisibilityDemo visibilityDemo = new VisibilityDemo();
        log.info("不使用volatile的情况：");
        visibilityDemo.testWithoutVolatile();
        log.info("\n使用volatile的情况：");
        visibilityDemo.testWithVolatile();

        // 2. 测试原子性
        log.info("\n=== 测试原子性 ===");
        AtomicityDemo atomicityDemo = new AtomicityDemo();
        atomicityDemo.testNonAtomic();
        atomicityDemo.testAtomic();

        // 3. 测试有序性
        log.info("\n=== 测试有序性 ===");
        OrderingDemo orderingDemo = new OrderingDemo();
        log.info("测试重排序：");
        orderingDemo.testReordering();
        log.info("\n测试volatile防止重排序：");
        orderingDemo.testVolatileOrdering();
    }
}
