package com.ch.basic.collection;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * PriorityQueue的使用示例和性能测试
 */
public class PriorityQueueDemo {

    /**
     * 基本操作示例
     */
    public static void basicOperations() {
        // 创建最小堆
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        // 创建最大堆
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        // 添加元素
        minHeap.offer(3);
        minHeap.offer(1);
        minHeap.offer(4);
        minHeap.offer(1);
        minHeap.offer(5);

        System.out.println("Min Heap:");
        while (!minHeap.isEmpty()) {
            System.out.print(minHeap.poll() + " ");
        }
        System.out.println();

        // 添加到最大堆
        maxHeap.addAll(Arrays.asList(3, 1, 4, 1, 5));

        System.out.println("Max Heap:");
        while (!maxHeap.isEmpty()) {
            System.out.print(maxHeap.poll() + " ");
        }
        System.out.println();
    }

    /**
     * 自定义对象示例
     */
    public static void customObjectExample() {
        // 使用自定义比较器
        PriorityQueue<Task> taskQueue = new PriorityQueue<>((t1, t2) ->
            t2.getPriority() - t1.getPriority()); // 高优先级在前

        taskQueue.offer(new Task(1, "Low Priority Task"));
        taskQueue.offer(new Task(3, "High Priority Task"));
        taskQueue.offer(new Task(2, "Medium Priority Task"));

        System.out.println("Tasks in priority order:");
        while (!taskQueue.isEmpty()) {
            System.out.println(taskQueue.poll());
        }
    }

    /**
     * Top K问题示例
     */
    public static void topKExample() {
        int k = 3;
        int[] numbers = {4, 1, 3, 2, 7, 5, 8, 6};

        // 找最大的K个数（使用最小堆）
        PriorityQueue<Integer> topK = new PriorityQueue<>();

        for (int num : numbers) {
            topK.offer(num);
            if (topK.size() > k) {
                topK.poll();
            }
        }

        System.out.println("Top " + k + " numbers:");
        while (!topK.isEmpty()) {
            System.out.print(topK.poll() + " ");
        }
        System.out.println();
    }

    /**
     * 性能测试示例
     */
    public static void performanceTest() {
        int n = 1000000;

        // 添加性能
        long startTime = System.nanoTime();
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for (int i = 0; i < n; i++) {
            pq.offer(i);
        }
        long endTime = System.nanoTime();
        System.out.println("Time to add " + n + " elements: " +
            (endTime - startTime) / 1_000_000 + "ms");

        // 移除性能
        startTime = System.nanoTime();
        while (!pq.isEmpty()) {
            pq.poll();
        }
        endTime = System.nanoTime();
        System.out.println("Time to remove " + n + " elements: " +
            (endTime - startTime) / 1_000_000 + "ms");
    }

    /**
     * 线程安全示例
     */
    public static void threadSafetyExample() {
        PriorityBlockingQueue<Integer> pbq = new PriorityBlockingQueue<>();

        // 创建多个生产者线程
        Thread[] producers = new Thread[3];
        for (int i = 0; i < producers.length; i++) {
            final int id = i;
            producers[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    pbq.offer(id * 100 + j);
                }
            });
        }

        // 创建消费者线程
        Thread consumer = new Thread(() -> {
            List<Integer> consumed = new ArrayList<>();
            while (consumed.size() < 300) {
                Integer item = pbq.poll();
                if (item != null) {
                    consumed.add(item);
                }
            }
            System.out.println("Consumed " + consumed.size() + " items");
        });

        // 启动所有线程
        for (Thread producer : producers) {
            producer.start();
        }
        consumer.start();

        // 等待所有线程完成
        try {
            for (Thread producer : producers) {
                producer.join();
            }
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 中位数查找示例
     */
    public static void medianFindingExample() {
        // 使用两个堆来维护中位数
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        int[] numbers = {5, 2, 8, 1, 9, 3, 7, 4, 6};

        for (int num : numbers) {
            // 添加新数字
            if (maxHeap.isEmpty() || num < maxHeap.peek()) {
                maxHeap.offer(num);
            } else {
                minHeap.offer(num);
            }

            // 平衡两个堆
            if (maxHeap.size() > minHeap.size() + 1) {
                minHeap.offer(maxHeap.poll());
            } else if (minHeap.size() > maxHeap.size()) {
                maxHeap.offer(minHeap.poll());
            }

            // 打印当前中位数
            if (maxHeap.size() > minHeap.size()) {
                System.out.println("Current median: " + maxHeap.peek());
            } else {
                System.out.println("Current median: " +
                    (maxHeap.peek() + minHeap.peek()) / 2.0);
            }
        }
    }

    /**
     * Stream操作示例
     */
    public static void streamOperations() {
        List<Task> tasks = Arrays.asList(
            new Task(3, "High Priority"),
            new Task(1, "Low Priority"),
            new Task(2, "Medium Priority")
        );

        // 使用Stream创建PriorityQueue
        PriorityQueue<Task> taskQueue = tasks.stream()
            .collect(Collectors.toCollection(() ->
                new PriorityQueue<>(Comparator.comparingInt(Task::getPriority))));

        System.out.println("Tasks in priority order (using stream):");
        while (!taskQueue.isEmpty()) {
            System.out.println(taskQueue.poll());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Basic Operations ===");
        basicOperations();

        System.out.println("\n=== Custom Object Example ===");
        customObjectExample();

        System.out.println("\n=== Top K Example ===");
        topKExample();

        System.out.println("\n=== Performance Test ===");
        performanceTest();

        System.out.println("\n=== Thread Safety Example ===");
        threadSafetyExample();

        System.out.println("\n=== Median Finding Example ===");
        medianFindingExample();

        System.out.println("\n=== Stream Operations ===");
        streamOperations();
    }

    /**
     * Task类 - 用于演示优先级队列的自定义对象
     */
    static class Task {
        private int priority;
        private String name;

        public Task(int priority, String name) {
            this.priority = priority;
            this.name = name;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return "Task{priority=" + priority + ", name='" + name + "'}";
        }
    }
}
