# Java并发编程：深入理解AbstractQueuedSynchronizer（AQS）

## 一、AQS概述

### 1.1 定义
AbstractQueuedSynchronizer（简称AQS）是Java并发包（java.util.concurrent）中的核心基础组件，用于构建锁或其他同步组件的基础框架。它使用一个整型的state变量表示同步状态，通过内置的FIFO队列来完成线程的排队工作。

### 1.2 核心特性
1. **状态管理**：使用单个原子整型变量表示状态
2. **FIFO队列**：使用CLH变体的双向队列管理等待线程
3. **可中断性**：支持独占/共享模式的获取与释放
4. **公平性选择**：支持公平与非公平两种模式
5. **条件变量**：支持条件变量（Condition），实现等待/通知机制

## 二、AQS核心架构

### 2.1 同步状态（State）
```java
private volatile int state; // 核心状态字段
```

状态变量的使用方式：
- **独占模式**：state表示是否被占用（0：未占用，1：占用）
- **共享模式**：state表示可用资源数量
- **重入锁**：state表示重入次数

### 2.2 等待队列
```java
static final class Node {
    volatile Node prev;       // 前驱节点
    volatile Node next;       // 后继节点
    volatile Thread thread;   // 当前线程
    volatile int waitStatus; // 等待状态
}
```

节点状态（waitStatus）：
- CANCELLED(1)：线程已取消
- SIGNAL(-1)：后继节点需要唤醒
- CONDITION(-2)：节点在条件队列中
- PROPAGATE(-3)：共享式同步状态将会传播
- 0：初始状态

## 三、AQS工作原理

### 3.1 独占模式获取流程
1. **尝试获取资源**：调用tryAcquire()尝试获取资源
2. **加入等待队列**：获取失败则将线程包装成Node加入队列
3. **自旋等待**：循环检查前驱节点，满足条件则尝试获取资源
4. **阻塞等待**：不满足条件则阻塞当前线程

关键源码分析：
```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

### 3.2 独占模式释放流程
1. **释放资源**：调用tryRelease()释放资源
2. **唤醒后继**：如果释放成功，唤醒后继节点

关键源码分析：
```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

## 四、AQS实现分析

### 4.1 ReentrantLock的实现
ReentrantLock通过继承AQS实现可重入锁：

```java
public class ReentrantLock {
    private final Sync sync;
    
    abstract static class Sync extends AbstractQueuedSynchronizer {
        // state表示重入次数
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                setState(c + acquires);
                return true;
            }
            return false;
        }
    }
}
```

### 4.2 CountDownLatch的实现
CountDownLatch使用AQS的共享模式：

```java
public class CountDownLatch {
    private static final class Sync extends AbstractQueuedSynchronizer {
        // state表示计数器值
        Sync(int count) {
            setState(count);
        }
        
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }
        
        protected boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                if (c == 0) return false;
                int nextc = c - 1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }
}
```

## 五、AQS最佳实践

### 5.1 实现自定义同步器的标准步骤
1. 继承AbstractQueuedSynchronizer类
2. 根据需求实现tryAcquire/tryRelease或tryAcquireShared/tryReleaseShared方法
3. 实现isHeldExclusively方法（独占模式需要）
4. 根据需要实现其他辅助方法

### 5.2 使用建议
1. **选择合适的模式**：
   - 独占模式：一次只能被一个线程获取的同步器
   - 共享模式：可以被多个线程同时获取的同步器

2. **状态管理**：
   - 合理设计state的含义
   - 正确使用CAS操作更新状态

3. **超时处理**：
   - 合理设置获取资源的超时时间
   - 正确处理中断异常

## 六、面试要点

### 6.1 基础概念
Q：什么是AQS？为什么需要AQS？
A：AQS是构建锁和同步器的框架，它实现了线程等待队列的管理、线程的阻塞和唤醒等底层操作，避免了重复造轮子，提供了一种标准的资源访问模式。

### 6.2 实现原理
Q：AQS的等待队列是如何实现的？
A：AQS使用CLH队列的变体实现等待队列，是一个双向队列。每个节点包含线程信息、等待状态、前驱和后继节点的引用。

### 6.3 性能考量
Q：AQS如何保证性能？
A：
1. 使用CAS操作保证原子性
2. 采用自旋和阻塞结合的方式
3. 使用双向队列提高操作效率
4. 设计了超时机制避免永久等待

## 七、总结

AQS通过提供一个标准的同步器框架，大大简化了并发编程的复杂度。它的核心思想是：
1. 使用state表示同步状态
2. 使用CLH队列管理等待线程
3. 提供独占和共享两种模式
4. 支持超时和中断机制

掌握AQS的原理和使用方法，对于理解Java并发包的实现原理以及自定义同步器都有重要意义。
