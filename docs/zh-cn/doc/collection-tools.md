常用集合构建工具
---

在编写测试数据的时候，经常会用到各种集合类型的对象，然而Java原生的集合类型需要反复调用`add()`或`put()`方法来添加数据，使代码显得十分冗长。

`TestableMock`借鉴了Kotlin语言简洁的集合构造方法，提供了一个实用的集合构造工具类`CollectionTool`，让Java集合对象的创建从此变得优雅。

这个工具类提供了以下全局静态方法：

## arrayOf

快速构造任意类型的数组，等效于Kotlin的`arrayOf()`方法。

用法示例：

```java
import static com.alibaba.testable.core.tool.CollectionTool.arrayOf;

// 构造一个字符串数组
String[] manyWords = arrayOf("this", "is", "an", "example");

// 构造一个浮点值数组
Double[] manyValues = arrayOf(1.0D, 2.0D, 3.0D);
```

## listOf

快速构造任意类型的列表，等效于Kotlin的`mutableListOf()`方法。

用法示例：

```java
import static com.alibaba.testable.core.tool.CollectionTool.listOf;

// 构造一个字符串列表
List<String> manyWords = listOf("this", "is", "an", "example");

// 构造一个浮点值列表        
List<Double> manyValues = listOf(1.0D, 2.0D, 3.0D);
```

## setOf

快速构造任意类型的集合，等效于Kotlin的`mutableSetOf()`方法。

用法示例：

```java
import static com.alibaba.testable.core.tool.CollectionTool.setOf;

// 构造一个字符串集合
Set<String> manyWords = setOf("this", "is", "an", "example");

// 构造一个浮点值集合
Set<Double> manyValues = setOf(1.0D, 2.0D, 3.0D);
```

## mapOf

快速构造任意类型的无序映射表，等效于Kotlin的`mutableMapOf()`方法。

用法示例：

```java
import static com.alibaba.testable.core.tool.CollectionTool.mapOf;
import static com.alibaba.testable.core.tool.CollectionTool.entryOf;

// 构造一个字符串到字符串的映射
Map<String, String> manyWords = mapOf(
    entryOf("language", "java"),
    entryOf("type", "unittest"),
    entryOf("library", "testable")
);

// 构造一个整型数值到浮点数值的映射
Map<Integer, Double> manyValues = mapOf(
    entryOf(1, 10.12D),
    entryOf(2, 20.24D),
    entryOf(3, 30.36D)
);
```

## orderedMapOf

快速构造任意类型的有序映射表（LinkedListMap），相较于`mapOf()`创建的HashMap对象，当在循环中遍历它的键或值列表时，其访问顺序始终会与构造时传给`orderedMapOf()`方法的数据顺序一致。

用法示例：

```java
import static com.alibaba.testable.core.tool.CollectionTool.orderedMapOf;
import static com.alibaba.testable.core.tool.CollectionTool.entryOf;

// 构造一个字符串到字符串的有序映射
Map<String, String> manyWords = orderedMapOf(
    entryOf("language", "java"),
    entryOf("type", "unittest"),
    entryOf("library", "testable")
);

// 构造一个整型数值到浮点数值的有序映射
Map<Integer, Double> manyValues = orderedMapOf(
    entryOf(1, 10.12D),
    entryOf(2, 20.24D),
    entryOf(3, 30.36D)
);
```

## entryOf

配合`mapOf()`和`orderedMapOf()`方法使用，用于构造映射表。

## slice

用于提取任意类型数组中间的一部分数据，组成新的数组。

用法示例：

```java
import static com.alibaba.testable.core.tool.CollectionTool.slice;
import static com.alibaba.testable.core.tool.CollectionTool.arrayOf;

// 提取指定字符串数组从下标1到3之间的元素，得到的新数组内容为["is", "a", "simple"]
String[] fullWords = arrayOf("this", "is", "a", "simple", "example");
String[] partOfWords = slice(fullWords, 1, 3);

// 只传入起始位置下标，提取该位置之后的所有元素，下例返回的新数组内容为[5, 8, 13, 21]
Integer[] fibonacci = arrayOf(1, 1, 2, 3, 5, 8, 13, 21);
Integer[] partOfFibonacci = slice(fibonacci, 4);
```
