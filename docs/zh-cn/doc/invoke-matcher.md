校验Mock调用
---

在测试中，除了需要将某些含有外部依赖的方法替换为Mock，经常还会需要验证该方法被调用时的参数是否符合预期。

在TestableMock中提供了校验器（verifier）和匹配器（matcher）来实现这一功能。譬如：

```java
@Test
public test_case() {
    int res = insToTest.methodToTest();
    verify("mockMethod").with(123, "abc");
}
```

这个用例会检查在执行被测方法`methodToTest()`时，名称是`mockMethod`的Mock方法应当被调用过，且调用时收到的参数值为123和"abc"（假设被Mock的`mockMethod`方法有两个参数）。

除了这种简单校验以外，TestableMock当前已经支持了多种**校验器**，以及能够模糊匹配参数特征的**匹配器**。

在示例项目`java-demo`和`kotlin-demo`中的`DemoMatcherTest`测试类详细展示了这些校验器和匹配器的用法。

## 基本校验器

- `with(Object... args)` → 验证方法是否被指定参数调用过
- `withInOrder(Object... args)` → 如果指定方法被调用了多次，依据实际调用顺序依次匹配
- `withTimes(int expectedCount)` → 验证方法是否被调用过指定次数，忽略对调用参数的检查
- `without(Object... args)` → 验证方法从未被使用指定参数调用过
- `times(int count)` → 连在`with()`或`withInOrder()`方法之后使用，验证该方法被同样条件的参数调用过了指定次数

## 基本匹配器

- `any()` → 匹配任何值，包括Null
- `any(Class<?> clazz)` → 匹配任何指定类型或子类型的值
- `anyTypeOf(Class<?>... classes)` → 匹配在列表中的任意一种类型的值
- `anyString()` → 匹配任何字符串
- `anyNumber()` → 匹配任何数值（整数或浮点数）
- `anyBoolean()` → 匹配任何布尔值
- `anyByte()` → 匹配任何单字节类型的值
- `anyChar()` → 匹配任何单字符类型的值
- `anyInt()` → 匹配任何整数类型的值
- `anyLong()` → 匹配任何长整数类型的值
- `anyFloat()` → 匹配任何浮点数类型的值
- `anyDouble()` → 匹配任何双精度浮点数类型的值
- `anyShort()` → 匹配任何短整数类型的值
- `anyArray()` → 匹配任何数组
- `anyArrayOf(Class<?> clazz)` → 匹配任何指定类型的数组
- `anyList()` → 匹配任何列表
- `anyListOf(Class<?> clazz)` → 匹配任何指定类型的列表
- `anySet()` → 匹配任何集合
- `anySetOf(Class<?> clazz)` → 匹配任何指定类型的集合
- `anyMap()` → 匹配任何映射
- `anyMapOf(Class<?> keyClass, Class<?> valueClass)` → 匹配任何指定类型的映射
- `anyCollection()` → 匹配任何容器
- `anyCollectionOf(Class<?> clazz)` → 匹配任何指定类型的容器
- `anyIterable()` → 匹配任何迭代器
- `anyIterableOf(Class<?> clazz)` → 匹配任何指定类型的迭代器
- `eq(Object obj)` → 匹配与指定值相等的对象
- `refEq(Object obj)` → 匹配指定对象（非值相等，而是就是同一个对象）

## 空值匹配器

- `isNull()` → 匹配Null
- `notNull()` → 匹配除Null以外的任何值
- `nullable(Class<?> clazz)` → 匹配空或指定类型的任何值

## 字符串匹配器

- `contains(String substring)` → 匹配包含特定子串的字符串
- `matches(String regex)` → 匹配符合指定正则表达式的字符串
- `endsWith(String suffix)` → 匹配以指定子串结尾的字符串
- `startsWith(String prefix)` → 匹配以指定子串开头的字符串

## 万能匹配器

- `any(MatchFunction matcher)` → 匹配符合指定表达式的值
