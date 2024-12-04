# Java线程池详解

## 1. 线程池基本概念

### 1.1 为什么需要线程池
- 减少线程创建和销毁的开销
- 提高响应速度
- 便于线程管理
- 提供资源限制和管理

### 1.2 核心组件
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

## 2. 线程池类型

### 2.1 FixedThreadPool
```java
// 固定大小的线程池
ExecutorService fixedPool = Executors.newFixedThreadPool(nThreads);

// 实现原理
new ThreadPoolExecutor(nThreads, nThreads,
                      0L, TimeUnit.MILLISECONDS,
                      new LinkedBlockingQueue<Runnable>());
```

### 2.2 CachedThreadPool
```java
// 可缓存的线程池
ExecutorService cachedPool = Executors.newCachedThreadPool();

// 实现原理
new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                      60L, TimeUnit.SECONDS,
                      new SynchronousQueue<Runnable>());
```

### 2.3 SingleThreadExecutor
```java
// 单线程执行器
ExecutorService singlePool = Executors.newSingleThreadExecutor();

// 实现原理
new ThreadPoolExecutor(1, 1,
                      0L, TimeUnit.MILLISECONDS,
                      new LinkedBlockingQueue<Runnable>());
```

### 2.4 ScheduledThreadPool
```java
// 调度线程池
ScheduledExecutorService scheduledPool = 
    Executors.newScheduledThreadPool(corePoolSize);

// 实现原理
new ScheduledThreadPoolExecutor(corePoolSize);
```

## 3. 线程池参数详解

### 3.1 核心线程数（corePoolSize）
- 线程池中常驻的线程数量
- 即使线程空闲也不会被回收
- 可以通过allowCoreThreadTimeOut(true)允许回收

### 3.2 最大线程数（maximumPoolSize）
- 线程池中允许的最大线程数
- 当工作队列满时才会创建超出核心线程数的线程
- 必须大于等于核心线程数

### 3.3 线程存活时间（keepAliveTime）
- 超出核心线程数的线程的空闲时间
- 超过这个时间，多余的线程会被回收
- 只有在线程数大于核心线程数时才有效

### 3.4 工作队列（workQueue）
1. **ArrayBlockingQueue**
   ```java
   // 有界队列
   new ArrayBlockingQueue<>(capacity);
   ```

2. **LinkedBlockingQueue**
   ```java
   // 无界队列（实际受内存限制）
   new LinkedBlockingQueue<>();
   ```

3. **SynchronousQueue**
   ```java
   // 直接交付队列
   new SynchronousQueue<>();
   ```

4. **PriorityBlockingQueue**
   ```java
   // 优先级队列
   new PriorityBlockingQueue<>();
   ```

### 3.5 拒绝策略（RejectedExecutionHandler）
1. **AbortPolicy**
   ```java
   // 抛出异常（默认策略）
   new ThreadPoolExecutor.AbortPolicy();
   ```

2. **CallerRunsPolicy**
   ```java
   // 由调用线程执行任务
   new ThreadPoolExecutor.CallerRunsPolicy();
   ```

3. **DiscardPolicy**
   ```java
   // 直接丢弃任务
   new ThreadPoolExecutor.DiscardPolicy();
   ```

4. **DiscardOldestPolicy**
   ```java
   // 丢弃最旧的任务
   new ThreadPoolExecutor.DiscardOldestPolicy();
   ```

## 4. 线程池的生命周期

### 4.1 状态转换
```
RUNNING -> SHUTDOWN -> STOP -> TIDYING -> TERMINATED
```

### 4.2 关闭方法
```java
// 平缓关闭
void shutdown();

// 立即关闭
List<Runnable> shutdownNow();

// 等待终止
boolean awaitTermination(long timeout, TimeUnit unit);
```

## 5. 线程池监控

### 5.1 基本指标
```java
// 当前线程数
int getPoolSize();

// 活跃线程数
int getActiveCount();

// 完成任务数
long getCompletedTaskCount();

// 任务总数
long getTaskCount();

// 队列大小
int getQueue().size();
```

### 5.2 扩展监控
```java
public class MonitorableThreadPool extends ThreadPoolExecutor {
    private final AtomicLong totalTime = new AtomicLong();
    private final AtomicLong totalTasks = new AtomicLong();
    
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        // 记录开始时间
    }
    
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        // 计算执行时间
        super.afterExecute(r, t);
    }
}
```

## 6. 最佳实践

### 6.1 线程池大小设置
```java
// CPU密集型任务
int threads = Runtime.getRuntime().availableProcessors() + 1;

// IO密集型任务
int threads = Runtime.getRuntime().availableProcessors() * 2;
```

### 6.2 工作队列选择
1. CPU密集型：使用SynchronousQueue
2. IO密集型：使用LinkedBlockingQueue
3. 混合型：使用ArrayBlockingQueue

### 6.3 拒绝策略选择
1. 重要任务：CallerRunsPolicy
2. 可丢弃任务：DiscardPolicy
3. 优先级任务：DiscardOldestPolicy

## 7. 常见问题

### 7.1 线程池满了怎么办
1. 增加最大线程数
2. 使用更大的队列
3. 使用合适的拒绝策略
4. 添加任务超时机制

### 7.2 线程池内存泄漏
1. 任务对象持有大对象引用
2. 任务执行时间过长
3. 线程本地变量未清理

### 7.3 性能优化
1. 避免任务执行时间过长
2. 合理设置队列大小
3. 及时关闭不用的线程池
4. 使用预热机制

## 8. 实际应用场景

### 8.1 Web服务器
```java
// HTTP请求处理线程池
ThreadPoolExecutor serverPool = new ThreadPoolExecutor(
    10, 100, 60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(1000),
    new ThreadPoolExecutor.CallerRunsPolicy());
```

### 8.2 定时任务调度
```java
// 定时任务线程池
ScheduledExecutorService scheduler = 
    Executors.newScheduledThreadPool(5);
    
scheduler.scheduleAtFixedRate(task, 
    initialDelay, period, TimeUnit.SECONDS);
```

### 8.3 异步处理
```java
// 异步处理线程池
ExecutorService asyncPool = new ThreadPoolExecutor(
    5, 10, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100),
    new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("AsyncThread-" + t.getId());
            return t;
        }
    },
    new ThreadPoolExecutor.CallerRunsPolicy());
```

## 9. 面试要点

### 9.1 线程池核心参数
1. corePoolSize的设置依据
2. maximumPoolSize的确定方法
3. keepAliveTime的作用
4. 工作队列的选择

### 9.2 线程池工作原理
1. 任务提交流程
2. 线程创建时机
3. 任务执行顺序
4. 拒绝策略触发条件

### 9.3 实践经验
1. 线程池参数调优
2. 常见问题处理
3. 监控指标设置
4. 性能优化方法
