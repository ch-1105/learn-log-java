# Java 线程基础详解

## 1. 线程基本概念

### 1.1 什么是线程
- 线程是程序执行的最小单位
- 一个进程可以包含多个线程
- 线程共享进程的资源
- 每个线程都有自己的程序计数器、栈和局部变量

### 1.2 线程的状态
```java
public enum State {
    NEW,           // 新建状态
    RUNNABLE,      // 可运行状态
    BLOCKED,       // 阻塞状态
    WAITING,       // 等待状态
    TIMED_WAITING, // 超时等待状态
    TERMINATED     // 终止状态
}
```

## 2. 创建线程的方式

### 2.1 继承Thread类
```java
public class MyThread extends Thread {
    @Override
    public void run() {
        // 线程执行的代码
    }
}

// 使用方式
MyThread thread = new MyThread();
thread.start();
```

### 2.2 实现Runnable接口
```java
public class MyRunnable implements Runnable {
    @Override
    public void run() {
        // 线程执行的代码
    }
}

// 使用方式
Thread thread = new Thread(new MyRunnable());
thread.start();

// Lambda表达式方式
Thread thread = new Thread(() -> {
    // 线程执行的代码
});
thread.start();
```

### 2.3 实现Callable接口
```java
public class MyCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        // 线程执行的代码
        return "执行结果";
    }
}

// 使用方式
FutureTask<String> futureTask = new FutureTask<>(new MyCallable());
Thread thread = new Thread(futureTask);
thread.start();
String result = futureTask.get(); // 获取返回值
```

## 3. 线程的基本操作

### 3.1 线程启动
```java
thread.start(); // 启动线程
```

### 3.2 线程休眠
```java
Thread.sleep(1000); // 休眠1秒
```

### 3.3 线程中断
```java
thread.interrupt(); // 中断线程
boolean interrupted = Thread.interrupted(); // 检查中断状态
boolean isInterrupted = thread.isInterrupted(); // 检查中断状态
```

### 3.4 线程等待和唤醒
```java
synchronized(object) {
    object.wait(); // 等待
    object.notify(); // 唤醒单个线程
    object.notifyAll(); // 唤醒所有线程
}
```

### 3.5 线程合并
```java
thread.join(); // 等待线程结束
thread.join(1000); // 等待线程结束，最多等待1秒
```

## 4. 线程优先级

### 4.1 优先级设置
```java
thread.setPriority(Thread.MIN_PRIORITY); // 1
thread.setPriority(Thread.NORM_PRIORITY); // 5
thread.setPriority(Thread.MAX_PRIORITY); // 10
```

### 4.2 守护线程
```java
thread.setDaemon(true); // 设置为守护线程
boolean isDaemon = thread.isDaemon(); // 判断是否为守护线程
```

## 5. 线程安全

### 5.1 synchronized关键字
```java
// 同步方法
public synchronized void method() {
    // 线程安全的代码
}

// 同步代码块
synchronized(object) {
    // 线程安全的代码
}
```

### 5.2 volatile关键字
```java
private volatile boolean flag = false; // 保证可见性
```

## 6. 线程组

### 6.1 创建和使用线程组
```java
ThreadGroup group = new ThreadGroup("myGroup");
Thread thread = new Thread(group, new MyRunnable());
```

### 6.2 线程组操作
```java
group.activeCount(); // 获取活动线程数
group.list(); // 打印线程组信息
group.interrupt(); // 中断线程组中所有线程
```

## 7. 最佳实践

### 7.1 线程命名
```java
thread.setName("MyThread-1"); // 设置线程名称
String name = thread.getName(); // 获取线程名称
```

### 7.2 异常处理
```java
thread.setUncaughtExceptionHandler((t, e) -> {
    System.out.println("线程：" + t.getName() + " 发生异常：" + e.getMessage());
});
```

### 7.3 线程状态监控
```java
Thread.State state = thread.getState(); // 获取线程状态
```

## 8. 注意事项

### 8.1 避免直接调用run方法
```java
// 错误方式
thread.run(); // 这只是普通方法调用，不会创建新线程

// 正确方式
thread.start(); // 创建新线程并执行run方法
```

### 8.2 正确处理中断
```java
while (!Thread.currentThread().isInterrupted()) {
    try {
        // 线程执行的代码
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // 重新设置中断状态
        break;
    }
}
```

### 8.3 避免使用stop方法
```java
// 不推荐使用
thread.stop(); // 已废弃

// 推荐使用中断机制
thread.interrupt();
```

## 9. 面试要点

### 9.1 线程状态转换
- NEW -> RUNNABLE
- RUNNABLE <-> BLOCKED
- RUNNABLE <-> WAITING
- RUNNABLE <-> TIMED_WAITING
- Any -> TERMINATED

### 9.2 线程安全的实现方式
1. synchronized关键字
2. volatile关键字
3. 原子类
4. 锁机制
5. 线程安全集合

### 9.3 常见问题
1. 死锁产生的条件和预防
2. 线程池的使用场景
3. synchronized和volatile的区别
4. 线程通信的方式
5. 守护线程的特点
