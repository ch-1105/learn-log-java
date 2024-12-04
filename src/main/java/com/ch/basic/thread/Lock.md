# Java锁机制深度解析

## 1. synchronized详解

### 1.1 实现原理
- **对象头Mark Word结构**
  ```
  32位JVM的Mark Word结构:
  |--------------------------------------------|
  |  锁标志位 |  锁状态  |    存储内容         |
  |--------------------------------------------|
  |    01    |  无锁    |  对象的hashCode     |
  |    01    |  偏向锁  |  线程ID            |
  |    00    |  轻量级锁|  指向栈中锁记录的指针|
  |    10    |  重量级锁|  指向互斥量的指针    |
  |    11    |  GC标记  |  空                 |
  |--------------------------------------------|
  ```

### 1.2 锁的升级过程
1. **偏向锁**
   - 目的：减少同一线程获取锁的开销
   - 原理：第一次获取锁时记录线程ID
   - 升级条件：其他线程尝试获取锁
   ```java
   // JVM参数
   -XX:+UseBiasedLocking  // 开启偏向锁
   -XX:BiasedLockingStartupDelay=0  // 取消偏向锁延迟
   ```

2. **轻量级锁**
   - 目的：避免线程进入内核态
   - 原理：CAS操作替代互斥量
   - 升级条件：CAS失败次数超过阈值
   ```java
   // 典型场景
   public synchronized void method() {
       // 短期持有的锁
       // 竞争较少的场景
   }
   ```

3. **重量级锁**
   - 原理：使用操作系统互斥量
   - 特点：线程阻塞、上下文切换
   ```java
   // 典型场景
   public synchronized void method() {
       // 长期持有的锁
       // 竞争激烈的场景
   }
   ```

### 1.3 synchronized的优化
1. **锁消除**
   ```java
   public void method() {
       // JVM会分析到StringBuffer只在方法内使用
       // 不可能被其他线程访问，所以会消除synchronized
       StringBuffer sb = new StringBuffer();
       sb.append("a");
       sb.append("b");
   }
   ```

2. **锁粗化**
   ```java
   // 优化前
   for (int i = 0; i < 100; i++) {
       synchronized(lock) {
           // 操作同步资源
       }
   }
   
   // 优化后
   synchronized(lock) {
       for (int i = 0; i < 100; i++) {
           // 操作同步资源
       }
   }
   ```

3. **适应性自旋**
   ```java
   // JVM参数
   -XX:+UseSpinning  // 开启自旋
   -XX:PreBlockSpin  // 自旋次数
   ```

## 2. Lock接口详解

### 2.1 ReentrantLock
```java
public class ReentrantLockExample {
    private final ReentrantLock lock = new ReentrantLock(true); // 公平锁
    
    public void method() {
        // 获取锁的不同方式
        lock.lock();  // 普通加锁
        lock.lockInterruptibly();  // 可中断加锁
        lock.tryLock();  // 尝试加锁
        lock.tryLock(1, TimeUnit.SECONDS);  // 限时等待
        
        try {
            // 临界区代码
        } finally {
            lock.unlock();  // 释放锁
        }
    }
}
```

#### 特性对比
| 特性 | synchronized | ReentrantLock |
|------|-------------|---------------|
| 可中断 | 不支持 | 支持 |
| 公平锁 | 不支持 | 支持 |
| 限时等待 | 不支持 | 支持 |
| 条件变量 | 一个 | 多个 |
| 自动释放 | 支持 | 不支持 |

### 2.2 ReadWriteLock
```java
public class CacheData {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private Map<String, Object> cache = new HashMap<>();
    
    public Object read(String key) {
        readLock.lock();
        try {
            return cache.get(key);
        } finally {
            readLock.unlock();
        }
    }
    
    public void write(String key, Object value) {
        writeLock.lock();
        try {
            cache.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }
}
```

### 2.3 StampedLock
```java
public class Point {
    private final StampedLock sl = new StampedLock();
    private double x, y;
    
    // 写锁
    public void move(double deltaX, double deltaY) {
        long stamp = sl.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            sl.unlockWrite(stamp);
        }
    }
    
    // 乐观读
    public double distanceFromOrigin() {
        long stamp = sl.tryOptimisticRead();
        double currentX = x, currentY = y;
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }
}
```

## 3. 锁的高级特性

### 3.1 可重入性
```java
public class ReentrantExample {
    public synchronized void outer() {
        inner(); // 可重入
    }
    
    public synchronized void inner() {
        // 同一个线程可以再次获取锁
    }
}
```

### 3.2 锁的公平性
```java
// 非公平锁（默认）
ReentrantLock unfairLock = new ReentrantLock();

// 公平锁
ReentrantLock fairLock = new ReentrantLock(true);
```

### 3.3 条件变量
```java
public class BoundedQueue<T> {
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Object[] items;
    private int count, putIndex, takeIndex;
    
    public void put(T x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();
            items[putIndex] = x;
            if (++putIndex == items.length)
                putIndex = 0;
            count++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            Object x = items[takeIndex];
            if (++takeIndex == items.length)
                takeIndex = 0;
            count--;
            notFull.signal();
            return (T) x;
        } finally {
            lock.unlock();
        }
    }
}
```

## 4. 使用场景分析

### 4.1 synchronized适用场景
1. 代码块执行时间短
2. 线程竞争不激烈
3. 需要自动释放锁
4. 只需要一个条件变量
```java
// 典型场景
public synchronized void transferMoney(Account from, Account to, int amount) {
    from.debit(amount);
    to.credit(amount);
}
```

### 4.2 ReentrantLock适用场景
1. 需要可中断锁
2. 需要公平锁
3. 需要多个条件变量
4. 需要限时等待
```java
// 典型场景
public boolean withdraw(Account account, int amount, long timeout) {
    if (lock.tryLock(timeout, TimeUnit.SECONDS)) {
        try {
            return account.debit(amount);
        } finally {
            lock.unlock();
        }
    }
    return false;
}
```

### 4.3 ReadWriteLock适用场景
1. 读多写少
2. 读操作可以并发执行
```java
// 典型场景
public class ConcurrentCache {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Map<String, Object> cache = new HashMap<>();
    
    public Object read(String key) {
        rwLock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    public void write(String key, Object value) {
        rwLock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

### 4.4 StampedLock适用场景
1. 读多写少
2. 性能要求高
3. 能够容忍读到过期数据
```java
// 典型场景
public class OptimisticReadExample {
    private final StampedLock sl = new StampedLock();
    private double x, y;
    
    public double calculateDistance() {
        long stamp = sl.tryOptimisticRead();
        double currentX = x, currentY = y;
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }
}
```

## 5. 性能考虑

### 5.1 锁的开销
1. 上下文切换
2. 内存同步
3. 线程调度

### 5.2 减少锁竞争的方法
1. 缩小锁的范围
2. 减少锁的持有时间
3. 锁分离
4. 锁粗化
5. 使用乐观锁

### 5.3 性能对比
```
性能从高到低：
无锁 > 偏向锁 > 轻量级锁 > 重量级锁
```

## 6. 死锁问题

### 6.1 死锁产生的条件
1. 互斥条件
2. 持有并等待
3. 不可剥夺
4. 循环等待

### 6.2 死锁预防
```java
// 1. 固定加锁顺序
public void transfer(Account fromAccount, Account toAccount, int amount) {
    Account first = fromAccount.getId() < toAccount.getId() ? fromAccount : toAccount;
    Account second = fromAccount.getId() < toAccount.getId() ? toAccount : fromAccount;
    
    synchronized(first) {
        synchronized(second) {
            // 转账操作
        }
    }
}

// 2. 使用tryLock避免死锁
public boolean transfer(Account fromAccount, Account toAccount, int amount) {
    while (true) {
        if (fromAccount.getLock().tryLock()) {
            try {
                if (toAccount.getLock().tryLock()) {
                    try {
                        // 转账操作
                        return true;
                    } finally {
                        toAccount.getLock().unlock();
                    }
                }
            } finally {
                fromAccount.getLock().unlock();
            }
        }
        // 随机等待一段时间再重试
        Thread.sleep(new Random().nextInt(1000));
    }
}
```

## 7. 锁优化实践

### 7.1 分段锁
```java
public class StripedMap {
    private static final int N_LOCKS = 16;
    private final Node[] buckets;
    private final Object[] locks;
    
    private static class Node {
        Node next;
        Object key;
        Object value;
    }
    
    public StripedMap(int numBuckets) {
        buckets = new Node[numBuckets];
        locks = new Object[N_LOCKS];
        for (int i = 0; i < N_LOCKS; i++)
            locks[i] = new Object();
    }
    
    private final int hash(Object key) {
        return Math.abs(key.hashCode() % buckets.length);
    }
    
    private Object getLock(Object key) {
        return locks[Math.abs(key.hashCode() % N_LOCKS)];
    }
    
    public Object get(Object key) {
        int hash = hash(key);
        synchronized (getLock(key)) {
            for (Node m = buckets[hash]; m != null; m = m.next)
                if (m.key.equals(key))
                    return m.value;
        }
        return null;
    }
}
```

### 7.2 锁消除
```java
public String concatString(String s1, String s2, String s3) {
    // JVM会进行锁消除优化
    StringBuffer sb = new StringBuffer();
    sb.append(s1);
    sb.append(s2);
    sb.append(s3);
    return sb.toString();
}
```

### 7.3 锁粗化
```java
// 优化前
for (int i = 0; i < N; i++) {
    synchronized(lock) {
        // 操作共享数据
    }
}

// 优化后
synchronized(lock) {
    for (int i = 0; i < N; i++) {
        // 操作共享数据
    }
}
```

## 8. 面试重点

### 8.1 synchronized vs ReentrantLock
1. 实现层面
   - synchronized是JVM层面的锁实现
   - ReentrantLock是API层面的锁实现

2. 功能层面
   - ReentrantLock具有更多特性
   - synchronized使用更简单，由JVM管理

3. 性能层面
   - 低竞争：synchronized优于ReentrantLock
   - 高竞争：两者性能接近

### 8.2 锁优化相关问题
1. 偏向锁的原理
2. 轻量级锁的实现
3. 锁消除的判定
4. 锁粗化的时机
5. 自旋锁的阈值

### 8.3 实践经验
1. 优先使用synchronized
2. 需要特殊特性时使用ReentrantLock
3. 读多写少场景考虑ReadWriteLock
4. 性能要求高的场景考虑StampedLock
