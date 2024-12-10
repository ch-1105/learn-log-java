# 现代GC面试题和核心机制

## 一、核心概念解析

### 1.1 为什么需要GC？
- **手动内存管理的问题**
  - 容易造成内存泄漏
  - 容易出现悬挂指针
  - 开发效率低
- **自动GC的优势**
  - 自动识别垃圾对象
  - 自动回收内存
  - 防止内存泄漏
  - 提高开发效率

### 1.2 判断对象存活的方式
```
┌─────────────────┐
│    GC Roots     │
├─────────────────┤
│ 1.虚拟机栈引用   │
│ 2.静态变量引用   │
│ 3.常量引用      │
│ 4.JNI引用      │
└─────────────────┘
```

## 二、现代GC面试重点

### 2.1 G1收集器（默认）

#### 工作原理
```
┌──────┬──────┬──────┐
│ Eden │ Eden │ Eden │
├──────┼──────┼──────┤
│ Surv │ Old  │ Old  │
├──────┼──────┼──────┤
│ Old  │ Old  │ Hum  │
└──────┴──────┴──────┘
```

#### 面试要点
1. **为什么G1比CMS更好？**
   - 可预测的停顿时间
   - 更少的内存碎片
   - 支持并发标记和混合回收

2. **G1如何实现可预测的停顿时间？**
   - Region划分（默认2048个）
   - 停顿预测模型
   - 优先回收价值最大的Region

3. **G1的回收过程？**
```
Young GC → Concurrent Mark → Mixed GC
```

### 2.2 ZGC（低延迟）

#### 工作原理
```
┌─────────────────┐
│  Colored Pointers│
└─────────────────┘
        ↓
┌─────────────────┐
│   Load Barrier  │
└─────────────────┘
        ↓
┌─────────────────┐
│ Concurrent Mark │
└─────────────────┘
```

#### 面试要点
1. **ZGC如何实现低延迟？**
   - 读屏障技术
   - 着色指针
   - 并发处理

2. **什么场景选择ZGC？**
   - 对延迟敏感的应用
   - 大内存应用（TB级）
   - 高并发场景

## 三、GC调优实战

### 3.1 常见问题及解决方案

#### 1. Full GC频繁
```
原因：
- 内存分配速率过高
- 存活对象过多
- 内存碎片化

解决：
- 增加年轻代大小
- 优化对象生命周期
- 使用G1或ZGC
```

#### 2. GC停顿时间过长
```
原因：
- 堆内存过大
- 存活对象过多
- 使用了STW收集器

解决：
- 使用ZGC
- 减小堆内存
- 优化对象分配
```

### 3.2 调优参数速查

```bash
# G1调优
-XX:G1HeapRegionSize=n      # Region大小
-XX:MaxGCPauseMillis=n      # 最大停顿时间
-XX:G1NewSizePercent=n      # 新生代最小比例
-XX:G1MaxNewSizePercent=n   # 新生代最大比例

# ZGC调优
-XX:+UseZGC                 # 启用ZGC
-XX:ZCollectionInterval=n   # GC间隔
-XX:ZAllocationSpikeTolerance=n # 分配容忍度
```

## 四、性能监控和分析

### 4.1 JFR关键指标
```
1. Allocation
   - Object Count
   - Allocation Rate
   - TLABs

2. GC Events
   - Pause Time
   - Concurrent Time
   - Memory Usage

3. Thread
   - CPU Usage
   - Lock Contention
```

### 4.2 GC日志分析
```
[0.234s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause)
[0.234s][info][gc] GC(1) User=0.00s Sys=0.00s Real=0.00s
```

## 五、最佳实践案例

### 5.1 微服务应用
```
推荐配置：
-XX:+UseG1GC
-Xms=Xmx=4g
-XX:MaxGCPauseMillis=200
```

### 5.2 低延迟应用
```
推荐配置：
-XX:+UseZGC
-Xms=Xmx=8g
-XX:+UseTransparentHugePages
```

## 六、面试高频问题

### 6.1 基础概念
1. **什么是Stop-The-World？**
   - 所有应用线程暂停
   - GC线程执行收集
   - 现代GC尽量减少STW

2. **什么是垃圾回收的三色标记法？**
   - 白色：未被访问
   - 灰色：已访问但引用未扫描
   - 黑色：已访问且引用已扫描

### 6.2 实践问题
1. **如何处理内存泄漏？**
   ```java
   // 常见场景
   - 集合类泄漏
   - 监听器泄漏
   - ThreadLocal泄漏
   
   // 解决方案
   - 使用弱引用
   - 及时清理资源
   - 使用try-with-resources
   ```

2. **如何选择合适的GC？**
   ```
   G1：通用场景，4GB-16GB堆
   ZGC：低延迟要求，16GB+堆
   Parallel：批处理，注重吞吐量
   ```

### 6.3 调优问题
1. **GC调优的步骤是什么？**
   ```
   1. 确定目标
   2. 收集数据
   3. 分析问题
   4. 调整参数
   5. 验证效果
   ```

2. **如何避免频繁Full GC？**
   ```
   1. 合理设置堆大小
   2. 及时释放无用对象
   3. 使用软引用/弱引用
   4. 避免大对象分配
   ```

## 七、未来趋势

### 7.1 GC发展方向
1. **更低延迟**
   - 毫秒级→微秒级
   - 并发处理提升

2. **智能化**
   - 自适应调节
   - 机器学习优化

3. **新技术**
   - Generational ZGC
   - 弹性堆
   - Region合并
