# Java线程安全集合详解

## 1. ConcurrentHashMap

### 1.1 基本特性
- 线程安全的HashMap实现
- 支持完全并发的读取
- 支持特定数量的并发写入
- 不允许null键或null值
- 弱一致性的迭代器

### 1.2 重要概念
1. **分段锁机制（JDK 7）**
   - Segment数组
   - 每个Segment独立加锁
   - 默认16个Segment

2. **CAS + synchronized（JDK 8+）**
   - 取消Segment设计
   - 使用Node数组
   - 基于CAS和synchronized实现

### 1.3 主要优化
1. **并发度提升**
   - 锁粒度更细
   - 更好的并发性能
   - 扩容优化

2. **性能优化**
   - 红黑树优化
   - 扩容并行化
   - 更少的锁竞争

## 2. CopyOnWriteArrayList

### 2.1 基本特性
- 线程安全的ArrayList实现
- 适用于读多写少场景
- 写时复制策略
- 弱一致性的迭代器

### 2.2 实现原理
1. **写时复制**
   - 修改时复制整个数组
   - 修改新副本
   - 原子性地替换引用

2. **不变性**
   - 底层数组不可变
   - 迭代器看到的是快照
   - 无需加锁的遍历

### 2.3 使用场景
1. **事件监听器列表**
2. **配置信息列表**
3. **读多写少的缓存**

## 3. 其他常用线程安全集合

### 3.1 ConcurrentLinkedQueue
- 线程安全的无界队列
- 基于链表实现
- 适用于高并发场景
- 非阻塞算法实现

### 3.2 BlockingQueue家族
1. **ArrayBlockingQueue**
   - 有界队列
   - 基于数组实现
   - FIFO顺序

2. **LinkedBlockingQueue**
   - 可选有界队列
   - 基于链表实现
   - FIFO顺序

3. **PriorityBlockingQueue**
   - 带优先级的阻塞队列
   - 自然顺序或比较器排序
   - 不允许null元素

### 3.3 ConcurrentSkipListMap/Set
- 线程安全的有序集合
- 基于跳表实现
- 适用于需要排序的并发场景

## 4. 性能对比

### 4.1 ConcurrentHashMap vs Hashtable
1. **并发度**
   - ConcurrentHashMap：分段锁/CAS
   - Hashtable：整表锁

2. **性能**
   - ConcurrentHashMap：更好的并发性能
   - Hashtable：并发性能差

### 4.2 CopyOnWriteArrayList vs Vector
1. **锁策略**
   - CopyOnWriteArrayList：写时复制
   - Vector：方法级锁

2. **适用场景**
   - CopyOnWriteArrayList：读多写少
   - Vector：读写均衡

## 5. 最佳实践

### 5.1 ConcurrentHashMap最佳实践
1. **初始容量设置**
   - 合理设置初始大小
   - 避免频繁扩容

2. **复合操作注意**
   - 使用原子性方法
   - 注意弱一致性

### 5.2 CopyOnWriteArrayList最佳实践
1. **容量控制**
   - 控制元素数量
   - 避免过大的内存消耗

2. **更新频率**
   - 适用于低频率更新
   - 高频率更新考虑其他选择

### 5.3 通用建议
1. **选择合适的集合类型**
2. **注意迭代器的一致性语义**
3. **避免过度同步**
4. **合理使用批量操作**

## 6. 面试常见问题

### 6.1 原理相关
1. ConcurrentHashMap的实现原理？
2. CopyOnWriteArrayList的适用场景？
3. 各种线程安全集合的区别？

### 6.2 实践相关
1. 如何选择合适的线程安全集合？
2. 线程安全集合的性能考虑？
3. 如何处理复合操作的原子性？

### 6.3 进阶问题
1. JDK 8中ConcurrentHashMap的改进？
2. 弱一致性迭代器的含义？
3. 如何处理并发集合的性能问题？
