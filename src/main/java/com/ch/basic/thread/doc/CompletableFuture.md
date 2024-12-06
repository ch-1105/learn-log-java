# CompletableFuture异步编程详解

## 1. 基本概念

### 1.1 Future接口的局限性
- 不能手动完成
- 不能链式操作
- 无法合并多个Future
- 无法处理异常

### 1.2 CompletableFuture的优势
- 手动完成计算
- 支持链式调用
- 支持组合操作
- 提供异常处理
- 支持回调函数

## 2. 创建CompletableFuture

### 2.1 静态工厂方法
```java
// 创建已完成的Future
CompletableFuture<String> future = CompletableFuture.completedFuture("result");

// 创建异步计算
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "result");

// 创建无返回值的异步计算
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> System.out.println("done"));
```

### 2.2 自定义线程池
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "result", executor);
```

## 3. 链式操作

### 3.1 转换结果（thenApply, thenApplyAsync）
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World");
```

### 3.2 消费结果（thenAccept, thenRun）
```java
future.thenAccept(System.out::println);
future.thenRun(() -> System.out.println("Done"));
```

### 3.3 组合操作（thenCompose, thenCombine）
```java
// 串行操作
future1.thenCompose(result -> future2);

// 并行操作
future1.thenCombine(future2, (result1, result2) -> combinedResult);
```

## 4. 异常处理

### 4.1 处理异常（exceptionally）
```java
future.exceptionally(throwable -> "默认值");
```

### 4.2 完整异常处理（handle）
```java
future.handle((result, throwable) -> {
    if (throwable != null) {
        return "发生异常";
    }
    return result;
});
```

### 4.3 whenComplete
```java
future.whenComplete((result, throwable) -> {
    if (throwable != null) {
        System.err.println("发生异常: " + throwable);
    } else {
        System.out.println("结果: " + result);
    }
});
```

## 5. 多任务组合

### 5.1 allOf - 等待所有完成
```java
CompletableFuture<Void> all = CompletableFuture.allOf(future1, future2, future3);
```

### 5.2 anyOf - 等待任意一个完成
```java
CompletableFuture<Object> any = CompletableFuture.anyOf(future1, future2, future3);
```

### 5.3 自定义组合
```java
// 并行执行多个任务并收集结果
List<CompletableFuture<String>> futures = tasks.stream()
    .map(task -> CompletableFuture.supplyAsync(() -> task.process()))
    .collect(Collectors.toList());

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
    .thenApply(v -> futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList()));
```

## 6. 最佳实践

### 6.1 超时处理
```java
future.orTimeout(1, TimeUnit.SECONDS)
      .exceptionally(throwable -> "超时默认值");
```

### 6.2 默认值
```java
future.completeOnTimeout("默认值", 1, TimeUnit.SECONDS);
```

### 6.3 异步与同步选择
- thenApply vs thenApplyAsync
- thenAccept vs thenAcceptAsync
- thenRun vs thenRunAsync

### 6.4 线程池管理
- 使用自定义线程池
- 合理设置线程池参数
- 注意资源释放

## 7. 实际应用场景

### 7.1 并行API调用
```java
CompletableFuture<UserInfo> userInfo = CompletableFuture.supplyAsync(() -> getUserInfo());
CompletableFuture<OrderInfo> orderInfo = CompletableFuture.supplyAsync(() -> getOrderInfo());

CompletableFuture.allOf(userInfo, orderInfo)
    .thenAccept(v -> {
        // 处理所有结果
    });
```

### 7.2 异步任务链
```java
CompletableFuture.supplyAsync(() -> readData())
    .thenApplyAsync(data -> processData(data))
    .thenAcceptAsync(result -> saveResult(result))
    .exceptionally(throwable -> {
        handleError(throwable);
        return null;
    });
```

### 7.3 超时重试机制
```java
public <T> CompletableFuture<T> withRetry(Supplier<T> supplier, int maxRetries) {
    return CompletableFuture.supplyAsync(supplier)
        .orTimeout(1, TimeUnit.SECONDS)
        .exceptionally(throwable -> {
            if (maxRetries > 0) {
                return withRetry(supplier, maxRetries - 1).join();
            }
            throw new CompletionException(throwable);
        });
}
```

## 8. 面试常见问题

### 8.1 基础概念
1. CompletableFuture与Future的区别？
2. CompletableFuture的优势是什么？
3. 如何处理CompletableFuture的异常？

### 8.2 实践应用
1. 如何实现并行API调用？
2. 如何处理超时情况？
3. 异步方法和同步方法的选择？

### 8.3 性能优化
1. 如何正确使用线程池？
2. 如何避免常见的性能陷阱？
3. 如何处理复杂的异步任务链？
