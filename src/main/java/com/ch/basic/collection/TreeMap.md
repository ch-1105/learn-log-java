# TreeMap 详解

## 1. 基本原理

### 1.1 数据结构
```java
public class TreeMap<K,V> extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable {
    
    // 比较器
    private final Comparator<? super K> comparator;
    
    // 根节点
    private transient Entry<K,V> root;
    
    // 元素个数
    private transient int size = 0;
    
    // 红黑树节点
    static final class Entry<K,V> implements Map.Entry<K,V> {
        K key;
        V value;
        Entry<K,V> left;
        Entry<K,V> right;
        Entry<K,V> parent;
        boolean color = BLACK;
    }
}
```

### 1.2 核心特点
- 基于红黑树实现
- 有序Map（按键的自然顺序或自定义顺序）
- 不允许null键（但允许null值）
- 非线程安全
- 支持范围操作和导航方法
- 保证log(n)的时间复杂度

## 2. 源码分析

### 2.1 红黑树操作
```java
// 插入操作
private void fixAfterInsertion(Entry<K,V> x) {
    x.color = RED;
    while (x != null && x != root && x.parent.color == RED) {
        if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
            Entry<K,V> y = rightOf(parentOf(parentOf(x)));
            if (colorOf(y) == RED) {
                setColor(parentOf(x), BLACK);
                setColor(y, BLACK);
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x));
            } else {
                if (x == rightOf(parentOf(x))) {
                    x = parentOf(x);
                    rotateLeft(x);
                }
                setColor(parentOf(x), BLACK);
                setColor(parentOf(parentOf(x)), RED);
                rotateRight(parentOf(parentOf(x)));
            }
        } else {
            // 对称操作
        }
    }
    root.color = BLACK;
}
```

### 2.2 导航方法
```java
// 获取最小键
public K firstKey() {
    return key(getFirstEntry());
}

// 获取最大键
public K lastKey() {
    return key(getLastEntry());
}

// 获取小于给定键的最大键
public K lowerKey(K key) {
    return keyOrNull(getLowerEntry(key));
}

// 获取大于给定键的最小键
public K higherKey(K key) {
    return keyOrNull(getHigherEntry(key));
}
```

## 3. 版本演进

### 3.1 JDK 9 改进
- 优化了红黑树平衡算法
- 改进了序列化性能
```java
// 优化的序列化
private void writeObject(java.io.ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    s.writeInt(size);
    for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e)) {
        s.writeObject(e.key);
        s.writeObject(e.value);
    }
}
```

### 3.2 JDK 10+ 优化
- 改进了并发性能
- 优化了内存占用
```java
// 优化的节点结构
static final class Entry<K,V> implements Map.Entry<K,V> {
    K key;
    V value;
    Entry<K,V> left;
    Entry<K,V> right;
    Entry<K,V> parent;
    boolean color;
}
```

## 4. 性能优化

### 4.1 比较器优化
```java
// 自定义比较器，避免不必要的对象创建
TreeMap<Person, Data> map = new TreeMap<>((p1, p2) -> {
    int result = p1.getName().compareTo(p2.getName());
    return result != 0 ? result : Integer.compare(p1.getAge(), p2.getAge());
});
```

### 4.2 范围操作优化
```java
// 使用subMap代替手动过滤
TreeMap<Integer, String> map = new TreeMap<>();
// ... 添加元素
SortedMap<Integer, String> subMap = map.subMap(10, 20); // [10, 20)范围的元素
```

## 5. 最佳实践

### 5.1 自定义对象作为键
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
TreeMap<Integer, String> map = new TreeMap<>();
// ... 添加元素

// 获取指定范围的元素
SortedMap<Integer, String> range1 = map.subMap(10, 20);     // [10, 20)
SortedMap<Integer, String> range2 = map.headMap(20);        // < 20
SortedMap<Integer, String> range3 = map.tailMap(10);        // >= 10
NavigableMap<Integer, String> range4 = map.descendingMap(); // 降序视图
```

### 5.3 线程安全
```java
// 同步包装
TreeMap<String, Integer> map = new TreeMap<>();
Map<String, Integer> syncMap = Collections.synchronizedMap(map);

// 使用锁保护
Lock lock = new ReentrantLock();
TreeMap<String, Integer> map = new TreeMap<>();
// 在访问map时获取锁
```

## 6. 常见陷阱

### 6.1 可比较性问题
```java
// 错误示例：键没有实现Comparable接口
class User {
    String name;
}
TreeMap<User, Data> map = new TreeMap<>(); // 运行时异常

// 正确示例1：实现Comparable接口
class User implements Comparable<User> {
    String name;
    
    @Override
    public int compareTo(User other) {
        return this.name.compareTo(other.name);
    }
}

// 正确示例2：提供Comparator
TreeMap<User, Data> map = new TreeMap<>((u1, u2) -> 
    u1.name.compareTo(u2.name));
```

### 6.2 修改可变键
```java
// 危险操作：修改已在TreeMap中的可变键
class MutableKey implements Comparable<MutableKey> {
    private String value;
    
    public void setValue(String value) {
        this.value = value; // 可能破坏TreeMap的有序性
    }
    
    @Override
    public int compareTo(MutableKey other) {
        return this.value.compareTo(other.value);
    }
}
```

## 7. 面试要点

### 7.1 TreeMap vs HashMap
1. 实现方式
   - TreeMap：红黑树
   - HashMap：哈希表 + 红黑树（JDK 8+）

2. 有序性
   - TreeMap：有序（键的自然顺序或自定义顺序）
   - HashMap：无序

3. 性能比较
   - TreeMap：O(log n)的增删改查
   - HashMap：O(1)的增删改查（平均情况）

### 7.2 红黑树特性
1. 每个节点是红色或黑色
2. 根节点是黑色
3. 叶子节点（NIL）是黑色
4. 红色节点的子节点必须是黑色
5. 从根到叶子的所有路径包含相同数量的黑色节点

### 7.3 应用场景
- 需要按键排序的场景
- 需要范围查询的场景
- 需要获取最大最小键的场景
- 需要获取前驱后继键的场景

### 7.4 注意事项
- 键必须可比较
- 比较逻辑要稳定
- 避免修改可变键
- 注意并发安全
- null键不允许
