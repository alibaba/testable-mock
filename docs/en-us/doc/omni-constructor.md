Parameter Constructor
---

In unit testing, the preparation and construction of test data is a necessary and tedious task. Object-oriented layer-by-layer encapsulation becomes an obstacle to initializing the state of the object during testing. Especially when the type structure is complicated, there is no suitable construction method, or some fields need to use private inner classes, etc. Using conventional methods to construct those class often appear to be inadequate.

For this reason, `TestableMock` provides two minimalist tool classes, `OmniConstructor` and `OmniAccessor`, which makes the construction of any object no longer difficult.

### 1. Construct any object with one line of code

No matter how special the target type is, `OmniConstructor` will hand it to you immediately~~ The universal object constructor `OmniConstructor` has two static methods:

- `newInstance(<AnyClass>)` ➜ Specify any type, and return an object of that type
- `newArray(<AnyClass>, <ArraySize>)` ➜ Specify any type, and return an array of that type

Usage example:

```java
// Construct a object of "WhatEverClass" type
WhatEverClass obj = OmniConstructor.newInstance(WhatEverClass.class);
// Construct a array of "WhatEverClass[]" type with capability of 5
WhatEverClass[] arr = OmniConstructor.newArray(WhatEverClass.class, 5);
```

Beside that, object constructed by `OmniConstructor` is not just a simple empty object with all member values of `null`, but a "fullness" object in which all members and all sub-members of all members have been recursively initialized. Compared with using `new` operation, `OmniConstructor` can ensure the integrity of the object structure and avoid the `NullPointerException` problem caused by partial initialization of test data.

```java
// Construct object using new operation
Parent parent = new Parent();
// Inner member is not initialized, will cause NullPointerException (❌)
parent.getChild().getGrandChild();

// Construct object using OmniConstructor
Parent parent = OmniConstructor.newInstance(Parent.class);
// No need to worry, visit any child member safely (✅)
parent.getChild().getGrandChild().getContent();
```

> **Note 1**: In the current version, the member fields of type interface or abstract class will still be initialized to `null`, this problem will be fixed in the future.
>
> **Note 2**: Based on the light-weight principle, in the default mode, `OmniConstructor` will only uses the original constructor of the class to create objects. For POJO and most model layer objects, this mode has been able to meet the needs.
> But for more complex situations, such as when certain class have constructors throwing exceptions or contain other statements that hinder the normal execution of the construction, the object construction may fail.
> In those situations, you can use the [Testable global configuration](en-us/doc/javaagent-args.md) `omni.constructor.enhance.enable = true` to enable bytecode enhancement mode of `OmniConstructor`, in this mode, any Java class can be constructed.

In addition to use as input parameters of the method under test, `OmniConstructor` can also be used to quickly construct the return value of the mock method. Compared to using `null` as the return value of the mock method, using a fully initialized object can better guarantee the reliability of the test .

In the `DemoOmniMethodsTest` test class of the `java-demo` and `kotlin-demo` sample projects, it is shown in detail how `OmniConstructor` could be used when the target type has a multi-layered nested structure, the construction method is throwing exception, and even without public construction method available.

### 2. Access any inner member with one line of code

For test data, even with complex structure, it is usually only part of its attributes and states that are related to a specific test case. However, it is sometimes not easy to assign values to these fields deep wrapped in the object structure.

As an enhanced version of the `PrivateAccessor` tool, `OmniAccessor` is inspired by the [XPath node selector](https://www.w3schools.com/xml/xpath_syntax.asp) in the `XML` language, It provides two main static methods of `get` and `set`:

- `get(arbitrary object, "access path")` ➜ returns all member objects searched for based on path-matching
- `set(arbitrary object, "access path", new value)` ➜ Assign a value to any objects based on path-matching

There is also a `getFirst()` method used to directly obtain the unique target object during exact path matching. Its function is equivalent to `OmniAccessor.get(...).get(0)`:

- `getFirst(arbitrary object, "access path")` ➜ returns the first member object searched based on path-matching

You only need to write the access path that meets the rules, no matter what type and depth of members, you can directly reach them with one line of code:

```java
// Get all field of the parent object, which named as content and inside type GrandChild
OmniAccessor.get(parent, "{GrandChild}/content");
// Assign 100 to any fields which named as value and inside any child member that matches the 3rd item of the array named children
OmniAccessor.set(parent, "children[2]/*/value", 100);
```

The path rules are as follows:

**1. Matching member name**

The path name without additional decoration will match any member object with the same name

- `child`: match any descendant member whose name is `child`
- `child/grandChild`: matches the child member named `grandChild` among the descendants of the name `child`

**2. Matching member type**

Use curly braces to match the type name, usually used to obtain or assign multiple member objects of the same type in batches

- `{Child}`: match all descendants of `Child`
- `{Children[]}`: match all descendants of the `Children` array
- `{Child}/{GrandChild}`: match all descendant members of `Child`, all types are children of `GrandChild`

The member name and type can be mixed on the path (currently it is not supported to specify the member name and type at the same time in the same level path)

- `child/{GrandChild}`: match all descendant members whose name is `child`, all types are child members of `GrandChild`
- `{Child}/grandChild/content`: match all descendant members whose type is `Child`, the child members named `grandChild`, and the child members named `content`

**3. Use subscripts to access array members**

Use square brackets with numerical values to indicate that the matching position is an array type, and the object with the specified subscript is taken (without subscript, when the matching object is an array type, all objects in the array are matched)

- `children[1]/content`: match the descendant members of the array type named `children`, and take the child member named `content` in the `2`th object
- `parent/children[1]`: match the child member of the array type named `children` among the descendant members named `parent`, and take the `2`th object among them

**4. Use wildcards**

Wildcards can be used to match member names or type names

- `child*`: match all descendant members whose name starts with `child`
- `{*Child}`: match all descendant members whose type ends with `Child`
- `c*ld/{Grand*ld}`: match the descendant members whose name starts with `c` and ends with `ld`, and the members whose type starts with `Grand` and ends with `ld`
- `child/*/content`: At this time, `*` will match any member, that is, the child member of `content` contained in any child member of the `child` object

For details, see the use cases in the test classes of the `java-demo` and `kotlin-demo` sample projects `DemoOmniMethodsTest`.

### 3. Special instructions

> **Do you really need to use `OmniAccessor`? **
>
> `OmniAccessor` implement the basic anti-refactoring mechanism based on the Fail-Fast principle. When the access path provided by the user cannot match any member, the `OmniAccessor` will immediately throw a `NoSuchMemberError` error, so that the unit test is terminated early. However, compared to the conventional member access method, the support of `OmniAccessor` in IDE refactoring is still weak.
>
> For content assignment of complex objects, in most cases, we recommend using [Builder Pattern](https://www.geeksforgeeks.org/builder-pattern-in-java/), or exposing Getter/Setter method implementations. Although these conventional methods are slightly clumsy (especially when you need to assign values to many similar members in batches), they are more friendly to the encapsulation and reconstruction of business logic.
> Only when the original type is not suitable for transformation, and there is no other way to access the target member, `OmniAccessor` is the last resort.
