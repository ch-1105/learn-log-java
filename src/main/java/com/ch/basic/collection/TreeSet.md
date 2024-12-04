# TreeSet 详解

## 1. 基本原理

### 1.1 数据结构
```java
public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable {
    
    // 内部使用NavigableMap（实际是TreeMap）存储元素
    private transient NavigableMap<E,Object> m;
    
    // 用于存储Map的value
    private static final Object PRESENT = new Object();
}
```

### 1.2 核心特点
- 基于红黑树实现
- 有序集合（自然顺序或自定义顺序）
- 不允许重复元素
- 不允许null元素
- 非线程安全
- 支持范围操作

## 2. 源码分析

### 2.1 构造方法
```java
// 默认构造，使用自然顺序
public TreeSet() {
    this(new TreeMap<>());
}

// 使用自定义比较器
public TreeSet(Comparator<? super E> comparator) {
    this(new TreeMap<>(comparator));
}

// 使用已有的NavigableMap
TreeSet(NavigableMap<E,Object> m) {
    this.m = m;
}
```

### 2.2 添加和删除元素
```java
public boolean add(E e) {
    return m.put(e, PRESENT) == null;
}

public boolean remove(Object o) {
    return m.remove(o) == PRESENT;
}
```

### 2.3 导航方法
```java
// 获取第一个元素
public E first() {
    return m.firstKey();
}

// 获取最后一个元素
public E last() {
    return m.lastKey();
}

// 获取小于给定元素的最大元素
public E lower(E e) {
    return m.lowerKey(e);
}

// 获取大于给定元素的最小元素
public E higher(E e) {
    return m.higherKey(e);
}
```

## 3. 版本演进

### 3.1 JDK 9 改进
- 引入不可变集合工厂方法
```java
NavigableSet<String> set = Collections.unmodifiableNavigableSet(
    new TreeSet<>(Arrays.asList("a", "b", "c"))
);
```

### 3.2 JDK 10+ 优化
- 改进了红黑树平衡算法
- 优化了序列化性能
```java
// 优化的序列化实现
private void writeObject(java.io.ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    s.writeObject(m.comparator());
    s.writeInt(m.size());
    for (E e : m.keySet())
        s.writeObject(e);
}
```

## 4. 性能优化

### 4.1 比较器优化
```java
// 自定义比较器，避免不必要的对象创建
TreeSet<Person> set = new TreeSet<>((p1, p2) -> {
    int result = p1.getName().compareTo(p2.getName());
    return result != 0 ? result : Integer.compare(p1.getAge(), p2.getAge());
});
```

### 4.2 范围操作优化
```java
// 使用subSet代替手动过滤
TreeSet<Integer> set = new TreeSet<>();
// ... 添加元素
SortedSet<Integer> subset = set.subSet(10, 20); // [10, 20)范围的元素
```

## 5. 最佳实践

### 5.1 自定义对象排序
```java
class Person implements Comparable<Person> {
    private String name;
    private int age;
    
    @Override
    public int compareTo(Person other) {
        int result = this.name.compareTo(other.name);
        return result != 0 ? result : Integer.compare(this.age, other.age);
    }
}
```

### 5.2 范围操作
```java
TreeSet<Integer> set = new TreeSet<>();
// ... 添加元素

// 获取指定范围的元素
SortedSet<Integer> range1 = set.subSet(10, 20);     // [10, 20)
SortedSet<Integer> range2 = set.headSet(20);        // < 20
SortedSet<Integer> range3 = set.tailSet(10);        // >= 10
NavigableSet<Integer> range4 = set.descendingSet(); // 降序视图
```

### 5.3 线程安全
```java
// 同步包装
TreeSet<String> set = new TreeSet<>();
Set<String> syncSet = Collections.synchronizedSet(set);

// 使用锁保护
Lock lock = new ReentrantLock();
TreeSet<String> set = new TreeSet<>();
// 在访问set时获取锁
```

## 6. 常见陷阱

### 6.1 可比较性问题
```java
// 错误示例：元素没有实现Comparable接口
class User {
    String name;
}
TreeSet<User> set = new TreeSet<>(); // 运行时异常

// 正确示例1：实现Comparable接口
class User implements Comparable<User> {
    String name;
    
    @Override
    public int compareTo(User other) {
        return this.name.compareTo(other.name);
    }
}

// 正确示例2：提供Comparator
TreeSet<User> set = new TreeSet<>((u1, u2) -> 
    u1.name.compareTo(u2.name));
```

### 6.2 修改可变对象
```java
// 危险操作：修改已在TreeSet中的可变对象
class MutablePerson implements Comparable<MutablePerson> {
    private String name;
    
    public void setName(String name) {
        this.name = name; // 可能破坏TreeSet的有序性
    }
    
    @Override
    public int compareTo(MutablePerson other) {
        return this.name.compareTo(other.name);
    }
}
```

## 7. 面试要点

### 7.1 TreeSet vs HashSet
1. 实现方式
   - TreeSet：红黑树
   - HashSet：哈希表

2. 有序性
   - TreeSet：有序
   - HashSet：无序

3. 性能比较
   - TreeSet：O(log n)的增删改查
   - HashSet：O(1)的增删改查

### 7.2 红黑树特性
1. 每个节点是红色或黑色
2. 根节点是黑色
3. 叶子节点（NIL）是黑色
4. 红色节点的子节点必须是黑色
5. 从根到叶子的所有路径包含相同数量的黑色节点

### 7.3 应用场景
- 需要有序集合时
- 需要范围操作时
- 需要获取最大最小值时
- 需要获取前驱后继元素时

### 7.4 注意事项
- 元素必须可比较
- 比较逻辑要稳定
- 避免修改可变元素
- 注意并发安全
