# Java类加载机制详解

## 一、类加载的生命周期

### 1.1 完整的类加载过程
```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  加载   │ → │  验证   │ → │  准备   │ → │  解析   │ → │  初始化  │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
                    └────────── 链接 ──────────┘
```

### 1.2 各阶段详解

#### 1. 加载（Loading）
- 通过类的全限定名获取二进制字节流
- 将字节流所代表的静态存储结构转化为方法区的运行时数据结构
- 在内存中生成Class对象，作为访问入口

#### 2. 验证（Verification）
- 文件格式验证
- 元数据验证
- 字节码验证
- 符号引用验证

#### 3. 准备（Preparation）
- 为类变量分配内存
- 设置初始值（零值）
```java
public static int value = 123;  // 准备阶段设置为0，初始化阶段才设置为123
```

#### 4. 解析（Resolution）
- 将符号引用替换为直接引用
- 解析类或接口、字段、方法等引用

#### 5. 初始化（Initialization）
- 执行类构造器<clinit>()方法
- 初始化类变量和静态代码块

## 二、类加载器

### 2.1 类加载器层次结构
```
┌─────────────────────┐
│  Bootstrap ClassLoader │ (C++实现)
└─────────────────────┘
           ↑
┌─────────────────────┐
│  Platform ClassLoader │ (JDK 9+)
└─────────────────────┘
           ↑
┌─────────────────────┐
│   System ClassLoader  │
└─────────────────────┘
           ↑
┌─────────────────────┐
│   User ClassLoader    │
└─────────────────────┘
```

### 2.2 现代JDK的改进
- JDK 9引入模块系统
- 重命名类加载器（Extension → Platform）
- 内部类加载器模块化

## 三、类加载机制的特点

### 3.1 双亲委派模型
```java
protected Class<?> loadClass(String name, boolean resolve) {
    // 1. 检查类是否已加载
    Class<?> c = findLoadedClass(name);
    if (c == null) {
        // 2. 委派给父加载器
        try {
            c = parent.loadClass(name);
        } catch (ClassNotFoundException e) {
            // 3. 父加载器失败，自己尝试加载
            c = findClass(name);
        }
    }
    return c;
}
```

### 3.2 打破双亲委派
- SPI机制（Service Provider Interface）
- OSGi框架
- Java 9模块系统

## 四、模块系统（Java 9+）

### 4.1 模块定义
```java
module com.example.myapp {
    requires java.base;        // 必需的依赖
    requires transitive java.sql;  // 传递依赖
    exports com.example.api;   // 导出包
    provides Service with Impl;  // 服务提供
}
```

### 4.2 模块特性
- 强封装
- 可靠配置
- 增强安全性

## 五、常见问题解析

### 5.1 类加载相关问题

1. **NoClassDefFoundError vs ClassNotFoundException**
```
NoClassDefFoundError：类存在但加载失败
ClassNotFoundException：类不存在
```

2. **类的初始化时机**
- 创建类的实例
- 访问类的静态变量（非常量）
- 调用类的静态方法
- 反射调用类
- 初始化子类时
- 作为启动类时

### 5.2 类加载器问题

1. **上下文类加载器**
```java
Thread.currentThread().getContextClassLoader();
Thread.currentThread().setContextClassLoader(loader);
```

2. **热部署实现**
- 自定义类加载器
- 监控类文件变化
- 重新加载类

## 六、最佳实践

### 6.1 自定义类加载器
```java
public class CustomClassLoader extends ClassLoader {
    @Override
    protected Class<?> findClass(String name) {
        // 1. 获取类文件字节数组
        byte[] classBytes = loadClassBytes(name);
        // 2. 调用defineClass方法
        return defineClass(name, classBytes, 0, classBytes.length);
    }
}
```

### 6.2 模块化应用设计
```
project
├── app
│   └── module-info.java
├── core
│   └── module-info.java
└── api
    └── module-info.java
```

## 七、性能优化

### 7.1 类加载优化
1. **预加载**
   - 启动时加载关键类
   - 避免运行时延迟

2. **懒加载**
   - 按需加载非关键类
   - 减少启动时间

### 7.2 动态加载优化
1. **并行加载**
   - 多线程加载独立类
   - 提高加载效率

2. **缓存优化**
   - 缓存常用类
   - 避免重复加载

## 八、面试重点

### 8.1 基础概念
1. **类加载过程的线程安全性**
2. **类加载器的命名空间**
3. **类的唯一性判定**

### 8.2 实践问题
1. **如何实现热部署？**
2. **如何解决类加载器泄漏？**
3. **模块系统的优势是什么？**

### 8.3 性能问题
1. **类加载对性能的影响**
2. **如何优化类加载性能**
3. **动态加载的最佳实践**
