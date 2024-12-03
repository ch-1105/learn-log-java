import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

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
        
        // 5. 线程安全示例
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

    // 线程安全示例
    private static void threadSafeList() {
        System.out.println("\n=== 线程安全示例 ===");
        
        // 创建线程安全的ArrayList
        List<String> synchronizedList = Collections.synchronizedList(new ArrayList<>());
        
        // 模拟多线程环境
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                synchronizedList.add("Item " + i);
            }
        };
        
        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);
        
        thread1.start();
        thread2.start();
        
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("线程安全列表大小: " + synchronizedList.size());
    }
}
