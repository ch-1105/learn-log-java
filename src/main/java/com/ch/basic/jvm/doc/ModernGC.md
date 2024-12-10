# 现代JVM垃圾回收机制

## 一、垃圾回收的演进

### 1.1 历史演进
1. **早期**：Serial/Serial Old - 单线程收集器
2. **发展期**：ParNew/CMS - 并发收集器
3. **现代**：G1 - 区域化分代收集器
4. **未来**：ZGC/Shenandoah - 低延迟收集器

### 1.2 现代GC关注点
1. **低延迟**：毫秒级甚至亚毫秒级的停顿时间
2. **高吞吐**：更高的CPU利用效率
3. **自适应**：自动调节，降低人工配置成本
4. **大内存**：支持TB级别的堆内存

## 二、主流垃圾收集器

### 2.1 G1（Garbage First）收集器
- **JDK 9开始作为默认收集器**
- **特点**：
  1. 区域化分代收集
  2. 可预测的停顿时间
  3. 适合大内存应用（4GB-16GB）

```bash
# G1常用参数
-XX:+UseG1GC                  # 使用G1收集器
-XX:MaxGCPauseMillis=200     # 最大停顿时间目标
-XX:G1HeapRegionSize=n       # 区域大小（1MB-32MB，必须是2的幂）
```

### 2.2 ZGC（Z Garbage Collector）
- **JDK 15开始正式商用**
- **特点**：
  1. 停顿时间不超过10ms
  2. 支持TB级堆内存
  3. 与内存大小无关的停顿时间
  4. 适合低延迟应用

```bash
# ZGC常用参数
-XX:+UseZGC                  # 使用ZGC收集器
-XX:ZAllocationSpikeTolerance # 内存分配速率容忍度
-XX:ZCollectionInterval      # ZGC触发间隔
```

### 2.3 Shenandoah
- **RedHat开发，JDK 12引入**
- **特点**：
  1. 与ZGC类似的低延迟
  2. 更激进的并发策略
  3. 适合对延迟敏感的应用

```bash
# Shenandoah常用参数
-XX:+UseShenandoahGC        # 使用Shenandoah收集器
-XX:ShenandoahGCMode=passive/adaptive/aggressive
```

## 三、现代GC最佳实践

### 3.1 选择合适的GC
```
┌────────────────────────────────────┐
│            应用场景                │
├────────────────┬───────────────────┤
│   G1           │  通用场景         │
│   ZGC          │  低延迟要求       │
│   Shenandoah   │  极限低延迟要求   │
└────────────────┴───────────────────┘
```

### 3.2 JDK版本选择建议
- **JDK 17 LTS**：长期支持版本，G1已经非常成熟
- **JDK 21 LTS**：ZGC性能显著提升，生产环境可用

### 3.3 内存设置建议
```bash
# 通用设置
-Xms=Xmx               # 避免堆内存动态调整
-XX:+UseContainerSupport  # 容器环境支持
-XX:MaxRAMPercentage=75.0 # 最大堆内存比例
```

## 四、GC调优现代方法

### 4.1 自适应调优
- 让GC自己做决策
- 减少人工参数配置
- 专注于监控而非调优

### 4.2 监控指标
1. **延迟指标**
   - GC停顿时间分布
   - 请求响应时间

2. **吞吐量指标**
   - GC时间占比
   - 应用吞吐量

3. **内存指标**
   - 堆内存使用趋势
   - 对象分配速率

### 4.3 现代化工具
1. **JDK Flight Recorder (JFR)**
   ```bash
   # 开启JFR
   -XX:+FlightRecorder
   -XX:StartFlightRecording
   ```

2. **JDK Mission Control (JMC)**
   - 可视化分析JFR数据
   - 实时监控GC行为

3. **Prometheus + Grafana**
   - 监控JVM指标
   - 可视化展示

## 五、常见问题处理

### 5.1 内存泄漏
1. **使用JFR进行诊断**
2. **观察内存分配热点**
3. **分析对象生命周期**

### 5.2 性能调优
1. **优先使用默认配置**
2. **根据监控指标微调**
3. **关注业务代码优化**

### 5.3 升级建议
1. **从CMS迁移到G1**
   ```bash
   # 1. 先使用默认参数
   -XX:+UseG1GC
   
   # 2. 如果需要，再微调
   -XX:MaxGCPauseMillis=200
   ```

2. **从G1迁移到ZGC**
   ```bash
   # 1. 确保JDK版本 >= 15
   # 2. 切换到ZGC
   -XX:+UseZGC
   ```

## 六、最佳实践总结

### 6.1 通用原则
1. 优先使用默认GC（G1）
2. 特殊场景才考虑ZGC/Shenandoah
3. 保持JDK版本最新（使用LTS版本）

### 6.2 监控建议
1. 部署JFR持续监控
2. 设置合适的告警阈值
3. 定期回顾GC日志

### 6.3 调优建议
1. 从业务代码优化开始
2. 相信GC的自适应能力
3. 保持简单，避免过度调优
