package collection;

import java.util.*;

public class LinkedListDemo {
    public static void main(String[] args) {
        // 1. LinkedList的基本操作
        basicOperations();
        
        // 2. LinkedList作为双端队列使用
        dequeOperations();
        
        // 3. LinkedList作为栈使用
        stackOperations();
        
        // 4. LinkedList vs ArrayList性能对比
        performanceComparison();
        
        // 5. LinkedList的遍历方式对比
        iterationComparison();
    }

    // 基本操作示例
    private static void basicOperations() {
        System.out.println("\n=== 基本操作示例 ===");
        
        LinkedList<String> list = new LinkedList<>();
        
        // 添加元素
        list.add("Java");
        list.addFirst("Python");    // 添加到头部
        list.addLast("JavaScript"); // 添加到尾部
        list.add(1, "C++");        // 在指定位置添加
        
        System.out.println("初始列表: " + list);
        
        // 获取元素
        String first = list.getFirst();  // 获取第一个元素
        String last = list.getLast();    // 获取最后一个元素
        String middle = list.get(1);     // 获取指定位置元素
        
        System.out.println("第一个元素: " + first);
        System.out.println("最后一个元素: " + last);
        System.out.println("中间元素: " + middle);
        
        // 删除元素
        list.removeFirst();             // 删除第一个元素
        list.removeLast();              // 删除最后一个元素
        list.remove(1);                // 删除指定位置元素
        
        System.out.println("删除后的列表: " + list);
    }

    // 双端队列操作示例
    private static void dequeOperations() {
        System.out.println("\n=== 双端队列操作示例 ===");
        
        Deque<String> deque = new LinkedList<>();
        
        // 添加元素
        deque.offerFirst("First");   // 在头部添加
        deque.offerLast("Last");     // 在尾部添加
        System.out.println("初始双端队列: " + deque);
        
        // 查看元素
        String peekFirst = deque.peekFirst();  // 查看头部元素
        String peekLast = deque.peekLast();    // 查看尾部元素
        System.out.println("头部元素: " + peekFirst);
        System.out.println("尾部元素: " + peekLast);
        
        // 移除元素
        String pollFirst = deque.pollFirst();  // 移除并返回头部元素
        String pollLast = deque.pollLast();    // 移除并返回尾部元素
        System.out.println("移除的头部元素: " + pollFirst);
        System.out.println("移除的尾部元素: " + pollLast);
        System.out.println("移除后的双端队列: " + deque);
    }

    // 栈操作示例
    private static void stackOperations() {
        System.out.println("\n=== 栈操作示例 ===");
        
        LinkedList<String> stack = new LinkedList<>();
        
        // 压栈
        stack.push("First");
        stack.push("Second");
        stack.push("Third");
        System.out.println("压栈后: " + stack);
        
        // 查看栈顶元素
        String peek = stack.peek();
        System.out.println("栈顶元素: " + peek);
        
        // 出栈
        String pop = stack.pop();
        System.out.println("弹出的元素: " + pop);
        System.out.println("弹出后的栈: " + stack);
    }

    // ArrayList vs LinkedList性能对比
    private static void performanceComparison() {
        System.out.println("\n=== 性能对比 ===");
        
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        
        // 添加元素性能对比
        long startTime = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            arrayList.add(0, i); // 在开头添加元素
        }
        long arrayListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            linkedList.add(0, i); // 在开头添加元素
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        System.out.println("在开头添加100000个元素:");
        System.out.println("ArrayList耗时: " + arrayListTime / 1_000_000.0 + " ms");
        System.out.println("LinkedList耗时: " + linkedListTime / 1_000_000.0 + " ms");
        
        // 随机访问性能对比
        startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            arrayList.get(i);
        }
        arrayListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            linkedList.get(i);
        }
        linkedListTime = System.nanoTime() - startTime;
        
        System.out.println("\n随机访问10000次:");
        System.out.println("ArrayList耗时: " + arrayListTime / 1_000_000.0 + " ms");
        System.out.println("LinkedList耗时: " + linkedListTime / 1_000_000.0 + " ms");
    }

    // 遍历方式对比
    private static void iterationComparison() {
        System.out.println("\n=== 遍历方式对比 ===");
        
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < 100000; i++) {
            list.add(i);
        }
        
        // 1. for循环遍历
        long startTime = System.nanoTime();
        for (int i = 0; i < list.size(); i++) {
            Integer value = list.get(i);
        }
        long forLoopTime = System.nanoTime() - startTime;
        
        // 2. 增强for循环遍历
        startTime = System.nanoTime();
        for (Integer value : list) {
            // do nothing
        }
        long enhancedForTime = System.nanoTime() - startTime;
        
        // 3. Iterator遍历
        startTime = System.nanoTime();
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            Integer value = iterator.next();
        }
        long iteratorTime = System.nanoTime() - startTime;
        
        System.out.println("遍历100000个元素:");
        System.out.println("for循环耗时: " + forLoopTime / 1_000_000.0 + " ms");
        System.out.println("增强for循环耗时: " + enhancedForTime / 1_000_000.0 + " ms");
        System.out.println("Iterator耗时: " + iteratorTime / 1_000_000.0 + " ms");
    }
}
