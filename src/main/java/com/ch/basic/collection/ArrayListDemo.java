package com.ch.basic.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ArrayListDemo {
    public static void main(String[] args) {
        // 1. 创建ArrayList的几种方式
        basicOperations();

        // 2. ArrayList的常用操作
        commonOperations();

        // 3. ArrayList的遍历方式
        iterationMethods();

        // 4. ArrayList的性能优化示例
        performanceOptimization();

        // 5. 线程安全与非线程安全对比示例
        threadSafeList();
    }

    // 基本操作示例
    private static void basicOperations() {
        System.out.println("\n=== 基本操作示例 ===");

        // 创建默认大小的ArrayList
        List<String> list1 = new ArrayList<>();

        // 创建指定大小的ArrayList（推荐，避免扩容）
        List<String> list2 = new ArrayList<>(10);

        // 添加元素
        list1.add("Java");
        list1.add("Python");
        list1.add("JavaScript");

        // 在指定位置添加元素
        list1.add(1, "C++");

        System.out.println("列表内容: " + list1);

        // 获取元素
        String firstElement = list1.get(0);
        System.out.println("第一个元素: " + firstElement);

        // 修改元素
        list1.set(0, "Kotlin");
        System.out.println("修改后的列表: " + list1);

        // 删除元素
        list1.remove("Python");
        System.out.println("删除后的列表: " + list1);
    }

    // 常用操作示例
    private static void commonOperations() {
        System.out.println("\n=== 常用操作示例 ===");

        List<String> languages = new ArrayList<>();
        languages.add("Java");
        languages.add("Python");
        languages.add("JavaScript");

        // 检查元素是否存在
        boolean containsJava = languages.contains("Java");
        System.out.println("是否包含Java: " + containsJava);

        // 获取元素索引
        int index = languages.indexOf("Python");
        System.out.println("Python的索引: " + index);

        // 获取列表大小
        int size = languages.size();
        System.out.println("列表大小: " + size);

        // 清空列表
        languages.clear();
        System.out.println("清空后的大小: " + languages.size());

        // 检查列表是否为空
        boolean isEmpty = languages.isEmpty();
        System.out.println("列表是否为空: " + isEmpty);
    }

    // 遍历方式示例
    private static void iterationMethods() {
        System.out.println("\n=== 遍历方式示例 ===");

        List<String> fruits = new ArrayList<>();
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Orange");

        // 1. for循环遍历
        System.out.println("1. 使用for循环:");
        for (int i = 0; i < fruits.size(); i++) {
            System.out.println(fruits.get(i));
        }

        // 2. 增强for循环
        System.out.println("\n2. 使用增强for循环:");
        for (String fruit : fruits) {
            System.out.println(fruit);
        }

        // 3. Iterator遍历
        System.out.println("\n3. 使用Iterator:");
        Iterator<String> iterator = fruits.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

        // 4. forEach方法（Java 8+）
        System.out.println("\n4. 使用forEach方法:");
        fruits.forEach(System.out::println);
    }

    // 性能优化示例
    private static void performanceOptimization() {
        System.out.println("\n=== 性能优化示例 ===");

        // 预设容量，避免扩容
        long startTime = System.nanoTime();
        List<Integer> optimizedList = new ArrayList<>(10000);
        for (int i = 0; i < 10000; i++) {
            optimizedList.add(i);
        }
        long endTime = System.nanoTime();
        System.out.println("预设容量耗时: " + (endTime - startTime) + " ns");

        // 未预设容量，需要多次扩容
        startTime = System.nanoTime();
        List<Integer> unoptimizedList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            unoptimizedList.add(i);
        }
        endTime = System.nanoTime();
        System.out.println("未预设容量耗时: " + (endTime - startTime) + " ns");
    }

    // 线程安全与非线程安全对比示例
    private static void threadSafeList() {
        System.out.println("\n=== 线程安全与非线程安全对比示例 ===");

        // 创建普通ArrayList（非线程安全）
        List<String> unsafeList = new ArrayList<>();

        // 创建线程安全的ArrayList
        List<String> synchronizedList = Collections.synchronizedList(new ArrayList<>());

        // 创建多个线程操作这两个列表
        Runnable unsafeTask = () -> {
            for (int i = 0; i < 1000; i++) {
                unsafeList.add(Thread.currentThread().getName() + "-Item-" + i);
            }
        };

        Runnable safeTask = () -> {
            for (int i = 0; i < 1000; i++) {
                synchronizedList.add(Thread.currentThread().getName() + "-Item-" + i);
            }
        };

        // 测试非线程安全的ArrayList
        Thread unsafeThread1 = new Thread(unsafeTask, "Unsafe-1");
        Thread unsafeThread2 = new Thread(unsafeTask, "Unsafe-2");

        // 测试线程安全的ArrayList
        Thread safeThread1 = new Thread(safeTask, "Safe-1");
        Thread safeThread2 = new Thread(safeTask, "Safe-2");

        // 启动所有线程
        long startTime = System.currentTimeMillis();

        unsafeThread1.start();
        unsafeThread2.start();
        safeThread1.start();
        safeThread2.start();

        try {
            // 等待所有线程完成
            unsafeThread1.join();
            unsafeThread2.join();
            safeThread1.join();
            safeThread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();

        // 输出结果对比
        System.out.println("执行时间：" + (endTime - startTime) + "ms");
        System.out.println("非线程安全列表大小（预期2000）: " + unsafeList.size());
        System.out.println("线程安全列表大小（预期2000）: " + synchronizedList.size());

        // 检查是否有重复元素（通过线程名称区分）
        Set<String> unsafeSet = new HashSet<>(unsafeList);
        Set<String> safeSet = new HashSet<>(synchronizedList);

        // 计算每个线程实际添加的元素数量
        long unsafeThread1Count = unsafeList.stream()
                .filter(item -> item.startsWith("Unsafe-1")).count();
        long unsafeThread2Count = unsafeList.stream()
                .filter(item -> item.startsWith("Unsafe-2")).count();
        long safeThread1Count = synchronizedList.stream()
                .filter(item -> item.startsWith("Safe-1")).count();
        long safeThread2Count = synchronizedList.stream()
                .filter(item -> item.startsWith("Safe-2")).count();

        System.out.println("\n=== 非线程安全列表统计 ===");
        System.out.println("线程1添加元素数量: " + unsafeThread1Count);
        System.out.println("线程2添加元素数量: " + unsafeThread2Count);
        System.out.println("是否有重复元素: " + (unsafeList.size() != unsafeSet.size()));
        System.out.println("是否包含null: " + unsafeList.contains(null));

        System.out.println("\n=== 线程安全列表统计 ===");
        System.out.println("线程1添加元素数量: " + safeThread1Count);
        System.out.println("线程2添加元素数量: " + safeThread2Count);
        System.out.println("是否有重复元素: " + (synchronizedList.size() != safeSet.size()));
        System.out.println("是否包含null: " + synchronizedList.contains(null));
    }
}
