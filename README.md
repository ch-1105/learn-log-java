# Java Core Technology Learning Path

A comprehensive Java learning project covering core technologies, concurrency, collections, and JVM internals.

## 🎯 Learning Tracks

### 1. Java Virtual Machine (JVM)
- **Architecture**
  - JVM Components
  - Runtime Data Areas
  - Execution Engine
  
- **Memory Management**
  - Memory Model (JMM)
  - Object Memory Layout
  - Memory Leaks Prevention
  
- **Class Loading**
  - Loading Process
  - ClassLoader Hierarchy
  - Custom ClassLoaders
  - Common Issues & Solutions
  
- **Garbage Collection**
  - Modern GC Algorithms
  - GC Tuning
  - Performance Monitoring

### 2. Concurrency Programming
- **Thread Management**
  - Thread Lifecycle
  - Thread Pool Patterns
  - ThreadLocal Usage
  
- **Synchronization**
  - Locks and Monitors
  - Atomic Operations
  - Volatile Variables
  
- **Concurrent Collections**
  - ConcurrentHashMap
  - BlockingQueue
  - CopyOnWriteArrayList

### 3. Collections Framework
- **Core Collections**
  - List, Set, Map Implementations
  - Performance Characteristics
  - Usage Patterns
  
- **Algorithms**
  - Sorting
  - Searching
  - Custom Comparators

## 📚 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── ch/
│   │           ├── basic/
│   │           │   ├── jvm/
│   │           │   │   ├── doc/         # JVM Documentation
│   │           │   │   └── example/     # JVM Examples
│   │           │   ├── thread/          # Concurrency
│   │           │   └── collection/      # Collections
│   │           └── advanced/            # Advanced Topics
```

## 🛠️ Technical Stack

- **Java Version**: 17+ LTS
- **Build Tool**: Maven
- **Testing**: JUnit 5
- **Logging**: SLF4J + Logback
- **IDE**: IntelliJ IDEA

## 🚀 Getting Started

1. **Prerequisites**
   ```bash
   Java 17+
   Maven 3.6+
   ```

2. **Build**
   ```bash
   mvn clean install
   ```

3. **Run Examples**
   ```bash
   # JVM Examples
   java -XX:+UseG1GC com.ch.basic.jvm.example.GCDemo
   
   # Concurrency Examples
   java com.ch.basic.thread.example.ThreadPoolExample
   ```

## 📖 Documentation

Each topic includes:
- Markdown documentation explaining concepts
- Practical code examples
- Common pitfalls and solutions
- Performance considerations
- Interview preparation materials

## 🎓 Learning Path Progress

- [x] JVM Architecture
- [x] Memory Management
- [x] Class Loading
- [x] Garbage Collection
- [x] Thread Fundamentals
- [x] Synchronization
- [x] Collections Basics
- [ ] Advanced Concurrency
- [ ] Performance Tuning
- [ ] Microservices Patterns

## 📈 Performance Monitoring

- JVM GC Monitoring
- Thread Pool Metrics
- Memory Usage Analysis
- Class Loading Statistics

## 🤝 Contributing

Feel free to:
- Submit issues
- Propose new features
- Add documentation
- Share your learning experience

## 📝 License

This project is licensed under the MIT License.
