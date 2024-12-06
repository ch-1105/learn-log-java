package com.ch.basic.thread;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 线程池实际应用示例
 */
public class ThreadPoolPractical {

    /**
     * 模拟订单处理系统
     */
    static class OrderProcessor {
        // 订单处理线程池
        private final ExecutorService orderProcessPool;
        // 异步通知线程池
        private final ExecutorService notificationPool;
        // 定时任务线程池
        private final ScheduledExecutorService scheduledPool;

        public OrderProcessor() {
            // 处理订单的线程池：核心业务，使用固定线程池
            orderProcessPool = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "Order-Processor-" + threadNumber.getAndIncrement());
                        t.setPriority(Thread.MAX_PRIORITY); // 设置最高优先级
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者运行
            );

            // 发送通知的线程池：IO密集型，使用缓存线程池
            notificationPool = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "Notification-Sender");
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            });

            // 定时任务线程池：用于定时检查和清理
            scheduledPool = Executors.newScheduledThreadPool(2);

            // 启动监控任务
            startMonitoring();
        }

        // 处理订单
        public Future<String> processOrder(String orderId) {
            return orderProcessPool.submit(() -> {
                try {
                    System.out.println("Processing order: " + orderId +
                        " by thread: " + Thread.currentThread().getName());
                    // 模拟订单处理
                    Thread.sleep(1000);

                    // 异步发送通知
                    sendNotification(orderId);

                    return "Order " + orderId + " processed successfully";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Order processing interrupted", e);
                }
            });
        }

        // 批量处理订单
        public List<Future<String>> processBatchOrders(List<String> orderIds) {
            List<Future<String>> results = new ArrayList<>();
            for (String orderId : orderIds) {
                results.add(processOrder(orderId));
            }
            return results;
        }

        // 发送通知
        private void sendNotification(String orderId) {
            notificationPool.execute(() -> {
                try {
                    System.out.println("Sending notification for order: " + orderId +
                        " by thread: " + Thread.currentThread().getName());
                    Thread.sleep(500); // 模拟发送通知
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 启动监控
        private void startMonitoring() {
            // 每5秒打印线程池状态
            scheduledPool.scheduleAtFixedRate(() -> {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) orderProcessPool;
                System.out.println("\n=== Thread Pool Status ===");
                System.out.println("Active Threads: " + pool.getActiveCount());
                System.out.println("Pool Size: " + pool.getPoolSize());
                System.out.println("Task Count: " + pool.getTaskCount());
                System.out.println("Completed Tasks: " + pool.getCompletedTaskCount());
                System.out.println("Queue Size: " + pool.getQueue().size());
            }, 0, 5, TimeUnit.SECONDS);

            // 每小时执行一次清理任务
            scheduledPool.scheduleAtFixedRate(() -> {
                System.out.println("Performing cleanup tasks...");
                // 模拟清理操作
            }, 1, 60, TimeUnit.MINUTES);
        }

        // 关闭所有线程池
        public void shutdown() {
            System.out.println("Shutting down thread pools...");
            orderProcessPool.shutdown();
            notificationPool.shutdown();
            scheduledPool.shutdown();

            try {
                // 等待任务完成
                if (!orderProcessPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    orderProcessPool.shutdownNow();
                }
                if (!notificationPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    notificationPool.shutdownNow();
                }
                if (!scheduledPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduledPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 模拟高并发场景
     */
    static class LoadSimulator {
        private final OrderProcessor processor;
        private final Random random = new Random();

        public LoadSimulator(OrderProcessor processor) {
            this.processor = processor;
        }

        public void simulateLoad() {
            // 模拟突发流量
            for (int i = 0; i < 50; i++) {
                String orderId = "ORD-" + (1000 + i);
                processor.processOrder(orderId);

                // 随机延迟，模拟真实请求
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 模拟批量处理
            List<String> batchOrders = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                batchOrders.add("BATCH-ORD-" + (2000 + i));
            }
            List<Future<String>> results = processor.processBatchOrders(batchOrders);

            // 等待批量处理结果
            for (Future<String> result : results) {
                try {
                    System.out.println(result.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        // 创建订单处理器
        OrderProcessor processor = new OrderProcessor();

        // 创建负载模拟器
        LoadSimulator simulator = new LoadSimulator(processor);

        try {
            // 运行模拟测试
            System.out.println("Starting load simulation...");
            simulator.simulateLoad();

            // 等待一段时间观察结果
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 关闭线程池
            processor.shutdown();
        }
    }
}
