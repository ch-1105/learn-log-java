# HashSet 详解

## 1. 基本原理

### 1.1 数据结构
```java
public class HashSet<E> extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable {
    
    // 内部使用HashMap存储元素
    private transient HashMap<E,Object> map;
    
    // 用于存储HashMap的value
    private static final Object PRESENT = new Object();
}
```

### 1.2 核心特点
- 基于HashMap实现
- 不允许重复元素
- 允许null元素
- 无序集合
- 非线程安全

## 2. 源码分析

### 2.1 构造方法
```java
// 默认构造
public HashSet() {
    map = new HashMap<>();
}

// 指定初始容量
public HashSet(int initialCapacity) {
    map = new HashMap<>(initialCapacity);
}

// 指定初始容量和负载因子
public HashSet(int initialCapacity, float loadFactor) {
    map = new HashMap<>(initialCapacity, loadFactor);
}
```

### 2.2 添加元素
```java
public boolean add(E e) {
    return map.put(e, PRESENT) == null;
}

public boolean remove(Object o) {
    return map.remove(o) == PRESENT;
}
```

### 2.3 查找元素
```java
public boolean contains(Object o) {
    return map.containsKey(o);
}

public Iterator<E> iterator() {
    return map.keySet().iterator();
}
```

## 3. 版本演进

### 3.1 JDK 9 改进
- 引入不可变集合工厂方法
```java
Set<String> set = Set.of("a", "b", "c");
```

### 3.2 JDK 10+ 优化
- 改进了hash冲突处理
- 优化了序列化性能
```java
// 优化的序列化
private void writeObject(java.io.ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    s.writeInt(map.size());
    for (E e : map.keySet())
        s.writeObject(e);
}
```

## 4. 性能优化

### 4.1 初始容量设置
```java
// 预知元素数量时，指定初始容量
Set<String> set = new HashSet<>(1000);

// 设置合适的负载因子
Set<String> set = new HashSet<>(1000, 0.75f);
```

### 4.2 批量操作
```java
// 使用addAll而不是循环add
Set<String> set = new HashSet<>();
set.addAll(Arrays.asList("a", "b", "c"));

// 使用removeAll批量删除
set.removeAll(Arrays.asList("a", "b"));
```

## 5. 最佳实践

### 5.1 重写equals和hashCode
```java
public class Person {
    private String name;
    private int age;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return age == person.age && Objects.equals(name, person.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
```

### 5.2 线程安全
```java
// 同步包装
Set<String> syncSet = Collections.synchronizedSet(new HashSet<>());

// 使用ConcurrentHashMap
Set<String> concurrentSet = ConcurrentHashMap.newKeySet();
```

### 5.3 性能优化
```java
// 1. 合理设置初始容量
int expectedSize = getExpectedSize();
Set<String> set = new HashSet<>(expectedSize);

// 2. 使用removeIf进行条件删除
set.removeIf(item -> item.length() > 3);

// 3. 使用Stream进行并行操作
set.parallelStream()
   .filter(item -> item.length() > 3)
   .collect(Collectors.toSet());
```

## 6. 常见陷阱

### 6.1 自定义对象作为元素
```java
// 错误示例：未重写equals和hashCode
class User {
    String name;
    int age;
}

// 正确示例：重写equals和hashCode
class User {
    String name;
    int age;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return age == user.age && Objects.equals(name, user.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
```

### 6.2 并发修改
```java
// 错误示例
for (String item : set) {
    if (condition) {
        set.remove(item); // 可能抛出ConcurrentModificationException
    }
}

// 正确示例
Iterator<String> it = set.iterator();
while (it.hasNext()) {
    String item = it.next();
    if (condition) {
        it.remove();
    }
}
```

## 7. 面试要点

### 7.1 HashSet vs HashMap
- HashSet是基于HashMap实现的
- HashSet只存储元素，HashMap存储键值对
- HashSet不允许重复，HashMap允许value重复

### 7.2 HashSet vs TreeSet
- HashSet：无序，O(1)的添加和查找
- TreeSet：有序，O(log n)的添加和查找
- TreeSet基于红黑树实现

### 7.3 时间复杂度
- 添加：O(1)
- 删除：O(1)
- 查找：O(1)
- 遍历：O(n)

### 7.4 特性总结
- 不保证元素顺序
- 允许null元素
- 非线程安全
- 依赖equals和hashCode方法
