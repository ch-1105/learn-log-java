package collection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class HashMapDemo {
    public static void main(String[] args) {
        // 1. 基本操作示例
        basicOperations();
        
        // 2. 性能优化示例
        performanceOptimization();
        
        // 3. 哈希冲突演示
        hashCollisionDemo();
        
        // 4. 线程安全示例
        threadSafetyDemo();
        
        // 5. 自定义对象作为key的示例
        customKeyDemo();
        
        // 6. 扩容过程演示
        resizeDemo();
        
        // 7. 红黑树转换演示
        treeifyDemo();
        
        // 8. Hash碰撞攻击演示
        hashCollisionAttackDemo();
        
        // 9. 性能优化技巧演示
        performanceOptimizationTipsDemo();
        
        // 10. 最佳实践演示
        bestPracticesDemo();
        
        // 11. 常见陷阱演示
        commonPitfallsDemo();
        
        // 12. 新版本特性演示
        modernFeaturesDemo();
    }

    // 基本操作示例
    private static void basicOperations() {
        System.out.println("\n=== 基本操作示例 ===");
        
        // 创建HashMap
        Map<String, Integer> map = new HashMap<>();
        
        // 添加元素
        map.put("Java", 95);
        map.put("Python", 92);
        map.put("JavaScript", 88);
        System.out.println("初始Map: " + map);
        
        // 获取元素
        Integer javaScore = map.get("Java");
        System.out.println("Java的分数: " + javaScore);
        
        // 检查键是否存在
        boolean hasKey = map.containsKey("Python");
        System.out.println("是否包含Python: " + hasKey);
        
        // 检查值是否存在
        boolean hasValue = map.containsValue(88);
        System.out.println("是否包含分数88: " + hasValue);
        
        // 更新值
        map.put("Java", 98);  // 更新已存在的key
        System.out.println("更新后的Map: " + map);
        
        // 获取所有键
        Set<String> keys = map.keySet();
        System.out.println("所有键: " + keys);
        
        // 获取所有值
        Collection<Integer> values = map.values();
        System.out.println("所有值: " + values);
        
        // 遍历Map
        System.out.println("\n遍历Map:");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        // 删除元素
        map.remove("Python");
        System.out.println("删除Python后: " + map);
        
        // 清空Map
        map.clear();
        System.out.println("清空后的大小: " + map.size());
    }

    // 性能优化示例
    private static void performanceOptimization() {
        System.out.println("\n=== 性能优化示例 ===");
        
        // 1. 不指定初始容量
        long startTime = System.nanoTime();
        Map<Integer, String> mapWithoutCapacity = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            mapWithoutCapacity.put(i, "Value" + i);
        }
        long timeWithoutCapacity = System.nanoTime() - startTime;
        
        // 2. 指定初始容量
        startTime = System.nanoTime();
        Map<Integer, String> mapWithCapacity = new HashMap<>((int)(100000 / 0.75 + 1));
        for (int i = 0; i < 100000; i++) {
            mapWithCapacity.put(i, "Value" + i);
        }
        long timeWithCapacity = System.nanoTime() - startTime;
        
        System.out.println("不指定初始容量耗时: " + timeWithoutCapacity / 1_000_000.0 + " ms");
        System.out.println("指定初始容量耗时: " + timeWithCapacity / 1_000_000.0 + " ms");
    }

    // 哈希冲突演示
    private static void hashCollisionDemo() {
        System.out.println("\n=== 哈希冲突演示 ===");
        
        // 创建一个自定义的类，使其产生相同的哈希码
        class CollisionKey {
            private final int value;
            
            CollisionKey(int value) {
                this.value = value;
            }
            
            @Override
            public int hashCode() {
                return 1; // 所有对象返回相同的哈希码，强制发生冲突
            }
            
            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof CollisionKey)) return false;
                CollisionKey other = (CollisionKey) obj;
                return value == other.value;
            }
            
            @Override
            public String toString() {
                return String.valueOf(value);
            }
        }
        
        // 创建HashMap并添加会发生哈希冲突的键
        Map<CollisionKey, String> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(new CollisionKey(i), "Value" + i);
        }
        
        System.out.println("发生哈希冲突的Map大小: " + map.size());
        System.out.println("所有元素: " + map);
        
        // 测试获取元素的性能（会遍历链表或红黑树）
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            map.get(new CollisionKey(i % 10));
        }
        long endTime = System.nanoTime();
        
        System.out.println("查找10000次的耗时: " + (endTime - startTime) / 1_000_000.0 + " ms");
    }

    // 线程安全示例
    private static void threadSafetyDemo() {
        System.out.println("\n=== 线程安全示例 ===");
        
        // 1. 普通HashMap（非线程安全）
        Map<String, Integer> unsafeMap = new HashMap<>();
        
        // 2. Collections.synchronizedMap（线程安全，但性能较差）
        Map<String, Integer> synchronizedMap = Collections.synchronizedMap(new HashMap<>());
        
        // 3. ConcurrentHashMap（线程安全，性能较好）
        Map<String, Integer> concurrentMap = new ConcurrentHashMap<>();
        
        // 创建多线程任务
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                String key = Thread.currentThread().getName() + "-" + i;
                unsafeMap.put(key, i);
                synchronizedMap.put(key, i);
                concurrentMap.put(key, i);
            }
        };
        
        // 创建多个线程
        Thread thread1 = new Thread(task, "Thread1");
        Thread thread2 = new Thread(task, "Thread2");
        
        // 启动线程
        thread1.start();
        thread2.start();
        
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 比较结果
        System.out.println("非线程安全Map大小（预期2000）: " + unsafeMap.size());
        System.out.println("synchronizedMap大小（预期2000）: " + synchronizedMap.size());
        System.out.println("concurrentMap大小（预期2000）: " + concurrentMap.size());
    }

    // 自定义对象作为key的示例
    private static void customKeyDemo() {
        System.out.println("\n=== 自定义对象作为key的示例 ===");
        
        class Person {
            private final String name;
            private final int age;
            
            Person(String name, int age) {
                this.name = name;
                this.age = age;
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Person)) return false;
                Person person = (Person) o;
                return age == person.age && Objects.equals(name, person.name);
            }
            
            @Override
            public int hashCode() {
                return Objects.hash(name, age);
            }
            
            @Override
            public String toString() {
                return name + "(" + age + ")";
            }
        }
        
        // 创建使用自定义对象作为key的Map
        Map<Person, String> map = new HashMap<>();
        
        // 添加元素
        Person p1 = new Person("Alice", 25);
        Person p2 = new Person("Bob", 30);
        Person p3 = new Person("Alice", 25);  // 与p1相等
        
        map.put(p1, "Developer");
        map.put(p2, "Manager");
        map.put(p3, "Senior Developer");  // 会覆盖p1的值
        
        System.out.println("Map大小: " + map.size());
        System.out.println("所有元素: " + map);
        
        // 测试相等性
        Person searchKey = new Person("Alice", 25);
        System.out.println("查找结果: " + map.get(searchKey));
    }

    // 扩容过程演示
    private static void resizeDemo() {
        System.out.println("\n=== 扩容过程演示 ===");
        
        // 创建一个初始容量为4的HashMap
        Map<String, Integer> map = new HashMap<>(4);
        
        System.out.println("添加元素前的大小: " + map.size());
        
        // 添加元素触发扩容
        for (int i = 0; i < 10; i++) {
            map.put("Key" + i, i);
            System.out.println("添加第" + (i + 1) + "个元素后的大小: " + map.size());
        }
    }

    // 红黑树转换演示
    private static void treeifyDemo() {
        System.out.println("\n=== 红黑树转换演示 ===");
        
        // 创建一个会发生哈希冲突的key类
        class TreeKey {
            private final int value;
            
            TreeKey(int value) {
                this.value = value;
            }
            
            @Override
            public int hashCode() {
                return 1; // 确保所有key都在同一个桶中
            }
            
            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof TreeKey)) return false;
                TreeKey other = (TreeKey) obj;
                return value == other.value;
            }
            
            @Override
            public String toString() {
                return String.valueOf(value);
            }
        }
        
        // 创建HashMap并添加足够多的元素以触发树化
        Map<TreeKey, String> map = new HashMap<>();
        
        System.out.println("开始添加元素...");
        for (int i = 0; i < 20; i++) {
            map.put(new TreeKey(i), "Value" + i);
            System.out.println("添加第" + (i + 1) + "个元素，当前大小: " + map.size());
        }
        
        // 测试查找性能
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            map.get(new TreeKey(i % 20));
        }
        long endTime = System.nanoTime();
        
        System.out.println("10000次查找操作耗时: " + (endTime - startTime) / 1_000_000.0 + " ms");
    }

    // Hash碰撞攻击演示
    private static void hashCollisionAttackDemo() {
        System.out.println("\n=== Hash碰撞攻击演示 ===");
        
        // 创建一个易受攻击的key类
        class VulnerableKey {
            private final String value;
            
            VulnerableKey(String value) {
                this.value = value;
            }
            
            @Override
            public int hashCode() {
                return 1; // 所有key都返回相同的hash值
            }
            
            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof VulnerableKey)) return false;
                VulnerableKey other = (VulnerableKey) obj;
                return Objects.equals(value, other.value);
            }
            
            @Override
            public String toString() {
                return value;
            }
        }
        
        // 模拟正常使用
        Map<String, String> normalMap = new HashMap<>();
        Map<VulnerableKey, String> vulnerableMap = new HashMap<>();
        
        // 添加大量元素
        System.out.println("开始性能对比测试...");
        
        // 1. 正常Map的性能
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            normalMap.put("Key" + i, "Value");
        }
        long normalTime = System.nanoTime() - startTime;
        
        // 2. 受攻击Map的性能
        startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            vulnerableMap.put(new VulnerableKey("Key" + i), "Value");
        }
        long attackTime = System.nanoTime() - startTime;
        
        System.out.println("正常Map添加耗时: " + normalTime / 1_000_000.0 + " ms");
        System.out.println("受攻击Map添加耗时: " + attackTime / 1_000_000.0 + " ms");
        
        // 测试查找性能
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            normalMap.get("Key" + i);
        }
        normalTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            vulnerableMap.get(new VulnerableKey("Key" + i));
        }
        attackTime = System.nanoTime() - startTime;
        
        System.out.println("\n1000次查找操作:");
        System.out.println("正常Map查找耗时: " + normalTime / 1_000_000.0 + " ms");
        System.out.println("受攻击Map查找耗时: " + attackTime / 1_000_000.0 + " ms");
    }

    // 性能优化技巧演示
    private static void performanceOptimizationTipsDemo() {
        System.out.println("\n=== 性能优化技巧演示 ===");
        
        // 1. 初始容量优化
        System.out.println("1. 初始容量优化");
        int expectedSize = 10000;
        long startTime = System.nanoTime();
        
        // 未优化版本
        Map<String, String> unoptimizedMap = new HashMap<>();
        for (int i = 0; i < expectedSize; i++) {
            unoptimizedMap.put("Key" + i, "Value" + i);
        }
        long unoptimizedTime = System.nanoTime() - startTime;
        
        // 优化版本
        startTime = System.nanoTime();
        int initialCapacity = (int) (expectedSize / 0.75f + 1.0f);
        Map<String, String> optimizedMap = new HashMap<>(initialCapacity);
        for (int i = 0; i < expectedSize; i++) {
            optimizedMap.put("Key" + i, "Value" + i);
        }
        long optimizedTime = System.nanoTime() - startTime;
        
        System.out.println("未优化版本耗时: " + unoptimizedTime / 1_000_000.0 + " ms");
        System.out.println("优化版本耗时: " + optimizedTime / 1_000_000.0 + " ms");
        
        // 2. Key优化示例
        System.out.println("\n2. Key优化示例");
        class OptimizedKey {
            private final String value;
            private final int hash;
            
            OptimizedKey(String value) {
                this.value = value;
                this.hash = value != null ? value.hashCode() : 0;
            }
            
            @Override
            public int hashCode() {
                return hash;
            }
            
            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof OptimizedKey)) return false;
                OptimizedKey other = (OptimizedKey) obj;
                return Objects.equals(value, other.value);
            }
        }
        
        Map<OptimizedKey, String> optimizedKeyMap = new HashMap<>();
        OptimizedKey key = new OptimizedKey("test");
        
        startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            optimizedKeyMap.get(key);
        }
        System.out.println("优化的Key查找耗时: " + (System.nanoTime() - startTime) / 1_000_000.0 + " ms");
    }
    
    // 最佳实践演示
    private static void bestPracticesDemo() {
        System.out.println("\n=== 最佳实践演示 ===");
        
        // 1. 线程安全示例
        System.out.println("1. 线程安全示例");
        Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> concurrentMap = new ConcurrentHashMap<>();
        
        // 并发测试
        int threadCount = 10;
        int operationsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount * 2);
        
        // 测试synchronizedMap
        startTime = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    syncMap.put("Key" + j, "Value" + j);
                }
                latch.countDown();
            }).start();
        }
        
        // 测试ConcurrentHashMap
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    concurrentMap.put("Key" + j, "Value" + j);
                }
                latch.countDown();
            }).start();
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("synchronizedMap大小: " + syncMap.size());
        System.out.println("concurrentMap大小: " + concurrentMap.size());
        
        // 2. 迭代优化示例
        System.out.println("\n2. 迭代优化示例");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            map.put("Key" + i, "Value" + i);
        }
        
        // keySet方式
        startTime = System.nanoTime();
        for (String key : map.keySet()) {
            String value = map.get(key);
        }
        long keySetTime = System.nanoTime() - startTime;
        
        // entrySet方式
        startTime = System.nanoTime();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
        }
        long entrySetTime = System.nanoTime() - startTime;
        
        System.out.println("keySet迭代耗时: " + keySetTime / 1_000_000.0 + " ms");
        System.out.println("entrySet迭代耗时: " + entrySetTime / 1_000_000.0 + " ms");
    }
    
    // 常见陷阱演示
    private static void commonPitfallsDemo() {
        System.out.println("\n=== 常见陷阱演示 ===");
        
        // 1. 可变Key问题
        System.out.println("1. 可变Key问题");
        class MutableKey {
            private String value;
            
            MutableKey(String value) {
                this.value = value;
            }
            
            public void setValue(String value) {
                this.value = value;
            }
            
            @Override
            public int hashCode() {
                return value.hashCode();
            }
            
            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof MutableKey)) return false;
                MutableKey other = (MutableKey) obj;
                return Objects.equals(value, other.value);
            }
        }
        
        Map<MutableKey, String> map = new HashMap<>();
        MutableKey key = new MutableKey("original");
        map.put(key, "value");
        System.out.println("插入后值: " + map.get(key));
        
        key.setValue("modified");
        System.out.println("修改key后值: " + map.get(key));
        System.out.println("Map大小: " + map.size());
        
        // 2. 并发修改问题
        System.out.println("\n2. 并发修改问题");
        Map<String, String> testMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            testMap.put("Key" + i, "Value" + i);
        }
        
        try {
            for (String key : testMap.keySet()) {
                if (key.equals("Key5")) {
                    testMap.remove(key);
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("捕获到并发修改异常");
        }
        
        // 正确的移除方式
        Iterator<Map.Entry<String, String>> it = testMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if (entry.getKey().equals("Key6")) {
                it.remove();
            }
        }
        System.out.println("正确移除后的Map大小: " + testMap.size());
    }

    // 新版本特性演示
    private static void modernFeaturesDemo() {
        System.out.println("\n=== 新版本特性演示（JDK 9-21）===");
        
        // 1. 不可变集合工厂方法 (JDK 9+)
        System.out.println("1. 不可变集合工厂方法");
        Map<String, Integer> immutableMap = Map.of(
            "one", 1,
            "two", 2,
            "three", 3
        );
        System.out.println("不可变Map: " + immutableMap);
        
        // 2. Record类型的使用 (JDK 16+)
        System.out.println("\n2. Record类型的使用");
        record Person(String name, int age) {}
        
        Map<Person, String> recordMap = new HashMap<>();
        Person person = new Person("Alice", 25);
        recordMap.put(person, "Developer");
        System.out.println("Record作为key: " + recordMap.get(person));
        
        // 3. 模式匹配增强 (JDK 19+)
        System.out.println("\n3. 模式匹配增强");
        Object value = immutableMap.get("one");
        if (value instanceof Integer i && i > 0) {
            System.out.println("获取到正整数: " + i);
        }
        
        // 4. Switch表达式 (JDK 19+)
        System.out.println("\n4. Switch表达式");
        String size = switch(immutableMap.size()) {
            case 0 -> "空Map";
            case 1 -> "单元素Map";
            default -> "多元素Map";
        };
        System.out.println("Map大小描述: " + size);
        
        // 5. 并行流操作 (JDK 9+优化)
        System.out.println("\n5. 并行流操作性能");
        Map<String, Integer> largeMap = new HashMap<>();
        for (int i = 0; i < 1000000; i++) {
            largeMap.put("key" + i, i);
        }
        
        // 测试串行处理
        long startTime = System.nanoTime();
        long serialSum = largeMap.values().stream()
            .filter(v -> v % 2 == 0)
            .mapToLong(Integer::longValue)
            .sum();
        long serialTime = System.nanoTime() - startTime;
        
        // 测试并行处理
        startTime = System.nanoTime();
        long parallelSum = largeMap.values().parallelStream()
            .filter(v -> v % 2 == 0)
            .mapToLong(Integer::longValue)
            .sum();
        long parallelTime = System.nanoTime() - startTime;
        
        System.out.println("串行处理时间: " + serialTime / 1_000_000.0 + " ms");
        System.out.println("并行处理时间: " + parallelTime / 1_000_000.0 + " ms");
        
        // 6. 内存优化演示
        System.out.println("\n6. 内存优化");
        // 使用List.copyOf创建优化的不可变列表
        List<String> originalList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            originalList.add("item" + i);
        }
        List<String> optimizedList = List.copyOf(originalList);
        System.out.println("优化后的List大小: " + optimizedList.size());
        
        // 7. 集合工厂方法的性能对比
        System.out.println("\n7. 集合工厂方法性能对比");
        startTime = System.nanoTime();
        Map<String, Integer> traditionalMap = new HashMap<>();
        traditionalMap.put("one", 1);
        traditionalMap.put("two", 2);
        traditionalMap.put("three", 3);
        long traditionalTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Map<String, Integer> factoryMap = Map.of(
            "one", 1,
            "two", 2,
            "three", 3
        );
        long factoryTime = System.nanoTime() - startTime;
        
        System.out.println("传统方式创建时间: " + traditionalTime / 1_000.0 + " μs");
        System.out.println("工厂方法创建时间: " + factoryTime / 1_000.0 + " μs");
    }
}
