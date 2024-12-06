package com.ch.basic.thread.example;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * 并发工具类示例
 * 展示CountDownLatch、CyclicBarrier和Semaphore的实际应用场景
 */
@Slf4j
public class ConcurrentToolsExample {

    /**
     * 场景1：游戏匹配系统 - 使用CountDownLatch
     * 等待所有玩家准备完成后开始游戏
     */
    static class GameMatchingSystem {
        private final CountDownLatch startSignal;
        private final List<String> players;
        
        public GameMatchingSystem(int playerCount) {
            this.startSignal = new CountDownLatch(playerCount);
            this.players = new ArrayList<>();
        }
        
        public void playerReady(String playerName) {
            players.add(playerName);
            log.info("玩家 {} 已准备", playerName);
            startSignal.countDown();
        }
        
        public void waitForAllPlayersReady() throws InterruptedException {
            startSignal.await();
            log.info("所有玩家已准备完成，游戏开始！玩家列表: {}", players);
        }
    }

    /**
     * 场景2：并行数据处理 - 使用CyclicBarrier
     * 多阶段并行处理数据，每个阶段都需要等待所有线程完成
     */
    static class DataProcessor {
        private final CyclicBarrier barrier;
        private final int threadCount;
        private final List<String> data;
        
        public DataProcessor(int threadCount) {
            this.threadCount = threadCount;
            this.data = new ArrayList<>();
            this.barrier = new CyclicBarrier(threadCount, () -> {
                log.info("阶段完成，处理结果: {}", data);
                data.clear(); // 清理阶段数据，准备下一阶段
            });
        }
        
        public void processData(int stageCount) {
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            
            for (int stage = 1; stage <= stageCount; stage++) {
                final int currentStage = stage;
                for (int i = 0; i < threadCount; i++) {
                    final int threadId = i;
                    executor.submit(() -> {
                        try {
                            // 模拟数据处理
                            Thread.sleep(new Random().nextInt(1000));
                            String result = String.format("阶段%d-线程%d的结果", currentStage, threadId);
                            synchronized (data) {
                                data.add(result);
                            }
                            log.info("线程{}完成阶段{}处理", threadId, currentStage);
                            barrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            }
            
            executor.shutdown();
        }
    }

    /**
     * 场景3：连接池管理 - 使用Semaphore
     * 限制同时活跃的连接数量
     */
    static class ConnectionPool {
        private final Semaphore semaphore;
        private final List<String> connections;
        
        public ConnectionPool(int maxConnections) {
            this.semaphore = new Semaphore(maxConnections, true); // 使用公平模式
            this.connections = new ArrayList<>();
            // 初始化连接
            for (int i = 0; i < maxConnections; i++) {
                connections.add("Connection-" + i);
            }
        }
        
        public String acquireConnection() throws InterruptedException {
            semaphore.acquire();
            synchronized (connections) {
                String connection = connections.remove(0);
                log.info("获取连接: {}, 剩余可用连接数: {}", connection, semaphore.availablePermits());
                return connection;
            }
        }
        
        public void releaseConnection(String connection) {
            synchronized (connections) {
                connections.add(connection);
                log.info("释放连接: {}", connection);
            }
            semaphore.release();
        }
        
        public boolean tryAcquireConnection(long timeout, TimeUnit unit) throws InterruptedException {
            if (semaphore.tryAcquire(timeout, unit)) {
                synchronized (connections) {
                    if (!connections.isEmpty()) {
                        String connection = connections.remove(0);
                        log.info("尝试获取连接成功: {}", connection);
                        return true;
                    }
                    semaphore.release();
                }
            }
            log.info("尝试获取连接失败");
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 测试场景1：游戏匹配系统
        log.info("=== 测试游戏匹配系统 ===");
        GameMatchingSystem matchingSystem = new GameMatchingSystem(3);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        executor.submit(() -> {
            try {
                matchingSystem.waitForAllPlayersReady();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 模拟玩家陆续准备
        Thread.sleep(1000);
        matchingSystem.playerReady("Player1");
        Thread.sleep(500);
        matchingSystem.playerReady("Player2");
        Thread.sleep(800);
        matchingSystem.playerReady("Player3");
        
        // 测试场景2：并行数据处理
        log.info("\n=== 测试并行数据处理 ===");
        DataProcessor processor = new DataProcessor(3);
        processor.processData(2); // 处理2个阶段
        
        // 测试场景3：连接池管理
        log.info("\n=== 测试连接池管理 ===");
        ConnectionPool pool = new ConnectionPool(3);
        
        // 模拟多个线程同时请求连接
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    String connection = pool.acquireConnection();
                    Thread.sleep(new Random().nextInt(1000));
                    pool.releaseConnection(connection);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // 测试带超时的连接获取
        executor.submit(() -> {
            try {
                pool.tryAcquireConnection(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 等待所有任务完成
        Thread.sleep(5000);
        executor.shutdown();
    }
}
