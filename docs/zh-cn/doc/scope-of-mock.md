Mock的生效范围
---

在`@MockMethod`和`@MockConstructor`注解上都有一个`scope`参数，其可选值有两种

- `MockScope.GLOBAL`：该Mock方法将全局生效
- `MockScope.ASSOCIATED`：该Mock方法仅对Mock容器关联测试类中的测试用例生效

对于常规项目而言，单元测试里需要被Mock的调用都是由于其中包含了不需要或不便于测试的逻辑，譬如“依赖外部系统”、“包含随机结果”、“执行非常耗时”等等，这类调用在整个单元测试的生命周期里都应该被Mock方法置换，不论调用的发起者是谁。因此`TestableMock`默认所有Mock方法都是全局生效的，即`scope`默认值为`MockScope.GLOBAL`。

> 举例来说，`CookerService`和`SellerService`是两个需要被测试的类，假设`CookerService`的代码里的`hireXxx()`和`cookXxx()`方法都需要依赖外部系统。因此在进行单元测试时，开发者在`CookerService`关联的Mock容器里使用`@MockMethod`注解定义了这些调用的替代方法。
> 
> 此时，若该Mock方法的`scope`值为`MockScope.GLOBAL`，则不论是在`SellerServiceTest`测试类还是在`CookerServiceTest`测试类的测试用例，只要直接或间接的执行到这行调用，都会被置换为调用Mock方法。若该Mock方法的`scope`值为`MockScope.ASSOCIATED`，则Mock只对`CookerServiceTest`类中的测试用例生效，而`SellerServiceTest`类中的测试用例在运行过程中执行到了`CookerService`类的相关代码，将会执行原本的调用。
> 
> 参见Java和Kotlin示例中`SellerServiceTest`测试类的用例。

在一些大型项目中，会有“下层模块编写单元测试，上层模块编写端到端集成测试，两者混合在一起运行”的情况，这时候大部分Mock方法都应该使用`MockScope.ASSOCIATED`作为生效范围。针对这种情况，`TestableMock`支持通过`mock.scope.default`运行参数来修改默认的Mock方法生效范围，详见[全局运行参数](zh-cn/doc/javaagent-args.md)文档。

> 特别说明。若要Mock静态块里的调用，Mock方法的`scope`必须为`MockScope.GLOBAL`，因为静态块中的代码在程序初始化时就会执行，不属于任何测试用例。典型场景是在使用JNI开发的项目中Mock系统库的加载方法。
> ```java
> static {
>     System.loadLibrary("native-lib");
> }
> ```
> 若默认的`scope`参数不是`MockScope.GLOBAL`，则相应Mock方法应当显式的声明`scope`值，例如：
> ```java
> @MockMethod(targetClass = System.class, scope = MockScope.GLOBAL)
> private void loadLibrary(String libname) {
>     System.err.println("loadLibrary " + libname);
> }
> ```
