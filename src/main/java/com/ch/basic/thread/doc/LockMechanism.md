# Java锁机制详解

## 1. synchronized关键字

### 1.1 基本概念
- 内置锁（Intrinsic Lock）或监视器锁（Monitor Lock）
- 可重入锁
- 非公平锁
- 自动释放锁

### 1.2 使用方式
1. **同步实例方法**
```java
public synchronized void method() {}
```

2. **同步静态方法**
```java
public static synchronized void method() {}
```

3. **同步代码块**
```java
synchronized(object) {
    // 同步代码
}
```

### 1.3 实现原理
1. **对象头（Object Header）**
   - Mark Word：存储锁信息
   - Class Metadata Address：指向类元数据
   - Array Length（数组才有）

2. **锁升级过程**
   - 无锁状态
   - 偏向锁
   - 轻量级锁
   - 重量级锁

3. **Monitor机制**
   - Entry Set（阻塞队列）
   - Wait Set（等待队列）
   - Owner（持有锁的线程）

## 2. ReentrantLock

### 2.1 特点
- 可重入性
- 可中断性
- 可设置超时
- 可设置公平/非公平
- 可实现选择性通知
- 支持多个条件变量

### 2.2 基本用法
```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    // 临界区代码
} finally {
    lock.unlock();
}
```

### 2.3 高级特性
1. **可中断锁获取**
```java
try {
    lock.lockInterruptibly();
} catch (InterruptedException e) {
    // 处理中断
}
```

2. **超时锁获取**
```java
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try {
        // 获取到锁
    } finally {
        lock.unlock();
    }
}
```

3. **条件变量**
```java
Condition condition = lock.newCondition();
condition.await();  // 等待
condition.signal(); // 通知
```

## 3. synchronized vs ReentrantLock

### 3.1 相同点
- 都是可重入锁
- 都保证可见性和原子性
- 都支持独占访问

### 3.2 不同点
1. **实现方式**
   - synchronized：JVM层面
   - ReentrantLock：API层面

2. **功能特性**
   - 中断机制：ReentrantLock支持
   - 超时机制：ReentrantLock支持
   - 公平性选择：ReentrantLock支持
   - 条件变量：ReentrantLock支持多个

3. **性能**
   - JDK 6之前：ReentrantLock优于synchronized
   - JDK 6之后：性能基本持平

### 3.3 使用选择
1. **使用synchronized的场景**
   - 代码简单，不需要高级特性
   - 不需要公平性
   - 不需要中断或超时

2. **使用ReentrantLock的场景**
   - 需要高级功能（超时、中断等）
   - 需要公平锁
   - 需要多个条件变量
   - 需要选择性通知

## 4. 最佳实践

### 4.1 synchronized最佳实践
1. **缩小同步范围**
```java
// 不好的做法
synchronized void method() {
    // 大量非同步操作
    // 少量同步操作
}

// 好的做法
void method() {
    // 大量非同步操作
    synchronized(lock) {
        // 少量同步操作
    }
}
```

2. **避免死锁**
   - 固定加锁顺序
   - 避免嵌套锁
   - 及时释放锁
   - 使用超时机制

### 4.2 ReentrantLock最佳实践
1. **正确使用try-finally**
```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    // 业务代码
} finally {
    lock.unlock();
}
```

2. **合理使用高级特性**
   - 需要时才使用公平锁
   - 合理设置超时时间
   - 正确处理中断

## 5. 面试常见问题

### 5.1 synchronized
1. synchronized的实现原理？
2. 锁升级过程是什么？
3. synchronized和volatile的区别？
4. synchronized的优化方式？

### 5.2 ReentrantLock
1. ReentrantLock的实现原理？
2. 公平锁和非公平锁的区别？
3. 为什么要使用条件变量？
4. 如何避免死锁？

### 5.3 对比
1. synchronized和ReentrantLock的区别？
2. 什么时候用synchronized，什么时候用ReentrantLock？
3. 性能方面的考虑？
4. 使用中的注意事项？
