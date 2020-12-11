package com.alibaba.testable.core.annotation;

import java.lang.annotation.*;

/**
 * Mark method as mock method
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MockMethod {

    /**
     * mock specified method instead of method with same name
     * @return target method name
     */
    String targetMethod() default "";

}
