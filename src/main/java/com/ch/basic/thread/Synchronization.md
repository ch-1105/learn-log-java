# Java 线程同步机制详解

## 1. 线程安全问题

### 1.1 什么是线程安全
- 当多个线程同时访问共享资源时，程序的执行结果符合预期
- 保证数据的一致性、可见性和有序性

### 1.2 线程不安全的表现
- 数据不一致
- 死锁
- 活锁
- 饥饿

## 2. synchronized关键字

### 2.1 基本用法
```java
// 同步实例方法
public synchronized void method() {
    // 同步代码
}

// 同步静态方法
public static synchronized void staticMethod() {
    // 同步代码
}

// 同步代码块
synchronized(lockObject) {
    // 同步代码
}
```

### 2.2 synchronized原理
- 对象头中的Mark Word
- 锁的升级过程
  - 偏向锁
  - 轻量级锁
  - 重量级锁
- 同步队列和等待队列

### 2.3 synchronized优化
- 锁消除
- 锁粗化
- 自旋锁
- 适应性自旋

## 3. volatile关键字

### 3.1 作用
- 保证可见性
- 禁止指令重排序
- 不保证原子性

### 3.2 实现原理
- 内存屏障（Memory Barrier）
- 缓存一致性协议（MESI）

### 3.3 使用场景
```java
// 状态标志
private volatile boolean flag = false;

// 双重检查锁定
private volatile static Instance instance;
public static Instance getInstance() {
    if (instance == null) {
        synchronized (Instance.class) {
            if (instance == null) {
                instance = new Instance();
            }
        }
    }
    return instance;
}
```

## 4. 原子类

### 4.1 基本类型原子类
```java
AtomicInteger
AtomicLong
AtomicBoolean
```

### 4.2 数组原子类
```java
AtomicIntegerArray
AtomicLongArray
AtomicReferenceArray
```

### 4.3 引用原子类
```java
AtomicReference
AtomicStampedReference
AtomicMarkableReference
```

### 4.4 字段更新器
```java
AtomicIntegerFieldUpdater
AtomicLongFieldUpdater
AtomicReferenceFieldUpdater
```

## 5. 显式锁

### 5.1 ReentrantLock
```java
private final ReentrantLock lock = new ReentrantLock();

public void method() {
    lock.lock();
    try {
        // 临界区代码
    } finally {
        lock.unlock();
    }
}
```

### 5.2 ReentrantLock特性
- 可重入性
- 公平性选择
- 限时等待
- 可中断
- 条件变量

### 5.3 ReadWriteLock
```java
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
private final Lock readLock = rwLock.readLock();
private final Lock writeLock = rwLock.writeLock();
```

### 5.4 StampedLock
```java
private final StampedLock sl = new StampedLock();

// 乐观读
long stamp = sl.tryOptimisticRead();
// 读取共享变量
if (!sl.validate(stamp)) {
    // 升级为悲观读锁
    stamp = sl.readLock();
    try {
        // 读取共享变量
    } finally {
        sl.unlockRead(stamp);
    }
}
```

## 6. 同步工具类

### 6.1 Semaphore
```java
private final Semaphore semaphore = new Semaphore(5);

public void method() {
    try {
        semaphore.acquire();
        // 访问资源
    } finally {
        semaphore.release();
    }
}
```

### 6.2 CountDownLatch
```java
private final CountDownLatch latch = new CountDownLatch(3);

// 等待线程
latch.await();

// 计数线程
latch.countDown();
```

### 6.3 CyclicBarrier
```java
private final CyclicBarrier barrier = new CyclicBarrier(3);

// 等待其他线程
barrier.await();
```

### 6.4 Phaser
```java
private final Phaser phaser = new Phaser(3);

// 等待其他线程
phaser.arriveAndAwaitAdvance();

// 完成当前阶段
phaser.arrive();
```

## 7. 线程安全集合

### 7.1 同步包装器
```java
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
Set<String> syncSet = Collections.synchronizedSet(new HashSet<>());
Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<>());
```

### 7.2 并发集合
```java
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
```

## 8. 最佳实践

### 8.1 锁的选择
1. 优先使用并发集合而不是同步集合
2. 优先使用原子类而不是synchronized
3. 使用synchronized内置锁时注意锁的粒度
4. 适当时候使用volatile而不是锁

### 8.2 性能优化
1. 减少锁的持有时间
2. 减小锁的粒度
3. 读多写少场景使用读写锁
4. 考虑乐观锁代替悲观锁

### 8.3 死锁预防
1. 固定加锁顺序
2. 避免嵌套锁
3. 使用限时锁
4. 及时释放锁

## 9. 面试要点

### 9.1 synchronized vs volatile
1. volatile只能修饰变量，synchronized可以修饰方法和代码块
2. volatile保证可见性和有序性，synchronized还保证原子性
3. volatile不会造成线程阻塞，synchronized可能会阻塞线程
4. volatile适合一写多读场景

### 9.2 synchronized vs ReentrantLock
1. ReentrantLock具有更好的灵活性
2. ReentrantLock支持非阻塞获取锁
3. ReentrantLock可以实现公平锁
4. ReentrantLock需要手动释放锁

### 9.3 锁优化
1. 锁消除
2. 锁粗化
3. 锁升级
4. 偏向锁
5. 轻量级锁
6. 重量级锁
