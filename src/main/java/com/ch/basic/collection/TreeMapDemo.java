package com.ch.basic.collection;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * TreeMap的使用示例和性能测试
 */
public class TreeMapDemo {

    /**
     * 基本操作示例
     */
    public static void basicOperations() {
        TreeMap<String, Integer> map = new TreeMap<>();

        // 添加元素
        map.put("Java", 95);
        map.put("Python", 92);
        map.put("JavaScript", 88);

        // 元素自动按键排序
        System.out.println("Sorted map: " + map);

        // 导航方法
        System.out.println("First key: " + map.firstKey());
        System.out.println("Last key: " + map.lastKey());
        System.out.println("Lower key than 'Python': " + map.lowerKey("Python"));
        System.out.println("Higher key than 'Java': " + map.higherKey("Java"));

        // 范围视图
        SortedMap<String, Integer> subMap = map.subMap("Java", "Python");
        System.out.println("SubMap: " + subMap);
    }

    /**
     * 自定义比较器示例
     */
    public static void comparatorExample() {
        // 使用自定义比较器（按字符串长度排序）
        TreeMap<String, Integer> map = new TreeMap<>((s1, s2) -> {
            int lenCompare = Integer.compare(s1.length(), s2.length());
            return lenCompare != 0 ? lenCompare : s1.compareTo(s2);
        });

        map.put("Java", 95);
        map.put("C", 85);
        map.put("Python", 92);
        map.put("JavaScript", 88);

        System.out.println("Sorted by length: " + map);
    }

    /**
     * 自定义对象作为键的示例
     */
    public static void customObjectExample() {
        TreeMap<Person, String> map = new TreeMap<>();

        map.put(new Person("Alice", 25), "Developer");
        map.put(new Person("Bob", 30), "Manager");
        map.put(new Person("Charlie", 20), "Intern");

        System.out.println("Sorted persons:");
        map.forEach((person, role) ->
            System.out.println(person + " -> " + role));
    }

    /**
     * 范围操作示例
     */
    public static void rangeOperations() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < 100; i += 10) {
            map.put(i, "Value-" + i);
        }

        // 各种范围操作
        System.out.println("Original map: " + map);
        System.out.println("Keys < 50: " + map.headMap(50));
        System.out.println("Keys >= 50: " + map.tailMap(50));
        System.out.println("Keys between 30 and 70: " + map.subMap(30, 70));

        // 降序视图
        System.out.println("Descending map: " + map.descendingMap());

        // 导航方法
        System.out.println("Ceiling entry of 45: " + map.ceilingEntry(45));
        System.out.println("Floor entry of 45: " + map.floorEntry(45));
    }

    /**
     * 性能对比示例
     */
    public static void performanceComparison() {
        int size = 100000;

        // TreeMap
        long startTime = System.nanoTime();
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            treeMap.put(i, "Value-" + i);
        }
        long treeMapTime = System.nanoTime() - startTime;

        // HashMap
        startTime = System.nanoTime();
        HashMap<Integer, String> hashMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            hashMap.put(i, "Value-" + i);
        }
        long hashMapTime = System.nanoTime() - startTime;

        System.out.println("TreeMap insertion time: " + treeMapTime / 1_000_000 + "ms");
        System.out.println("HashMap insertion time: " + hashMapTime / 1_000_000 + "ms");

        // 查找操作
        startTime = System.nanoTime();
        treeMap.get(size/2);
        long treeMapSearchTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        hashMap.get(size/2);
        long hashMapSearchTime = System.nanoTime() - startTime;

        System.out.println("TreeMap search time: " + treeMapSearchTime + "ns");
        System.out.println("HashMap search time: " + hashMapSearchTime + "ns");
    }

    /**
     * 线程安全示例
     */
    public static void threadSafetyExample() {
        // 使用同步包装器
        TreeMap<String, Integer> map = new TreeMap<>();
        Map<String, Integer> syncMap = Collections.synchronizedMap(map);

        // 使用显式锁
        Lock lock = new ReentrantLock();
        TreeMap<String, Integer> lockedMap = new TreeMap<>();

        // 多线程添加元素
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                syncMap.put("Thread1-" + i, i);

                lock.lock();
                try {
                    lockedMap.put("Thread1-" + i, i);
                } finally {
                    lock.unlock();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                syncMap.put("Thread2-" + i, i);

                lock.lock();
                try {
                    lockedMap.put("Thread2-" + i, i);
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

        System.out.println("SyncMap size: " + syncMap.size());
        System.out.println("LockedMap size: " + lockedMap.size());
    }

    /**
     * Stream操作示例
     */
    public static void streamOperations() {
        TreeMap<String, Integer> map = new TreeMap<>();
        map.put("Java", 95);
        map.put("Python", 92);
        map.put("JavaScript", 88);
        map.put("C++", 90);

        // 过滤和收集
        Map<String, Integer> filtered = map.entrySet().stream()
                .filter(e -> e.getValue() >= 90)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    TreeMap::new
                ));
        System.out.println("High scores (>=90): " + filtered);

        // 转换值
        TreeMap<String, String> grades = new TreeMap<>((Comparator) map.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> convertToGrade(e.getValue()),
                    (e1, e2) -> e1,
                    TreeMap::new
                )));
        System.out.println("Grades: " + grades);
    }

    /**
     * 分数转换为等级
     */
    private static String convertToGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "D";
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
