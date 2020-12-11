package com.alibaba.testable.core.annotation;

import java.lang.annotation.*;

/**
 * Mark method as mock method
 * @deprecated will be remove in v0.5.0, use @MockMethod or @MockConstructor instead
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Deprecated
public @interface TestableMock {

    /**
     * mock specified method instead of method with same name
     * @return target method name
     */
    String targetMethod() default "";

}
