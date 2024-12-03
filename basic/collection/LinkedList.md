# LinkedList 详解

## 1. 基本原理

### 1.1 数据结构
```java
public class LinkedList<E> extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
    
    // 链表大小
    transient int size = 0;
    
    // 头节点
    transient Node<E> first;
    
    // 尾节点
    transient Node<E> last;
    
    // 节点类
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;
    }
}
```

### 1.2 核心特点
- 基于双向链表实现
- 实现了List和Deque接口
- 允许存储null元素
- 非线程安全

## 2. 源码分析

### 2.1 节点操作
```java
// 添加节点到末尾
void linkLast(E e) {
    final Node<E> l = last;
    final Node<E> newNode = new Node<>(l, e, null);
    last = newNode;
    if (l == null)
        first = newNode;
    else
        l.next = newNode;
    size++;
}

// 删除节点
E unlink(Node<E> x) {
    final E element = x.item;
    final Node<E> next = x.next;
    final Node<E> prev = x.prev;
    
    if (prev == null) {
        first = next;
    } else {
        prev.next = next;
        x.prev = null;
    }
    
    if (next == null) {
        last = prev;
    } else {
        next.prev = prev;
        x.next = null;
    }
    
    x.item = null;
    size--;
    return element;
}
```

### 2.2 查找操作
```java
// 根据索引查找节点
Node<E> node(int index) {
    if (index < (size >> 1)) {
        Node<E> x = first;
        for (int i = 0; i < index; i++)
            x = x.next;
        return x;
    } else {
        Node<E> x = last;
        for (int i = size - 1; i > index; i--)
            x = x.prev;
        return x;
    }
}
```

## 3. 版本演进

### 3.1 JDK 9 改进
- 优化了序列化性能
- 改进了迭代器实现
```java
private void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    s.writeInt(size);
    for (Node<E> x = first; x != null; x = x.next)
        s.writeObject(x.item);
}
```

### 3.2 JDK 11+ 优化
- 改进了并行操作支持
- 优化了内存占用
```java
// 优化的spliterator实现
public Spliterator<E> spliterator() {
    return new LLSpliterator<>(this, -1, 0);
}
```

## 4. 性能优化

### 4.1 插入/删除操作
```java
// 高效的头部操作
linkedList.addFirst(element);
linkedList.removeFirst();

// 高效的尾部操作
linkedList.addLast(element);
linkedList.removeLast();
```

### 4.2 遍历优化
```java
// 1. 使用迭代器（推荐）
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String item = it.next();
}

// 2. 使用for-each
for (String item : list) {
    // 处理item
}

// 3. 避免使用索引访问
// 不推荐
for (int i = 0; i < list.size(); i++) {
    String item = list.get(i); // O(n)复杂度
}
```

## 5. 最佳实践

### 5.1 队列操作
```java
// 作为队列使用
LinkedList<String> queue = new LinkedList<>();
queue.offer("first");
queue.offer("second");
String first = queue.poll();

// 作为栈使用
LinkedList<String> stack = new LinkedList<>();
stack.push("first");
stack.push("second");
String top = stack.pop();
```

### 5.2 线程安全
```java
// 同步包装
List<String> syncList = Collections.synchronizedList(new LinkedList<>());

// 使用并发集合
Queue<String> concurrentQueue = new ConcurrentLinkedQueue<>();
```

### 5.3 内存优化
```java
// 及时清理不用的元素
list.clear();

// 使用适当的集合大小
// 如果需要频繁随机访问，考虑使用ArrayList
```

## 6. 常见陷阱

### 6.1 随机访问
```java
// 错误示例：频繁随机访问
LinkedList<String> list = new LinkedList<>();
// ... 添加大量元素
for (int i = 0; i < list.size(); i++) {
    String item = list.get(i); // 性能很差
}

// 正确示例：使用迭代器
for (String item : list) {
    // 处理item
}
```

### 6.2 并发修改
```java
// 错误示例
for (String item : list) {
    if (condition) {
        list.remove(item); // 可能抛出ConcurrentModificationException
    }
}

// 正确示例
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String item = it.next();
    if (condition) {
        it.remove();
    }
}
```

## 7. 面试要点

### 7.1 LinkedList vs ArrayList
1. 内部实现
   - LinkedList: 双向链表
   - ArrayList: 动态数组

2. 性能特点
   - 随机访问: LinkedList O(n) vs ArrayList O(1)
   - 插入删除: LinkedList O(1) vs ArrayList O(n)
   - 内存占用: LinkedList较大（需要存储前后指针）

### 7.2 应用场景
- 频繁插入删除：选择LinkedList
- 频繁随机访问：选择ArrayList
- 需要队列/栈操作：选择LinkedList

### 7.3 时间复杂度
- 访问元素：O(n)
- 头尾操作：O(1)
- 中间插入/删除：O(1)（不考虑查找时间）
- 查找元素：O(n)

### 7.4 特殊功能
- 实现了Deque接口，可以作为双端队列
- 支持null元素
- 可以作为栈或队列使用
