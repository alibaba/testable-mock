package com.alibaba.testable.core.annotation;

import java.lang.annotation.*;

/**
 * Use marked method to replace the ones in source class
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface TestableInject {

    /**
     * mock method of specified class instead of the class under test
     */
    Class targetClass() default Object.class;

    /**
     * mock specified method instead of method with same name
     */
    String targetMethod() default "";

}
