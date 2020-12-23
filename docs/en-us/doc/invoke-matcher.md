Verify Mock Invocation
---

In the test, in addition to replacing **methods contain external dependencies** with mocks, it is often necessary to verify whether the actual parameters of mock invocation are in accordance with expectations.

The **verifiers** and **matchers** are provided in `TestableMock` to achieve this function. for example:

```java
@Test
public test_case() {
    int res = insToTest.methodToTest();
    verify("mockMethod").with(123, "abc");
}
```

This use case will check whether the mock method named `mockMethod` has been called when the method under test `methodToTest()` is executed, and whether the parameter values received during the call are `123` and `"abc"` (Assuming that the `mockMethod` method has two parameters).

In addition to this simple verification, `TestableMock` currently supports a variety of **validators**, as well as **matchers** that can fuzzy match parameter characteristics.

The `DemoMatcherTest` test classes in the sample projects `java-demo` and `kotlin-demo` show the usage of these validators and matchers in detail.

## Basic validator

- `with(Object...args)` → verify whether the method has been called by the specified parameters
- `withInOrder(Object... args)` → verify the specified method is called with specified parameters according to the actual calling order
- `withTimes(int expectedCount)` → verify whether the method has been called the specified number of times, ignoring the check of the calling parameters
- `without(Object...args)` → verification method has never been called with specified parameters
- `times(int count)` → use after the `with()` or `withInOrder()` method to verify that the method has been called the specified number of times with the same conditions

## Basic matcher

- `any()` → matches any value, including `null`
- `any(Class<?> clazz)` → match any value of the specified type or subtype
- `anyTypeOf(Class<?>... classes)` → match any type of value in the list
- `anyString()` → matches any string
- `anyNumber()` → matches any number (integer, long, float, double, etc)
- `anyBoolean()` → matches any Boolean value
- `anyByte()` → match any single byte type value
- `anyChar()` → matches any single character type value
- `anyInt()` → matches any integer value
- `anyLong()` → matches any value of long integer type
- `anyFloat()` → match any value of float type
- `anyDouble()` → match any double-precision float number type value
- `anyShort()` → matches any value of short integer type
- `anyArray()` → matches any array
- `anyArrayOf(Class<?> clazz)` → matches any array of the specified type
- `anyList()` → matches any list
- `anyListOf(Class<?> clazz)` → matches any list of the specified type
- `anySet()` → matches any set
- `anySetOf(Class<?> clazz)` → matches any set of the specified type
- `anyMap()` → match any map
- `anyMapOf(Class<?> keyClass, Class<?> valueClass)` → match any map of the specified type
- `anyCollection()` → match any container
- `anyCollectionOf(Class<?> clazz)` → matches any specified type of container
- `anyIterable()` → matches any iterator
- `anyIterableOf(Class<?> clazz)` → matches any iterator of the specified type
- `eq(Object obj)` → match objects equal to the specified value
- `refEq(Object obj)` → match the specified object (not equal to the value, but the same object)

## Null value matcher

- `isNull()` → match `null`
- `notNull()` → matches any value except `null`
- `nullable(Class<?> clazz)` → matches any value of `null` or specified type

## String matcher

- `contains(String substring)` → match a string containing a specific substring
- `matches(String regex)` → match strings that match the specified regular expression
- `endsWith(String suffix)` → match the string ending with the specified substring
- `startsWith(String prefix)` → match the string starting with the specified substring

## Universal matcher

- `any(MatchFunction matcher)` → match the value that matches the specified expression
