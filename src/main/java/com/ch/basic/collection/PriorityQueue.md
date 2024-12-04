# PriorityQueue 详解

## 1. 基本原理

### 1.1 数据结构
```java
public class PriorityQueue<E> extends AbstractQueue<E>
    implements java.io.Serializable {
    
    // 存储元素的数组
    transient Object[] queue;
    
    // 元素个数
    private int size = 0;
    
    // 比较器
    private final Comparator<? super E> comparator;
}
```

### 1.2 核心特点
- 基于二叉堆实现（完全二叉树）
- 可以自然排序或使用比较器
- 非线程安全
- 不允许null元素
- 堆顶元素总是最小值（最小堆）

## 2. 源码分析

### 2.1 堆操作
```java
// 上浮操作
private void siftUp(int k, E x) {
    if (comparator != null)
        siftUpUsingComparator(k, x);
    else
        siftUpComparable(k, x);
}

// 下沉操作
private void siftDown(int k, E x) {
    if (comparator != null)
        siftDownUsingComparator(k, x);
    else
        siftDownComparable(k, x);
}

// 添加元素
public boolean offer(E e) {
    if (e == null)
        throw new NullPointerException();
    int i = size;
    if (i >= queue.length)
        grow(i + 1);
    size = i + 1;
    if (i == 0)
        queue[0] = e;
    else
        siftUp(i, e);
    return true;
}

// 获取并移除堆顶元素
public E poll() {
    if (size == 0)
        return null;
    int s = --size;
    E result = (E) queue[0];
    E x = (E) queue[s];
    queue[s] = null;
    if (s != 0)
        siftDown(0, x);
    return result;
}
```

## 3. 版本演进

### 3.1 JDK 9 改进
- 优化了堆操作性能
- 改进了序列化实现
```java
private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException {
    s.defaultWriteObject();
    s.writeInt(Math.min(size, queue.length));
    for (int i = 0; i < size; i++)
        s.writeObject(queue[i]);
}
```

### 3.2 JDK 10+ 优化
- 改进了内存使用
- 优化了比较器性能
```java
// 优化的比较器使用
@SuppressWarnings("unchecked")
private int siftDownUsingComparator(int k, E x) {
    int half = size >>> 1;
    while (k < half) {
        int child = (k << 1) + 1;
        Object c = queue[child];
        int right = child + 1;
        if (right < size &&
            comparator.compare((E) c, (E) queue[right]) > 0)
            c = queue[child = right];
        if (comparator.compare(x, (E) c) <= 0)
            break;
        queue[k] = c;
        k = child;
    }
    queue[k] = x;
    return k;
}
```

## 4. 性能优化

### 4.1 初始容量设置
```java
// 预知元素数量时，指定初始容量
PriorityQueue<Integer> pq = new PriorityQueue<>(1000);

// 指定初始容量和比较器
PriorityQueue<String> pq = new PriorityQueue<>(1000, 
    (s1, s2) -> s2.length() - s1.length());
```

### 4.2 批量操作
```java
// 使用addAll而不是循环add
List<Integer> list = Arrays.asList(3, 1, 4, 1, 5, 9);
PriorityQueue<Integer> pq = new PriorityQueue<>(list);

// 高效地移除所有元素
pq.clear();
```

## 5. 最佳实践

### 5.1 自定义对象排序
```java
class Task implements Comparable<Task> {
    private int priority;
    private String name;
    
    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority);
    }
}

// 使用自定义比较器
PriorityQueue<Task> taskQueue = new PriorityQueue<>((t1, t2) -> 
    t2.priority - t1.priority); // 高优先级在前
```

### 5.2 线程安全
```java
// 使用PriorityBlockingQueue
PriorityBlockingQueue<Integer> pbq = new PriorityBlockingQueue<>();

// 或使用同步包装
Queue<Integer> syncQueue = Collections.synchronizedQueue(
    new PriorityQueue<>());
```

### 5.3 常见用途
```java
// 1. 任务调度
PriorityQueue<Task> scheduler = new PriorityQueue<>();
scheduler.offer(new Task(1, "Low Priority"));
scheduler.offer(new Task(3, "High Priority"));

// 2. Top K问题
PriorityQueue<Integer> topK = new PriorityQueue<>();
for (int num : numbers) {
    topK.offer(num);
    if (topK.size() > k) {
        topK.poll();
    }
}
```

## 6. 常见陷阱

### 6.1 遍历顺序
```java
// 错误理解：遍历顺序就是优先级顺序
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.addAll(Arrays.asList(3, 1, 4, 1, 5, 9));
// 遍历顺序不一定是有序的

// 正确获取有序元素
while (!pq.isEmpty()) {
    System.out.println(pq.poll());
}
```

### 6.2 null元素处理
```java
// 错误示例：添加null元素
PriorityQueue<String> pq = new PriorityQueue<>();
pq.offer(null); // 抛出NullPointerException

// 正确示例：检查null
if (element != null) {
    pq.offer(element);
}
```

## 7. 面试要点

### 7.1 PriorityQueue vs 普通Queue
- PriorityQueue: 优先级排序
- LinkedList: FIFO顺序
- 时间复杂度不同

### 7.2 时间复杂度
- 插入（offer）：O(log n)
- 删除（poll）：O(log n)
- 查看（peek）：O(1)
- 包含（contains）：O(n)

### 7.3 二叉堆特性
1. 完全二叉树
2. 父节点总是小于等于子节点（最小堆）
3. 数组实现，索引关系：
   - 父节点：(i-1)/2
   - 左子节点：2*i + 1
   - 右子节点：2*i + 2

### 7.4 应用场景
- 任务调度系统
- Top K问题
- Dijkstra算法
- 数据流中位数
- 合并K个有序链表
