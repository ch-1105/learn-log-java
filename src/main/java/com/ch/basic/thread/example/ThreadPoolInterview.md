# Java线程池面试核心知识点

## 1. 线程池核心参数（必考）

### 1.1 七大核心参数
```java
public ThreadPoolExecutor(
    int corePoolSize,          // 核心线程数
    int maximumPoolSize,       // 最大线程数
    long keepAliveTime,        // 线程存活时间
    TimeUnit unit,             // 时间单位
    BlockingQueue<Runnable> workQueue,  // 工作队列
    ThreadFactory threadFactory,         // 线程工厂
    RejectedExecutionHandler handler)    // 拒绝策略
```

### 1.2 参数详解
1. **corePoolSize（核心线程数）**
   - 线程池中常驻的线程数量
   - 即使线程空闲也不会被回收（除非设置allowCoreThreadTimeOut）
   - 如何设置：CPU密集型=CPU核心数+1，IO密集型=CPU核心数*2

2. **maximumPoolSize（最大线程数）**
   - 线程池能创建的最大线程数
   - 当工作队列满时才会创建超出核心线程数的线程
   - 必须大于等于核心线程数

3. **keepAliveTime（线程存活时间）**
   - 超出核心线程数的线程的空闲时间
   - 超过这个时间，多余的线程会被回收
   - 只有在线程数大于核心线程数时才有效

4. **workQueue（工作队列）**
   - ArrayBlockingQueue：有界队列
   - LinkedBlockingQueue：无界队列
   - SynchronousQueue：直接交付队列
   - PriorityBlockingQueue：优先级队列

5. **拒绝策略（RejectedExecutionHandler）**
   - AbortPolicy：抛出异常（默认）
   - CallerRunsPolicy：调用者线程执行
   - DiscardPolicy：直接丢弃
   - DiscardOldestPolicy：丢弃最旧的任务

## 2. 线程池工作原理（必考）

### 2.1 工作流程
1. 提交任务时，如果线程数小于核心线程数，创建新线程
2. 如果线程数等于核心线程数，任务进入队列
3. 如果队列满了，且线程数小于最大线程数，创建新线程
4. 如果队列满了，且线程数等于最大线程数，执行拒绝策略

### 2.2 执行示意图
```
提交任务 
   ↓
线程数 < 核心线程数? → 是 → 创建新线程
   ↓ 否
队列未满? → 是 → 加入队列
   ↓ 否
线程数 < 最大线程数? → 是 → 创建新线程
   ↓ 否
执行拒绝策略
```

## 3. 四种常见线程池（高频考点）

### 3.1 FixedThreadPool
```java
ExecutorService fixedPool = Executors.newFixedThreadPool(nThreads);
```
- 特点：固定线程数
- 应用：适合需要控制线程数量的场景
- 原理：核心线程数=最大线程数，使用无界队列

### 3.2 CachedThreadPool
```java
ExecutorService cachedPool = Executors.newCachedThreadPool();
```
- 特点：线程数量不固定，按需创建
- 应用：执行大量短期异步任务
- 原理：核心线程数=0，最大线程数=Integer.MAX_VALUE

### 3.3 SingleThreadExecutor
```java
ExecutorService singlePool = Executors.newSingleThreadExecutor();
```
- 特点：只有一个工作线程
- 应用：需要保证顺序执行的场景
- 原理：核心线程数=最大线程数=1

### 3.4 ScheduledThreadPool
```java
ScheduledExecutorService scheduledPool = 
    Executors.newScheduledThreadPool(corePoolSize);
```
- 特点：支持定时及周期性任务执行
- 应用：定时任务场景
- 原理：使用DelayQueue实现定时功能

## 4. 实际工作中的使用（面试常问）

### 4.1 线程池参数如何配置？
```java
// CPU密集型任务
int threads = Runtime.getRuntime().availableProcessors() + 1;

// IO密集型任务
int threads = Runtime.getRuntime().availableProcessors() * 2;
```

### 4.2 为什么不推荐使用Executors？
1. FixedThreadPool和SingleThreadExecutor使用无界队列，可能导致OOM
2. CachedThreadPool和ScheduledThreadPool最大线程数是Integer.MAX_VALUE，可能创建大量线程

### 4.3 正确的线程池创建方式
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    corePoolSize,
    maximumPoolSize,
    keepAliveTime,
    TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(queueCapacity),
    new ThreadPoolExecutor.CallerRunsPolicy());
```

## 5. 常见面试题

### 5.1 线程池的优势是什么？
1. 降低资源消耗（重用线程）
2. 提高响应速度（减少线程创建时间）
3. 提高线程的可管理性
4. 提供更多更强大的功能（定时、定期执行等）

### 5.2 线程池都有哪几种工作队列？
1. ArrayBlockingQueue：有界队列，FIFO
2. LinkedBlockingQueue：可设置容量，默认无界
3. SynchronousQueue：不存储元素，直接交付
4. PriorityBlockingQueue：优先级队列
5. DelayQueue：延时队列

### 5.3 线程池的关闭方式有哪些？
```java
// 温和关闭：等待任务完成
void shutdown();

// 立即关闭：尝试中断正在执行的任务
List<Runnable> shutdownNow();

// 等待终止
boolean awaitTermination(long timeout, TimeUnit unit);
```

### 5.4 如何合理配置线程池参数？
1. **核心线程数**：
   - CPU密集型：CPU核心数 + 1
   - IO密集型：CPU核心数 * 2
   - 混合型：根据场景具体分析

2. **最大线程数**：
   - 考虑系统资源承受能力
   - 考虑任务类型（CPU密集/IO密集）
   - 通常设置为核心线程数的2倍左右

3. **工作队列**：
   - CPU密集型：使用较小的队列
   - IO密集型：使用较大的队列
   - 内存有限：使用有界队列

4. **拒绝策略**：
   - 重要任务：CallerRunsPolicy
   - 可丢弃任务：DiscardPolicy
   - 默认情况：AbortPolicy

## 6. 实际案例分析

### 6.1 常见问题及解决方案
1. **线程池满了怎么办？**
   - 使用合适的拒绝策略
   - 监控线程池状态
   - 适当调整参数

2. **任务执行慢怎么处理？**
   - 分析任务类型（CPU密集/IO密集）
   - 调整线程池参数
   - 考虑任务拆分

3. **如何避免线程池死锁？**
   - 避免任务之间相互依赖
   - 合理设置超时时间
   - 使用多个线程池隔离任务

### 6.2 最佳实践
1. 使用有界队列
2. 自定义线程工厂
3. 监控线程池状态
4. 根据实际场景调整参数
