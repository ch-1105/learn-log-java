package com.ch.basic.collection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * HashSet的使用示例和性能测试
 */
public class HashSetDemo {

    /**
     * 基本操作示例
     */
    public static void basicOperations() {
        Set<String> set = new HashSet<>();

        // 添加元素
        set.add("Java");
        set.add("Python");
        set.add("JavaScript");

        // 添加重复元素
        boolean added = set.add("Java"); // 返回false
        System.out.println("Added duplicate element: " + added);

        // 删除元素
        set.remove("Python");

        // 检查元素
        System.out.println("Contains Java? " + set.contains("Java"));

        // 遍历元素
        for (String lang : set) {
            System.out.println(lang);
        }
    }

    /**
     * 自定义对象示例
     */
    public static void customObjectExample() {
        Set<Person> personSet = new HashSet<>();

        Person p1 = new Person("Alice", 25);
        Person p2 = new Person("Bob", 30);
        Person p3 = new Person("Alice", 25); // 与p1相同

        personSet.add(p1);
        personSet.add(p2);
        personSet.add(p3); // 不会被添加，因为equals返回true

        System.out.println("Set size: " + personSet.size()); // 输出2

        // 遍历并打印
        for (Person p : personSet) {
            System.out.println(p);
        }
    }

    /**
     * 性能优化示例
     */
    public static void performanceOptimization() {
        // 1. 初始容量优化
        long startTime = System.nanoTime();
        Set<Integer> set1 = new HashSet<>(10000);
        for (int i = 0; i < 10000; i++) {
            set1.add(i);
        }
        long endTime = System.nanoTime();
        System.out.println("With initial capacity: " + (endTime - startTime) + "ns");

        // 2. 不指定初始容量
        startTime = System.nanoTime();
        Set<Integer> set2 = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            set2.add(i);
        }
        endTime = System.nanoTime();
        System.out.println("Without initial capacity: " + (endTime - startTime) + "ns");

        // 3. 批量操作
        startTime = System.nanoTime();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }
        Set<Integer> set3 = new HashSet<>(list);
        endTime = System.nanoTime();
        System.out.println("Bulk operation: " + (endTime - startTime) + "ns");
    }

    /**
     * 并发操作示例
     */
    public static void concurrentOperations() {
        // 1. 同步包装
        Set<String> syncSet = Collections.synchronizedSet(new HashSet<>());

        // 2. ConcurrentHashMap.KeySetView
        Set<String> concurrentSet = ConcurrentHashMap.newKeySet();

        // 多线程添加元素
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                syncSet.add("Thread1-" + i);
                concurrentSet.add("Thread1-" + i);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                syncSet.add("Thread2-" + i);
                concurrentSet.add("Thread2-" + i);
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("SyncSet size: " + syncSet.size());
        System.out.println("ConcurrentSet size: " + concurrentSet.size());
    }

    /**
     * Stream操作示例
     */
    public static void streamOperations() {
        Set<String> set = new HashSet<>();
        set.add("Java");
        set.add("Python");
        set.add("JavaScript");
        set.add("C++");

        // 过滤操作
        Set<String> filtered = set.stream()
                .filter(lang -> lang.length() > 4)
                .collect(Collectors.toSet());
        System.out.println("Filtered languages: " + filtered);

        // 映射操作
        Set<Integer> lengths = set.stream()
                .map(String::length)
                .collect(Collectors.toSet());
        System.out.println("Language lengths: " + lengths);

        // 并行操作
        Set<String> upperCase = set.parallelStream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        System.out.println("Uppercase languages: " + upperCase);
    }

    /**
     * JDK新特性示例
     */
    public static void modernFeatures() {
        // 1. 不可变集合（JDK 9+）
        Set<String> immutableSet = Set.of("Java", "Python", "JavaScript");

        // 2. 集合工厂方法（JDK 9+）
        Set<String> copySet = Set.copyOf(immutableSet);

        // 3. 条件删除（JDK 8+）
        Set<String> set = new HashSet<>(immutableSet);
        set.removeIf(lang -> lang.length() > 5);

        System.out.println("After removeIf: " + set);
    }

    public static void main(String[] args) {
        System.out.println("=== Basic Operations ===");
        basicOperations();

        System.out.println("\n=== Custom Object Example ===");
        customObjectExample();

        System.out.println("\n=== Performance Optimization ===");
        performanceOptimization();

        System.out.println("\n=== Concurrent Operations ===");
        concurrentOperations();

        System.out.println("\n=== Stream Operations ===");
        streamOperations();

        System.out.println("\n=== Modern Features ===");
        modernFeatures();
    }

    /**
     * Person类 - 用于演示自定义对象
     */
    static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + '}';
        }
    }
}
