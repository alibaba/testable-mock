Collection Tools
---

During test data preparation, various collection types are often used. However, collection data initialization in Java needs series of redundant call to the `add()` or `put()` method, making the code very tedious.

Inspired by the collection operations of Kotlin language, `TestableMock` draws on a set of concise collection construction method, which makes the creation of Java collection objects elegant.

The utility class `CollectionTool` provides following global static methods:

## arrayOf

Quickly construct an array of any type, equivalent to Kotlin's `arrayOf()` method.

Usage example:

```java
import static com.alibaba.testable.core.tool.CollectionTool.arrayOf;

// Construct a string array
String[] manyWords = arrayOf("this", "is", "an", "example");

// Construct a double array
Double[] manyValues = arrayOf(1.0D, 2.0D, 3.0D);
```

## listOf

Quickly construct an list of any type, equivalent to Kotlin's `mutableListOf()` method.

Usage example:

```java
import static com.alibaba.testable.core.tool.CollectionTool.listOf;

// Construct a string list
List<String> manyWords = listOf("this", "is", "an", "example");

// Construct a double list        
List<Double> manyValues = listOf(1.0D, 2.0D, 3.0D);
```

## setOf

Quickly construct an set of any type, equivalent to Kotlin's `mutableSetOf()` method.

Usage example:

```java
import static com.alibaba.testable.core.tool.CollectionTool.setOf;

// Construct a string set
Set<String> manyWords = setOf("this", "is", "an", "example");

// Construct a double set
Set<Double> manyValues = setOf(1.0D, 2.0D, 3.0D);
```

## mapOf

Quickly construct an map of any type, equivalent to Kotlin's `mutableMapOf()` method.

Usage example:

```java
import static com.alibaba.testable.core.tool.CollectionTool.mapOf;
import static com.alibaba.testable.core.tool.CollectionTool.entryOf;

// Construct a string to string map
Map<String, String> manyWords = mapOf(
    entryOf("language", "java"),
    entryOf("type", "unittest"),
    entryOf("library", "testable")
);

// Construct a integer to double map
Map<Integer, Double> manyValues = mapOf(
    entryOf(1, 10.12D),
    entryOf(2, 20.24D),
    entryOf(3, 30.36D)
);
```

## orderedMapOf

Quickly construct ordered map (LinkedListMap) of any type. Compared with the HashMap map created by `mapOf()`, when traversing its keys or values in a loop, the order will always be the same as entries passed to `orderedMapOf()` method during construction.

Usage example:

```java
import static com.alibaba.testable.core.tool.CollectionTool.orderedMapOf;
import static com.alibaba.testable.core.tool.CollectionTool.entryOf;

// Construct a string to string ordered map
Map<String, String> manyWords = orderedMapOf(
    entryOf("language", "java"),
    entryOf("type", "unittest"),
    entryOf("library", "testable")
);

// Construct a integer to double ordered map
Map<Integer, Double> manyValues = orderedMapOf(
    entryOf(1, 10.12D),
    entryOf(2, 20.24D),
    entryOf(3, 30.36D)
);
```

## entryOf

Used with `mapOf()` and `orderedMapOf()` methods to construct a map.

## slice

Used to extract a part of data from any type of array to a new array.

Usage example:

```java
import static com.alibaba.testable.core.tool.CollectionTool.slice;
import static com.alibaba.testable.core.tool.CollectionTool.arrayOf;

// Extract the elements of the specified string array from subscript 1 to 3
// the returned new array content are ["is", "a", "simple"]
String[] fullWords = arrayOf("this", "is", "a", "simple", "example");
String[] partOfWords = slice(fullWords, 1, 3);

// If only given the index of starting position, all elements after this position are extracted
// the new array returned in the following example is [5, 8, 13, 21]
Integer[] fibonacci = arrayOf(1, 1, 2, 3, 5, 8, 13, 21);
Integer[] partOfFibonacci = slice(fibonacci, 4);
```
