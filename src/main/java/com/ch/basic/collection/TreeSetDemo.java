package com.ch.basic.collection;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * TreeSet的使用示例和性能测试
 */
public class TreeSetDemo {

    /**
     * 基本操作示例
     */
    public static void basicOperations() {
        TreeSet<String> set = new TreeSet<>();

        // 添加元素
        set.add("Java");
        set.add("Python");
        set.add("JavaScript");

        // 元素自动排序
        System.out.println("Sorted elements: " + set);

        // 导航方法
        System.out.println("First: " + set.first());
        System.out.println("Last: " + set.last());
        System.out.println("Lower than 'Python': " + set.lower("Python"));
        System.out.println("Higher than 'Java': " + set.higher("Java"));

        // 范围视图
        SortedSet<String> subset = set.subSet("Java", "Python");
        System.out.println("Subset: " + subset);
    }

    /**
     * 自定义比较器示例
     */
    public static void comparatorExample() {
        // 使用自定义比较器（按字符串长度排序）
        TreeSet<String> set = new TreeSet<>((s1, s2) -> {
            int lenCompare = Integer.compare(s1.length(), s2.length());
            return lenCompare != 0 ? lenCompare : s1.compareTo(s2);
        });

        set.add("Java");
        set.add("C");
        set.add("Python");
        set.add("JavaScript");

        System.out.println("Sorted by length: " + set);
    }

    /**
     * 自定义对象排序示例
     */
    public static void customObjectExample() {
        TreeSet<Person> set = new TreeSet<>();

        set.add(new Person("Alice", 25));
        set.add(new Person("Bob", 30));
        set.add(new Person("Charlie", 20));

        System.out.println("Sorted persons:");
        for (Person p : set) {
            System.out.println(p);
        }
    }

    /**
     * 范围操作示例
     */
    public static void rangeOperations() {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 0; i < 100; i += 10) {
            set.add(i);
        }

        // 各种范围操作
        System.out.println("Original set: " + set);
        System.out.println("Numbers < 50: " + set.headSet(50));
        System.out.println("Numbers >= 50: " + set.tailSet(50));
        System.out.println("Numbers between 30 and 70: " + set.subSet(30, 70));

        // 降序视图
        System.out.println("Descending set: " + set.descendingSet());

        // 导航方法
        System.out.println("Ceiling of 45: " + set.ceiling(45)); // ≥45的最小元素
        System.out.println("Floor of 45: " + set.floor(45));     // ≤45的最大元素
    }

    /**
     * 性能对比示例
     */
    public static void performanceComparison() {
        int size = 100000;

        // TreeSet
        long startTime = System.nanoTime();
        TreeSet<Integer> treeSet = new TreeSet<>();
        for (int i = 0; i < size; i++) {
            treeSet.add(i);
        }
        long treeSetTime = System.nanoTime() - startTime;

        // HashSet
        startTime = System.nanoTime();
        HashSet<Integer> hashSet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            hashSet.add(i);
        }
        long hashSetTime = System.nanoTime() - startTime;

        System.out.println("TreeSet insertion time: " + treeSetTime / 1_000_000 + "ms");
        System.out.println("HashSet insertion time: " + hashSetTime / 1_000_000 + "ms");

        // 查找操作
        startTime = System.nanoTime();
        treeSet.contains(size/2);
        long treeSetSearchTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        hashSet.contains(size/2);
        long hashSetSearchTime = System.nanoTime() - startTime;

        System.out.println("TreeSet search time: " + treeSetSearchTime + "ns");
        System.out.println("HashSet search time: " + hashSetSearchTime + "ns");
    }

    /**
     * 线程安全示例
     */
    public static void threadSafetyExample() {
        // 使用同步包装器
        TreeSet<String> set = new TreeSet<>();
        Set<String> syncSet = Collections.synchronizedSet(set);

        // 使用显式锁
        Lock lock = new ReentrantLock();
        TreeSet<String> lockedSet = new TreeSet<>();

        // 多线程添加元素
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                syncSet.add("Thread1-" + i);

                lock.lock();
                try {
                    lockedSet.add("Thread1-" + i);
                } finally {
                    lock.unlock();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                syncSet.add("Thread2-" + i);

                lock.lock();
                try {
                    lockedSet.add("Thread2-" + i);
                } finally {
                    lock.unlock();
                }
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
        System.out.println("LockedSet size: " + lockedSet.size());
    }

    /**
     * Stream操作示例
     */
    public static void streamOperations() {
        TreeSet<String> set = new TreeSet<>();
        set.add("Java");
        set.add("Python");
        set.add("JavaScript");
        set.add("C++");

        // 过滤和收集
        Set<String> filtered = set.stream()
                .filter(lang -> lang.length() > 4)
                .collect(Collectors.toCollection(TreeSet::new));
        System.out.println("Filtered languages: " + filtered);

        // 映射和收集
        TreeSet<Integer> lengths = set.stream()
                .map(String::length)
                .collect(Collectors.toCollection(TreeSet::new));
        System.out.println("Sorted lengths: " + lengths);
    }

    public static void main(String[] args) {
        System.out.println("=== Basic Operations ===");
        basicOperations();

        System.out.println("\n=== Comparator Example ===");
        comparatorExample();

        System.out.println("\n=== Custom Object Example ===");
        customObjectExample();

        System.out.println("\n=== Range Operations ===");
        rangeOperations();

        System.out.println("\n=== Performance Comparison ===");
        performanceComparison();

        System.out.println("\n=== Thread Safety Example ===");
        threadSafetyExample();

        System.out.println("\n=== Stream Operations ===");
        streamOperations();
    }

    /**
     * Person类 - 实现Comparable接口
     */
    static class Person implements Comparable<Person> {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public int compareTo(Person other) {
            int nameCompare = this.name.compareTo(other.name);
            return nameCompare != 0 ? nameCompare : Integer.compare(this.age, other.age);
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + '}';
        }
    }
}
