# Java内存模型（JMM）与volatile关键字

## 1. Java内存模型（JMM）

### 1.1 什么是JMM？
- JMM定义了Java虚拟机如何与计算机内存（RAM）进行交互
- 规定了一个线程如何和何时可以看到其他线程修改过的共享变量的值
- 规定了在什么情况下一个线程修改的共享变量对其他线程是可见的

### 1.2 JMM的主要内容
1. **工作内存与主内存**
   - 主内存：所有线程共享的内存区域
   - 工作内存：每个线程私有的内存区域
   - 线程对变量的操作必须在工作内存中进行

2. **内存交互操作**
   - read：从主内存读取
   - load：将主内存读取的值载入工作内存
   - use：从工作内存读取
   - assign：写入工作内存
   - store：存入主内存
   - write：写入主内存
   - lock：锁定主内存变量
   - unlock：解锁主内存变量

### 1.3 重排序
1. **三种重排序**
   - 编译器优化重排序
   - 指令级并行重排序
   - 内存系统重排序

2. **happens-before原则**
   - 程序顺序规则
   - 监视器锁规则
   - volatile变量规则
   - 传递性规则
   - 线程启动规则
   - 线程终止规则
   - 中断规则
   - 对象终结规则

## 2. volatile关键字

### 2.1 volatile的特性
1. **可见性**
   - 一个线程修改了volatile变量，其他线程立即可见
   - 实现原理：写入主内存时会强制刷新其他线程的工作内存

2. **有序性**
   - 禁止指令重排序
   - 实现内存屏障（Memory Barrier）

3. **原子性**
   - 仅保证单次读/写操作的原子性
   - 不保证复合操作的原子性

### 2.2 使用场景
1. **状态标志**
```java
private volatile boolean flag = false;

// 线程1
public void shutdown() {
    flag = true;
}

// 线程2
public void doWork() {
    while (!flag) {
        // 工作
    }
}
```

2. **双重检查锁定（DCL）**
```java
public class Singleton {
    private volatile static Singleton instance;
    
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

### 2.3 不适用场景
1. **需要原子性的复合操作**
```java
public class Counter {
    private volatile int count = 0;  // volatile不能保证原子性
    
    public void increment() {
        count++;  // 非原子操作
    }
}
```

2. **依赖当前值的运算**
```java
volatile int value = 0;
// 不安全的操作
value = value + 1;
```

## 3. 面试常见问题

### 3.1 volatile vs synchronized
1. **volatile**
   - 轻量级
   - 只能修饰变量
   - 保证可见性和有序性
   - 不保证原子性
   - 不会造成线程阻塞

2. **synchronized**
   - 重量级
   - 可修饰方法和代码块
   - 保证可见性、有序性和原子性
   - 可能造成线程阻塞

### 3.2 volatile的实现原理
1. **内存屏障**
   - LoadLoad屏障
   - StoreStore屏障
   - LoadStore屏障
   - StoreLoad屏障

2. **MESI缓存一致性协议**
   - 通过CPU缓存一致性协议保证可见性
   - 写操作会导致其他CPU缓存失效

### 3.3 什么时候用volatile？
1. **适用场景**
   - 写入变量值不依赖当前值
   - 单个线程写，多个线程读
   - 作为状态标志
   - DCL中的单例模式

2. **不适用场景**
   - 需要原子性的场景
   - 需要事务性的场景
   - 复合操作的场景

## 4. 实际应用示例

### 4.1 状态标志示例
```java
public class TaskManager {
    private volatile boolean isRunning = true;
    
    public void shutdown() {
        isRunning = false;
    }
    
    public void doWork() {
        while (isRunning) {
            // 执行任务
        }
    }
}
```

### 4.2 发布-订阅模式
```java
public class EventManager {
    private volatile String lastEvent;
    
    public void publish(String event) {
        lastEvent = event;
    }
    
    public String getLastEvent() {
        return lastEvent;
    }
}
```

### 4.3 懒加载
```java
public class Resource {
    private volatile Resource instance;
    
    public Resource getInstance() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = new Resource();
                }
            }
        }
        return instance;
    }
}
```

## 5. 最佳实践

### 5.1 使用建议
1. 优先考虑不可变对象
2. 谨慎使用volatile
3. 考虑使用atomic类
4. 必要时使用synchronized

### 5.2 性能考虑
1. volatile比synchronized轻量
2. 但volatile也会影响性能
3. 过度使用会导致缓存失效

### 5.3 调试技巧
1. 使用jcmd查看JVM内存状态
2. 使用Java Flight Recorder分析
3. 使用jstack查看线程状态
