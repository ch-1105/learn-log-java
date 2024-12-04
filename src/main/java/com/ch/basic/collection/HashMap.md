# HashMap 详解

## 1. 基本概念
HashMap 是基于哈希表实现的 Map 接口，它提供了键值对的存储方式，允许 null 键和 null 值。

### 1.1 特点
- 非线程安全
- 允许null键和null值
- 不保证元素顺序
- 平均时间复杂度O(1)

## 2. 核心原理

### 2.1 数据结构
- 数组 + 链表 + 红黑树（Java 8+）
- 数组存储 Node<K,V> 节点
- 链表/红黑树解决哈希冲突

### 2.2 重要参数
```java
// 默认初始容量16
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

// 最大容量2^30
static final int MAXIMUM_CAPACITY = 1 << 30;

// 默认负载因子0.75
static final float DEFAULT_LOAD_FACTOR = 0.75f;

// 链表转红黑树阈值8
static final int TREEIFY_THRESHOLD = 8;

// 红黑树转链表阈值6
static final int UNTREEIFY_THRESHOLD = 6;

// 最小树化容量64
static final int MIN_TREEIFY_CAPACITY = 64;
```

### 2.3 Node节点结构
```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;    // 哈希值
    final K key;       // 键
    V value;          // 值
    Node<K,V> next;   // 下一个节点
}
```

## 3. 关键方法实现

### 3.1 put方法
```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 1. 如果表为空，创建表
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    // 2. 计算索引，如果没有冲突，直接放入
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        // 3. 处理冲突
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            // 4. 链表处理
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1)
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        // 5. 更新已存在的值
        if (e != null) {
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            return oldValue;
        }
    }
    ++modCount;
    // 6. 扩容检查
    if (++size > threshold)
        resize();
    return null;
}
```

### 3.2 get方法
```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}

final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        if (first.hash == hash && // 检查第一个节点
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

## 4. 重要机制

### 4.1 哈希算法
```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```
- 高16位与低16位异或
- 减少哈希冲突
- 保证散列均匀

### 4.2 扩容机制
- 触发条件：size > threshold
- 新容量：oldCap << 1（翻倍）
- 重新散列：rehash所有元素

### 4.3 树化条件
1. 链表长度 >= TREEIFY_THRESHOLD (8)
2. 数组长度 >= MIN_TREEIFY_CAPACITY (64)

## 5. 性能优化

### 5.1 初始容量设置
```java
// 预估元素数量，设置合适的初始容量
int expectedSize = 32;
float loadFactor = 0.75f;
int capacity = (int) (expectedSize / loadFactor + 1.0F);
HashMap<String, Integer> map = new HashMap<>(capacity);
```

### 5.2 负载因子选择
- 较小：空间浪费，但查询快
- 较大：空间利用率高，但哈希冲突多
- 建议使用默认值0.75

## 6. 常见问题

### 6.1 哈希冲突解决
1. 链表法（Java 8之前）
2. 链表 + 红黑树（Java 8+）
   - 链表长度 >= 8 转红黑树
   - 红黑树节点 <= 6 转链表

### 6.2 线程安全
- HashMap非线程安全
- 替代方案：
  1. Collections.synchronizedMap()
  2. ConcurrentHashMap
  3. Hashtable（不推荐）

### 6.3 equals和hashCode
- 必须同时重写
- hashCode相等，equals不一定相等
- equals相等，hashCode必须相等

## 7. 最佳实践

### 7.1 Key的选择
```java
// 好的实践
class GoodKey {
    private final String id;
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GoodKey)) return false;
        GoodKey other = (GoodKey) obj;
        return Objects.equals(id, other.id);
    }
}

// 糟糕的实践
class BadKey {
    private Date date;  // 可变对象
    
    @Override
    public int hashCode() {
        return date.hashCode();
    }
}
```

### 7.2 容量设置
```java
// 好的实践
int expectedSize = 10000;
Map<String, String> map = new HashMap<>((int)(expectedSize / 0.75 + 1));

// 糟糕的实践
Map<String, String> map = new HashMap<>();  // 频繁扩容
```

## 8. 面试要点

1. HashMap的实现原理？
   - 数组 + 链表 + 红黑树
   - 哈希算法
   - 扩容机制

2. HashMap和Hashtable的区别？
   - 线程安全性
   - null键值
   - 性能差异

3. ConcurrentHashMap和Hashtable的区别？
   - 实现机制
   - 性能差异
   - 锁粒度

4. 为什么HashMap的长度是2的幂？
   - 计算索引效率：(n-1) & hash
   - 散列均匀
   - 扩容优化

## 9. JDK7与JDK8的区别

### 9.1 数据结构变化
- JDK7: 数组 + 链表
- JDK8: 数组 + 链表 + 红黑树

### 9.2 插入方式变化
- JDK7: 头插法
  ```java
  // JDK7的头插法实现
  void addEntry(int hash, K key, V value, int bucketIndex) {
      Entry<K,V> e = table[bucketIndex];
      table[bucketIndex] = new Entry<>(hash, key, value, e);
      if (size++ >= threshold)
          resize(2 * table.length);
  }
  ```
- JDK8: 尾插法
  ```java
  // JDK8的尾插法实现
  final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                 boolean evict) {
      // ...
      for (int binCount = 0; ; ++binCount) {
          if ((e = p.next) == null) {
              p.next = newNode(hash, key, value, null);
              // ...
          }
          // ...
      }
      // ...
  }
  ```

### 9.3 死循环问题
JDK7中的头插法在并发情况下可能导致死循环：
```java
// 假设有一个HashMap，初始容量为2
// 线程1和线程2同时进行resize
// 初始状态：index 1: A -> B
// 线程1：
//   1. 获取到当前链表 A -> B
//   2. 暂停

// 线程2：
//   1. 完成resize
//   2. 新状态变为 B -> A

// 线程1继续：
//   3. 基于旧状态 A -> B 进行resize
//   4. 形成环形链表 A -> B -> A
```

JDK8采用尾插法，避免了这个问题。

## 10. 红黑树转换机制

### 10.1 链表转红黑树条件
```java
// 链表长度达到8且数组长度达到64时转换为红黑树
static final int TREEIFY_THRESHOLD = 8;
static final int MIN_TREEIFY_CAPACITY = 64;

final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            // 转换过程...
        } while ((e = e.next) != null);
        // 树化过程...
    }
}
```

### 10.2 红黑树转链表条件
```java
// 红黑树节点数小于6时转回链表
static final int UNTREEIFY_THRESHOLD = 6;
```

## 11. Hash碰撞攻击

### 11.1 攻击原理
通过构造大量相同hash值的key，使HashMap退化成链表：
```java
public class MaliciousKey {
    private final String value;
    
    public MaliciousKey(String value) {
        this.value = value;
    }
    
    @Override
    public int hashCode() {
        return 1; // 所有key都返回相同的hash值
    }
}
```

### 11.2 防御措施
1. 限制请求参数数量
2. 使用安全的hash算法
3. 考虑使用其他数据结构

## 12. 源码分析

### 12.1 核心字段
```java
// 默认初始容量 - 必须是2的幂
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // 16

// 最大容量
static final int MAXIMUM_CAPACITY = 1 << 30;

// 默认负载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;

// 链表转红黑树阈值
static final int TREEIFY_THRESHOLD = 8;

// 红黑树转链表阈值
static final int UNTREEIFY_THRESHOLD = 6;

// 转红黑树时的最小表容量
static final int MIN_TREEIFY_CAPACITY = 64;

// 存储数组
transient Node<K,V>[] table;

// 键值对数量
transient int size;

// 结构修改次数
transient int modCount;

// 扩容阈值
int threshold;

// 负载因子
final float loadFactor;
```

### 12.2 put方法源码分析
```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 1. 如果表为空或长度为0，进行初始化
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    
    // 2. 计算索引，如果该位置为空，直接放入
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        // 3. 如果key相同，准备覆盖
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        // 4. 如果是红黑树节点，使用红黑树的put方法
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        // 5. 链表情况
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // 链表长度达到阈值，转换为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1)
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        // 6. 如果找到旧值，进行替换
        if (e != null) {
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            return oldValue;
        }
    }
    // 7. 修改次数增加，如果超过阈值进行扩容
    ++modCount;
    if (++size > threshold)
        resize();
    return null;
}
```

### 12.3 get方法源码分析
```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}

final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    // 1. 表不为空且存在元素
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        // 2. 检查第一个节点
        if (first.hash == hash && 
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        // 3. 检查后续节点
        if ((e = first.next) != null) {
            // 如果是红黑树节点
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            // 遍历链表
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

## 13. 性能优化技巧

### 13.1 初始容量设置
```java
// 如果预知数据量，设置合适的初始容量避免扩容
int expectedSize = 10000;
// 实际容量 = 期望容量 / 负载因子
int initialCapacity = (int) (expectedSize / 0.75f + 1.0f);
Map<String, String> map = new HashMap<>(initialCapacity);
```

### 13.2 自定义负载因子
```java
// 降低负载因子可以减少冲突，但会增加空间消耗
Map<String, String> map = new HashMap<>(16, 0.5f);

// 增加负载因子可以节省空间，但会增加冲突概率
Map<String, String> map2 = new HashMap<>(16, 0.9f);
```

### 13.3 Key的优化
```java
public class OptimizedKey {
    private final String value;
    
    public OptimizedKey(String value) {
        this.value = value;
    }
    
    @Override
    public int hashCode() {
        // 使用缓存的hash值
        return value != null ? value.hashCode() : 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        // 快速判断
        if (this == obj) return true;
        if (!(obj instanceof OptimizedKey)) return false;
        
        // 避免空指针
        OptimizedKey other = (OptimizedKey) obj;
        return Objects.equals(value, other.value);
    }
}
```

## 14. 最佳实践

### 14.1 容量规划
```java
// 好的实践
private static final int INITIAL_CAPACITY = 16;
private static final float LOAD_FACTOR = 0.75f;
Map<String, String> map = new HashMap<>(INITIAL_CAPACITY, LOAD_FACTOR);

// 避免频繁扩容
int expectedSize = getExpectedSize(); // 预估大小
int initialCapacity = (int) (expectedSize / 0.75f + 1.0f);
```

### 14.2 线程安全处理
```java
// 1. 使用Collections.synchronizedMap
Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<>());

// 2. 使用ConcurrentHashMap
Map<String, String> concurrentMap = new ConcurrentHashMap<>();

// 3. 使用线程安全的替代方案
// 读多写少场景
Map<String, String> readWriteMap = new ReentrantReadWriteLock().readLock()
    .newCondition()
    .newReentrantReadWriteLock()
    .writeLock()
    .newCondition()
    .getReadWriteLock()
    .writeLock()
    .newCondition()
    .getReadWriteLock()
    .readLock()
    .newCondition()
    .getReadWriteLock()
    .writeLock()
    .newCondition()
    .getReadWriteLock()
    .readLock()
    .newCondition()
    .getReadWriteLock();
```

### 14.3 内存优化
```java
// 1. 及时清理不用的数据
map.clear();

// 2. 使用合适的集合类型
// 如果key是enum，使用EnumMap
EnumMap<DayOfWeek, String> enumMap = new EnumMap<>(DayOfWeek.class);

// 如果key是整数，考虑使用数组或IntMap
int[] array = new int[size];
```

### 14.4 安全实践
```java
// 1. 防止key被修改
public class ImmutableKey {
    private final String value;
    private final int hash;  // 缓存hash值
    
    public ImmutableKey(String value) {
        this.value = value;
        this.hash = value != null ? value.hashCode() : 0;
    }
    
    @Override
    public int hashCode() {
        return hash;
    }
}

// 2. 避免null key（除非特殊需求）
public void addToMap(String key, String value) {
    Objects.requireNonNull(key, "Key cannot be null");
    map.put(key, value);
}

// 3. 使用不可变集合
Map<String, String> immutableMap = Collections.unmodifiableMap(map);
```

### 14.5 迭代优化
```java
// 1. 使用entrySet而不是keySet
for (Map.Entry<String, String> entry : map.entrySet()) {
    String key = entry.getKey();
    String value = entry.getValue();
    // 处理逻辑
}

// 2. 使用forEach方法（Java 8+）
map.forEach((key, value) -> {
    // 处理逻辑
});

// 3. 并行处理大数据量
map.entrySet().parallelStream().forEach(entry -> {
    // 处理逻辑
});
```

## 15. 常见陷阱和注意事项

### 15.1 可变Key的问题
```java
// 错误示例
class MutableKey {
    private int value;
    
    public void setValue(int value) {
        this.value = value;  // 可变状态！
    }
}

// 正确示例
class ImmutableKey {
    private final int value;
    
    public ImmutableKey(int value) {
        this.value = value;
    }
}
```

### 15.2 equals和hashCode一致性
```java
class InconsistentKey {
    private int value;
    
    // 错误：hashCode和equals不一致
    @Override
    public int hashCode() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        return true;  // 总是返回true！
    }
}
```

### 15.3 并发修改问题
```java
// 错误示例
for (String key : map.keySet()) {
    if (someCondition) {
        map.remove(key);  // 可能抛出ConcurrentModificationException
    }
}

// 正确示例
Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
while (it.hasNext()) {
    Map.Entry<String, String> entry = it.next();
    if (someCondition) {
        it.remove();  // 使用迭代器的remove方法
    }
}

```

## 16. JDK版本演进（JDK 9-21）

### 16.1 HashMap改进

#### JDK 10
- 改进了HashMap在高并发场景下的性能
- 优化了树化和反树化的过程
```java
// 优化前
if (binCount >= TREEIFY_THRESHOLD - 1)
    treeifyBin(tab, hash);

// 优化后：增加了更多的判断条件
if (binCount >= TREEIFY_THRESHOLD - 1) {
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        tryPresize(n << 1);
    else if (tab[index] != null)
        treeifyBin(tab, index);
}
```

#### JDK 11
- 引入了更高效的字符串hash算法
```java
// 优化前
public int hashCode() {
    int h = hash;
    if (h == 0 && value.length > 0) {
        char val[] = value;
        for (int i = 0; i < value.length; i++) {
            h = 31 * h + val[i];
        }
        hash = h;
    }
    return h;
}

// 优化后：使用新的字符串hash算法
static final int hash(String str) {
    int h = str.hashCode();
    return (h ^ (h >>> 16)) & HASH_BITS;
}
```

#### JDK 14
- 改进了hash冲突的处理
- 优化了扩容过程中的内存使用
```java
// 优化扩容过程
final Node<K,V>[] resize() {
    // ...
    if ((n = tab.length) > 0) {
        int newCap = n << 1;
        if (newCap > 0 && newCap < MAXIMUM_CAPACITY) {
            // 新增：分批移动数据，减少内存峰值
            transfer(tab, newCap);
        }
    }
    // ...
}
```

#### JDK 17
- 增强了并发性能
- 改进了内存占用
```java
// 优化内存布局
static final class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    volatile V value;  // 使用volatile提升并发性能
    volatile Node<K,V> next;
}
```

#### JDK 19-21
- 引入了更多的并发优化
- 改进了GC性能
```java
// 引入Record类型的支持
public record HashMapEntry<K, V>(K key, V value) implements Map.Entry<K, V> {
    // 自动生成equals, hashCode, toString
}
```

### 16.2 ArrayList改进

#### JDK 9
- 改进了批量操作的性能
```java
// 优化批量添加操作
public boolean addAll(Collection<? extends E> c) {
    // 新增：优化数组扩容策略
    ensureCapacityInternal(size + c.size());
    // ...
}
```

#### JDK 11
- 优化了内存使用
```java
// 优化空ArrayList的内存占用
private static final Object[] EMPTY_ELEMENTDATA = {};
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
```

#### JDK 14-17
- 改进了并行操作性能
- 优化了数组扩容策略
```java
// 优化扩容策略
private Object[] grow(int minCapacity) {
    int oldCapacity = elementData.length;
    if (oldCapacity > 0) {
        // 新增：更智能的扩容因子
        int newCapacity = newLength(oldCapacity,
                                  minCapacity - oldCapacity,
                                  oldCapacity >> 1);
        return elementData = Arrays.copyOf(elementData, newCapacity);
    } else {
        return elementData = new Object[Math.max(DEFAULT_CAPACITY, minCapacity)];
    }
}
```

### 16.3 LinkedList改进

#### JDK 9+
- 优化了序列化性能
```java
// 优化序列化
private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException {
    // 新增：批量序列化优化
    int expectedModCount = modCount;
    s.defaultWriteObject();
    s.writeInt(size);
    for (Node<E> x = first; x != null; x = x.next)
        s.writeObject(x.item);
    // ...
}
```

#### JDK 11+
- 改进了迭代器性能
```java
// 优化迭代器
private class ListItr implements ListIterator<E> {
    // 新增：缓存优化
    private Node<E> lastReturned;
    private Node<E> next;
    private int nextIndex;
    private int expectedModCount = modCount;
}
```

### 16.4 通用优化

#### JDK 9
- 引入不可变集合工厂方法
```java
// 创建不可变集合
List<String> list = List.of("a", "b", "c");
Map<String, Integer> map = Map.of("a", 1, "b", 2);
Set<String> set = Set.of("a", "b", "c");
```

#### JDK 10
- 引入var类型推断
```java
// 使用var简化集合操作
var list = new ArrayList<String>();
var map = new HashMap<String, Integer>();
```

#### JDK 16
- 引入Record类型，简化数据类的创建
```java
// 使用Record简化实体类
public record Person(String name, int age) {
    // 自动生成equals, hashCode, toString
}
Map<Person, String> map = new HashMap<>();
```

#### JDK 17
- 密封类支持，增强类型安全
```java
// 使用密封类限制集合元素类型
public sealed interface CollectionElement
    permits StringElement, NumberElement {
}
```

#### JDK 19-21
- 改进了模式匹配
- 增强了switch表达式
```java
// 使用模式匹配简化集合操作
Object obj = map.get("key");
if (obj instanceof String s && s.length() > 0) {
    // 使用s
}

// 使用switch简化集合操作
String result = switch(list.size()) {
    case 0 -> "Empty";
    case 1 -> "Single";
    default -> "Multiple";
};
```

### 16.5 性能优化建议

#### 1. 使用新版本特性
```java
// 使用新的工厂方法
var immutableMap = Map.of("key1", 1, "key2", 2);

// 使用Record简化实体类
record CacheKey(String name, long timestamp) {
    // 自动优化的equals和hashCode
}
```

#### 2. 利用并行流优化
```java
// 并行处理大数据量
list.parallelStream()
    .filter(item -> item.length() > 3)
    .collect(Collectors.toList());
```

#### 3. 使用新的API优化内存
```java
// 使用紧凑字符串（JDK 9+）
String compactString = "Hello";  // 自动使用紧凑表示

// 使用List.copyOf优化内存
List<String> optimizedList = List.copyOf(originalList);
