package com.example.thread.basic;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;

/**
 * 线程池基础示例
 * 通过简单的示例来理解线程池的基本概念和使用方法
 */
@Slf4j
public class SimpleThreadPoolExample {

    /**
     * 演示最基础的线程池使用
     */
    public static void basicUsage() {
        // 创建一个固定大小的线程池，有3个线程
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        try {
            // 提交5个任务到线程池
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    log.info("Task {} is running in thread: {}", 
                            taskId, Thread.currentThread().getName());
                    try {
                        // 模拟任务执行时间
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    log.info("Task {} is completed", taskId);
                    return taskId;
                });
            }
        } finally {
            // 关闭线程池
            executor.shutdown();
        }
    }

    /**
     * 演示不同类型的任务提交方式
     */
    public static void differentSubmissionTypes() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            // 1. execute方法 - 没有返回值
            executor.execute(() -> {
                log.info("Task executed with execute()");
            });

            // 2. submit方法 - 返回Future对象
            Future<String> future = executor.submit(() -> {
                log.info("Task executed with submit()");
                return "Task Result";
            });

            // 获取任务结果
            try {
                String result = future.get(2, TimeUnit.SECONDS);
                log.info("Task result: {}", result);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("Error getting task result", e);
            }
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 演示任务超时处理
     */
    public static void taskTimeout() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try {
            Future<String> future = executor.submit(() -> {
                log.info("Starting long-running task");
                Thread.sleep(3000); // 模拟长时间运行的任务
                return "Task completed";
            });

            try {
                // 等待任务完成，但最多等待2秒
                String result = future.get(2, TimeUnit.SECONDS);
                log.info("Task result: {}", result);
            } catch (TimeoutException e) {
                log.warn("Task timed out");
                future.cancel(true); // 取消任务
            } catch (InterruptedException | ExecutionException e) {
                log.error("Task error", e);
            }
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 演示异常处理
     */
    public static void exceptionHandling() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try {
            Future<?> future = executor.submit(() -> {
                log.info("Starting task that will throw exception");
                throw new RuntimeException("Simulated error");
            });

            try {
                future.get(); // 等待任务完成
            } catch (ExecutionException e) {
                log.error("Task threw exception: {}", e.getCause().getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Task was interrupted");
            }
        } finally {
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        log.info("=== Basic Thread Pool Usage ===");
        basicUsage();

        log.info("\n=== Different Submission Types ===");
        differentSubmissionTypes();

        log.info("\n=== Task Timeout Handling ===");
        taskTimeout();

        log.info("\n=== Exception Handling ===");
        exceptionHandling();
    }
}
