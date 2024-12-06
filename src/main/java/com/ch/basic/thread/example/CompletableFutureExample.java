package com.ch.basic.thread.example;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * CompletableFuture示例
 * 展示CompletableFuture在实际场景中的应用
 */
@Slf4j
public class CompletableFutureExample {

    /**
     * 场景1：电商订单处理
     * 并行处理订单信息、库存信息和用户信息
     */
    static class OrderProcessor {
        @Data
        static class Order {
            private final String orderId;
            private String userInfo;
            private String inventoryInfo;
            private String paymentInfo;
            
            public Order(String orderId) {
                this.orderId = orderId;
            }
        }
        
        public CompletableFuture<Order> processOrder(String orderId) {
            Order order = new Order(orderId);
            
            // 并行获取各种信息
            CompletableFuture<String> userFuture = CompletableFuture
                .supplyAsync(() -> getUserInfo(orderId))
                .exceptionally(ex -> "默认用户信息");
                
            CompletableFuture<String> inventoryFuture = CompletableFuture
                .supplyAsync(() -> getInventoryInfo(orderId))
                .exceptionally(ex -> "库存信息获取失败");
                
            CompletableFuture<String> paymentFuture = CompletableFuture
                .supplyAsync(() -> getPaymentInfo(orderId))
                .exceptionally(ex -> "支付信息获取失败");
            
            // 组合所有结果
            return CompletableFuture.allOf(userFuture, inventoryFuture, paymentFuture)
                .thenApply(v -> {
                    order.setUserInfo(userFuture.join());
                    order.setInventoryInfo(inventoryFuture.join());
                    order.setPaymentInfo(paymentFuture.join());
                    return order;
                });
        }
        
        private String getUserInfo(String orderId) {
            simulateDelay(500);  // 模拟API调用延迟
            return "用户信息 for " + orderId;
        }
        
        private String getInventoryInfo(String orderId) {
            simulateDelay(600);
            return "库存信息 for " + orderId;
        }
        
        private String getPaymentInfo(String orderId) {
            simulateDelay(700);
            return "支付信息 for " + orderId;
        }
    }

    /**
     * 场景2：异步任务链与重试机制
     * 模拟数据处理管道，包含重试逻辑
     */
    static class DataProcessor {
        private final Random random = new Random();
        
        public CompletableFuture<String> processDataWithRetry(String input) {
            return processWithRetry(() -> processData(input), 3);
        }
        
        private <T> CompletableFuture<T> processWithRetry(Supplier<T> supplier, int maxRetries) {
            CompletableFuture<T> future = new CompletableFuture<>();
            processWithRetry(supplier, maxRetries, future, 1);
            return future;
        }
        
        private <T> void processWithRetry(Supplier<T> supplier, int maxRetries, 
                                        CompletableFuture<T> future, int attempt) {
            CompletableFuture.supplyAsync(supplier)
                .thenAccept(result -> future.complete(result))
                .exceptionally(throwable -> {
                    if (attempt <= maxRetries) {
                        log.warn("处理失败，第{}次重试", attempt);
                        processWithRetry(supplier, maxRetries, future, attempt + 1);
                    } else {
                        future.completeExceptionally(throwable);
                    }
                    return null;
                });
        }
        
        private String processData(String input) {
            // 模拟随机失败
            if (random.nextFloat() < 0.7) {
                throw new RuntimeException("处理失败");
            }
            return "Processed: " + input;
        }
    }

    /**
     * 场景3：并行批处理与超时控制
     * 处理批量数据，带有超时控制
     */
    static class BatchProcessor {
        public CompletableFuture<List<String>> processBatch(List<String> inputs, long timeout, TimeUnit unit) {
            List<CompletableFuture<String>> futures = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> processItem(input))
                    .orTimeout(timeout, unit)
                    .exceptionally(ex -> "处理失败: " + input))
                .collect(Collectors.toList());
            
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList()));
        }
        
        private String processItem(String input) {
            simulateDelay(new Random().nextInt(1000));  // 模拟不同处理时间
            return "Processed " + input;
        }
    }

    /**
     * 场景4：异步事件处理
     * 模拟事件处理系统，支持异步事件处理和回调
     */
    static class EventProcessor {
        private final ExecutorService executor = Executors.newFixedThreadPool(4);
        
        public CompletableFuture<Void> processEvent(String event) {
            return CompletableFuture.supplyAsync(() -> validateEvent(event), executor)
                .thenApplyAsync(this::enrichEvent, executor)
                .thenAcceptAsync(this::saveEvent, executor)
                .whenCompleteAsync((result, throwable) -> {
                    if (throwable != null) {
                        log.error("事件处理失败: " + event, throwable);
                    } else {
                        log.info("事件处理成功: {}", event);
                    }
                }, executor);
        }
        
        private String validateEvent(String event) {
            log.info("验证事件: {}", event);
            simulateDelay(200);
            return event;
        }
        
        private String enrichEvent(String event) {
            log.info("丰富事件: {}", event);
            simulateDelay(300);
            return event + " [enriched]";
        }
        
        private void saveEvent(String event) {
            log.info("保存事件: {}", event);
            simulateDelay(200);
        }
        
        public void shutdown() {
            executor.shutdown();
        }
    }

    // 工具方法：模拟延迟
    private static void simulateDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        // 测试场景1：电商订单处理
        log.info("=== 测试电商订单处理 ===");
        OrderProcessor orderProcessor = new OrderProcessor();
        CompletableFuture<OrderProcessor.Order> orderFuture = orderProcessor.processOrder("ORDER-001");
        OrderProcessor.Order order = orderFuture.get(2, TimeUnit.SECONDS);
        log.info("订单处理完成: {}", order);

        // 测试场景2：异步任务链与重试
        log.info("\n=== 测试异步任务链与重试 ===");
        DataProcessor dataProcessor = new DataProcessor();
        CompletableFuture<String> dataFuture = dataProcessor.processDataWithRetry("测试数据");
        try {
            String result = dataFuture.get(5, TimeUnit.SECONDS);
            log.info("数据处理结果: {}", result);
        } catch (Exception e) {
            log.error("数据处理最终失败", e);
        }

        // 测试场景3：并行批处理
        log.info("\n=== 测试并行批处理 ===");
        BatchProcessor batchProcessor = new BatchProcessor();
        List<String> inputs = List.of("Item1", "Item2", "Item3", "Item4", "Item5");
        CompletableFuture<List<String>> batchFuture = batchProcessor.processBatch(inputs, 2, TimeUnit.SECONDS);
        List<String> results = batchFuture.get();
        log.info("批处理结果: {}", results);

        // 测试场景4：异步事件处理
        log.info("\n=== 测试异步事件处理 ===");
        EventProcessor eventProcessor = new EventProcessor();
        CompletableFuture<Void> eventFuture = eventProcessor.processEvent("UserLoginEvent");
        eventFuture.get(3, TimeUnit.SECONDS);
        
        // 清理资源
        eventProcessor.shutdown();
    }
}
