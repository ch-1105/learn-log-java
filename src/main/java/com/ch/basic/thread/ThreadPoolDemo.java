package com.ch.basic.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

/**
 * 线程池示例
 */
public class ThreadPoolDemo {

    /**
     * 可监控的线程池
     */
    static class MonitorableThreadPool extends ThreadPoolExecutor {
        private final AtomicLong totalTime = new AtomicLong();
        private final AtomicLong totalTasks = new AtomicLong();
        private final ThreadLocal<Long> startTime = new ThreadLocal<>();

        public MonitorableThreadPool(int corePoolSize,
                                   int maximumPoolSize,
                                   long keepAliveTime,
                                   TimeUnit unit,
                                   BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            startTime.set(System.nanoTime());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            try {
                long endTime = System.nanoTime();
                long taskTime = endTime - startTime.get();
                totalTime.addAndGet(taskTime);
                totalTasks.incrementAndGet();
            } finally {
                startTime.remove();
            }
            super.afterExecute(r, t);
        }

        public double getAverageTaskTime() {
            long tasks = totalTasks.get();
            return tasks == 0 ? 0 : (double) totalTime.get() / tasks / 1_000_000; // 转换为毫秒
        }
    }

    /**
     * 演示不同类型的线程池
     */
    private static void demonstrateThreadPools() throws InterruptedException {
        // 1. FixedThreadPool
        ExecutorService fixedPool = Executors.newFixedThreadPool(3);
        System.out.println("=== FixedThreadPool Demo ===");
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            fixedPool.submit(() -> {
                System.out.println("FixedThreadPool Task " + taskId +
                    " executed by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 2. CachedThreadPool
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        System.out.println("\n=== CachedThreadPool Demo ===");
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            cachedPool.submit(() -> {
                System.out.println("CachedThreadPool Task " + taskId +
                    " executed by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 3. ScheduledThreadPool
        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(2);
        System.out.println("\n=== ScheduledThreadPool Demo ===");
        scheduledPool.scheduleAtFixedRate(() ->
            System.out.println("Scheduled task executed by " +
                Thread.currentThread().getName()),
            0, 2, TimeUnit.SECONDS);

        // 等待一段时间观察执行情况
        Thread.sleep(5000);

        // 关闭线程池
        fixedPool.shutdown();
        cachedPool.shutdown();
        scheduledPool.shutdown();
    }

    /**
     * 演示自定义线程池
     */
    private static void demonstrateCustomThreadPool() throws InterruptedException {
        // 创建自定义线程池
        ThreadPoolExecutor customPool = new ThreadPoolExecutor(
            2,                      // 核心线程数
            4,                      // 最大线程数
            60L,                    // 空闲线程存活时间
            TimeUnit.SECONDS,       // 时间单位
            new ArrayBlockingQueue<>(2),  // 工作队列
            new ThreadFactory() {    // 线程工厂
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "CustomThread-" + threadNumber.getAndIncrement());
                    if (t.isDaemon()) t.setDaemon(false);
                    if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );

        System.out.println("\n=== Custom ThreadPool Demo ===");

        // 提交任务观察线程池行为
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            try {
                customPool.submit(() -> {
                    System.out.println("Task " + taskId + " executed by " +
                        Thread.currentThread().getName() +
                        " | Pool size: " + customPool.getPoolSize() +
                        " | Active threads: " + customPool.getActiveCount() +
                        " | Queue size: " + customPool.getQueue().size());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (RejectedExecutionException e) {
                System.out.println("Task " + taskId + " was rejected");
            }
        }

        // 关闭线程池
        customPool.shutdown();
        customPool.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * 演示可监控的线程池
     */
    private static void demonstrateMonitorableThreadPool() throws InterruptedException {
        // 创建可监控的线程池
        MonitorableThreadPool monitorPool = new MonitorableThreadPool(
            3, 3, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

        System.out.println("\n=== Monitorable ThreadPool Demo ===");

        // 提交一些任务
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            monitorPool.submit(() -> {
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 定期打印监控信息
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);
            System.out.println("Average task time: " +
                String.format("%.2f", monitorPool.getAverageTaskTime()) + "ms" +
                " | Completed tasks: " + monitorPool.getCompletedTaskCount() +
                " | Active threads: " + monitorPool.getActiveCount());
        }

        // 关闭线程池
        monitorPool.shutdown();
        monitorPool.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * 演示异常处理
     */
    private static void demonstrateExceptionHandling() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    System.out.println("Task threw exception: " + t.getMessage());
                }
            }
        };

        System.out.println("\n=== Exception Handling Demo ===");

        // 提交一个会抛出异常的任务
        executor.submit(() -> {
            throw new RuntimeException("Task failed");
        });

        // 提交一个正常的任务
        executor.submit(() ->
            System.out.println("Normal task executed"));

        executor.shutdown();
    }

    /**
     * 演示定时任务
     */
    private static void demonstrateScheduledTasks() throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        System.out.println("\n=== Scheduled Tasks Demo ===");

        // 延迟执行
        scheduler.schedule(() ->
            System.out.println("Delayed task executed"),
            2, TimeUnit.SECONDS);

        // 固定速率执行
        scheduler.scheduleAtFixedRate(() ->
            System.out.println("Fixed rate task executed"),
            0, 1, TimeUnit.SECONDS);

        // 固定延迟执行
        scheduler.scheduleWithFixedDelay(() ->
            System.out.println("Fixed delay task executed"),
            0, 1, TimeUnit.SECONDS);

        // 等待一段时间观察执行情况
        Thread.sleep(5000);

        scheduler.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        // 演示不同类型的线程池
        demonstrateThreadPools();

        // 演示自定义线程池
        demonstrateCustomThreadPool();

        // 演示可监控的线程池
        demonstrateMonitorableThreadPool();

        // 演示异常处理
        demonstrateExceptionHandling();

        // 演示定时任务
        demonstrateScheduledTasks();
    }
}
