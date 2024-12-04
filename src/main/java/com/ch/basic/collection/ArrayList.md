# ArrayList 详解

## 1. 基本原理

### 1.1 数据结构
```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    // 默认初始容量
    private static final int DEFAULT_CAPACITY = 10;
    
    // 存储元素的数组
    transient Object[] elementData;
    
    // 实际元素个数
    private int size;
}
```

### 1.2 核心特点
- 基于动态数组实现
- 支持随机访问
- 容量动态增长
- 非线程安全

## 2. 源码分析

### 2.1 构造方法
```java
// 默认构造
public ArrayList() {
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}

// 指定初始容量
public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
        this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
        this.elementData = EMPTY_ELEMENTDATA;
    } else {
        throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
    }
}
```

### 2.2 添加元素
```java
public boolean add(E e) {
    ensureCapacityInternal(size + 1);
    elementData[size++] = e;
    return true;
}

public void add(int index, E element) {
    rangeCheckForAdd(index);
    ensureCapacityInternal(size + 1);
    System.arraycopy(elementData, index, elementData, index + 1, size - index);
    elementData[index] = element;
    size++;
}
```

### 2.3 扩容机制
```java
private void grow(int minCapacity) {
    int oldCapacity = elementData.length;
    // 扩容为原来的1.5倍
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```

## 3. 版本演进

### 3.1 JDK 9 改进
- 优化了空集合的内存占用
- 改进了批量操作性能
```java
// 优化的空集合表示
private static final Object[] EMPTY_ELEMENTDATA = {};
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
```

### 3.2 JDK 11 优化
- 引入了更智能的扩容策略
- 优化了并行操作
```java
static <T> List<T> of() {
    return ImmutableCollections.emptyList();
}

static <T> List<T> of(T e1) {
    return new ImmutableCollections.List12<>(e1);
}
```

### 3.3 JDK 14-17 改进
- 改进了数组复制性能
- 优化了迭代器实现
```java
// 优化的数组复制
static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
    @SuppressWarnings("unchecked")
    T[] copy = ((Object)newType == (Object)Object[].class)
        ? (T[]) new Object[newLength]
        : (T[]) Array.newInstance(newType.getComponentType(), newLength);
    System.arraycopy(original, 0, copy, 0,
                     Math.min(original.length, newLength));
    return copy;
}
```

## 4. 性能优化

### 4.1 初始容量设置
```java
// 预知元素数量时，指定初始容量
List<String> list = new ArrayList<>(10000);

// 批量添加时，一次性扩容
list.ensureCapacity(expectedSize);
```

### 4.2 批量操作优化
```java
// 使用addAll而不是循环add
List<String> list = new ArrayList<>();
list.addAll(Arrays.asList("a", "b", "c"));

// 使用removeAll批量删除
list.removeAll(Arrays.asList("a", "b"));
```

### 4.3 遍历优化
```java
// 1. 使用for-each（推荐）
for (String item : list) {
    // 处理item
}

// 2. 使用索引（需要索引时）
for (int i = 0; i < list.size(); i++) {
    String item = list.get(i);
}

// 3. 使用迭代器（需要在遍历时删除元素）
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String item = it.next();
    if (needRemove(item)) {
        it.remove();
    }
}
```

## 5. 最佳实践

### 5.1 线程安全
```java
// 1. 使用Collections.synchronizedList
List<String> syncList = Collections.synchronizedList(new ArrayList<>());

// 2. 使用CopyOnWriteArrayList
List<String> cowList = new CopyOnWriteArrayList<>();
```

### 5.2 内存优化
```java
// 1. 及时清理不用的元素
list.clear();

// 2. 释放多余容量
list.trimToSize();

// 3. 使用合适的初始容量
int expectedSize = getExpectedSize();
List<String> list = new ArrayList<>(expectedSize);
```

### 5.3 并发处理
```java
// 1. 安全的遍历和修改
List<String> list = new CopyOnWriteArrayList<>();
for (String item : list) {
    // 可以安全地修改list
    list.add("new item");
}

// 2. 使用Stream进行并行处理
list.parallelStream()
    .filter(item -> item.length() > 3)
    .collect(Collectors.toList());
```

## 6. 常见陷阱

### 6.1 并发修改问题
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
        it.remove(); // 使用迭代器的remove方法
    }
}
```

### 6.2 类型安全
```java
// 错误示例
ArrayList list = new ArrayList(); // raw type

// 正确示例
ArrayList<String> list = new ArrayList<>(); // 使用泛型
```

### 6.3 容量规划
```java
// 错误示例：频繁扩容
List<String> list = new ArrayList<>();
for (int i = 0; i < 10000; i++) {
    list.add("item" + i);
}

// 正确示例：预估容量
List<String> list = new ArrayList<>(10000);
for (int i = 0; i < 10000; i++) {
    list.add("item" + i);
}
```

## 7. 面试要点

### 7.1 ArrayList vs LinkedList
- ArrayList: 基于数组，随机访问快，插入删除慢
- LinkedList: 基于链表，随机访问慢，插入删除快

### 7.2 ArrayList vs Vector
- ArrayList: 非线程安全，性能好
- Vector: 线程安全，性能较差

### 7.3 扩容机制
- 默认容量：10
- 扩容倍数：1.5倍
- 触发条件：size + 1 > capacity

### 7.4 时间复杂度
- 访问：O(1)
- 插入/删除：O(n)
- 末尾添加：均摊O(1)
