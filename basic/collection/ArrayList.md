# ArrayList 详解

## 1. 基本概念
ArrayList 是 Java 集合框架中最常用的类之一，它实现了 List 接口，是一个动态数组，可以动态地增长和缩减。

### 1.1 特点
- 基于数组实现
- 支持随机访问
- 非线程安全
- 容量可动态增长

## 2. 核心源码分析

### 2.1 重要属性
```java
// 默认初始容量
private static final int DEFAULT_CAPACITY = 10;

// 存储元素的数组
transient Object[] elementData;

// ArrayList的大小（包含元素的数量）
private int size;
```

### 2.2 关键方法实现原理

#### 2.2.1 添加元素 add(E e)
```java
public boolean add(E e) {
    ensureCapacityInternal(size + 1);
    elementData[size++] = e;
    return true;
}
```
- 添加元素时会先确保容量足够
- 如果容量不足，会进行扩容（通常扩容为原来的1.5倍）

#### 2.2.2 删除元素 remove(int index)
```java
public E remove(int index) {
    rangeCheck(index);
    E oldValue = elementData(index);
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    elementData[--size] = null;
    return oldValue;
}
```
- 删除指定位置的元素
- 将删除位置后面的元素整体向前移动
- 最后一个元素位置设为null

## 3. 性能分析

### 3.1 时间复杂度
- 随机访问：O(1)
- 添加/删除（末尾）：O(1)
- 添加/删除（中间）：O(n)
- 查找元素：O(n)

### 3.2 空间复杂度
- 空间复杂度：O(n)
- 当容量不足时，会创建新数组并复制元素，导致额外的空间开销

## 4. 常见面试题

### 4.1 ArrayList 和 LinkedList 的区别？
1. 底层实现：
   - ArrayList：动态数组
   - LinkedList：双向链表

2. 性能比较：
   - 随机访问：ArrayList 优于 LinkedList
   - 中间插入删除：LinkedList 优于 ArrayList
   - 末尾添加：两者相当

### 4.2 ArrayList 是如何扩容的？
1. 当添加元素时，如果容量不足：
   - 计算新容量：原容量的1.5倍
   - 创建新数组
   - 复制原数组元素到新数组
   - 新数组替换原数组

## 5. 最佳实践

### 5.1 性能优化建议
1. 初始容量设置
   - 如果能预估数据量，建议指定初始容量
   - 避免频繁扩容带来的性能开销

2. 删除元素
   - 从后向前删除更高效
   - 批量删除时使用removeAll或clear

3. 遍历方式选择
   - 随机访问推荐使用for循环
   - 顺序访问可以使用Iterator

### 5.2 代码示例
```java
// 推荐：预设容量
ArrayList<Integer> list = new ArrayList<>(10000);

// 不推荐：频繁扩容
ArrayList<Integer> list = new ArrayList<>();
for (int i = 0; i < 10000; i++) {
    list.add(i);
}
```

## 6. 注意事项
1. 线程安全
   - ArrayList 非线程安全
   - 多线程环境下使用 Vector 或 Collections.synchronizedList()

2. 内存泄漏
   - 及时清除不用的元素
   - 使用clear()方法而不是直接置null

3. 遍历时修改
   - 避免在foreach循环中修改列表
   - 使用Iterator的remove方法删除元素
