# Java 多线程与并发编程

## 学习路线图

### 1. 基础概念
- 进程与线程
- 线程的生命周期
- 线程的创建和使用
- 线程安全
- 同步与异步

### 2. 线程基础 API
- Thread类
- Runnable接口
- Callable接口
- Future接口
- 线程组
- 线程优先级

### 3. 线程同步
- synchronized关键字
- volatile关键字
- 原子类
- 锁机制
  - ReentrantLock
  - ReadWriteLock
  - StampedLock
- 条件变量

### 4. 线程通信
- wait/notify机制
- park/unpark机制
- 信号量
- CountDownLatch
- CyclicBarrier
- Phaser
- Exchanger

### 5. 线程池
- ThreadPoolExecutor
- ScheduledThreadPoolExecutor
- ForkJoinPool
- 自定义线程池
- 线程池最佳实践

### 6. 并发集合
- ConcurrentHashMap
- CopyOnWriteArrayList
- BlockingQueue
  - ArrayBlockingQueue
  - LinkedBlockingQueue
  - PriorityBlockingQueue
- ConcurrentLinkedQueue
- DelayQueue

### 7. 并发设计模式
- 生产者-消费者模式
- 发布-订阅模式
- 主从模式
- 工作窃取模式

### 8. JMM（Java内存模型）
- 内存可见性
- 指令重排序
- happens-before原则
- 内存屏障

### 9. 并发工具类
- CompletableFuture
- CompletionService
- ThreadLocal
- TimeUnit

### 10. 实战案例
- 线程池监控系统
- 高并发限流器
- 分布式锁实现
- 异步任务框架

### 11. 性能优化
- 线程池调优
- 锁优化
- 无锁编程
- 并发性能测试

### 12. 最佳实践
- 线程安全策略
- 死锁预防
- 性能与可伸缩性
- 调试与监控

## 项目结构
```
thread/
├── basic/           # 基础概念和API使用
├── sync/            # 同步机制
├── communication/   # 线程通信
├── pool/            # 线程池
├── collections/     # 并发集合
├── patterns/        # 并发设计模式
├── jmm/            # Java内存模型
├── tools/          # 并发工具类
└── cases/          # 实战案例
```

## 学习目标
1. 深入理解Java多线程与并发编程的核心概念
2. 掌握线程安全的各种实现方式
3. 能够设计和实现高性能的并发程序
4. 理解并发编程中的常见问题及解决方案
5. 掌握线程池的使用和调优
6. 熟练使用Java并发包（java.util.concurrent）
7. 能够进行并发程序的性能优化和调试

## 注意事项
1. 循序渐进，从基础开始
2. 注重实践，每个概念都要有代码示例
3. 关注性能和安全性
4. 理解底层原理
5. 掌握调试技巧

## 参考资源
- Java并发编程实战
- Java并发编程的艺术
- Java Memory Model Specification
- Doug Lea的并发编程著作
- Oracle Java Documentation
