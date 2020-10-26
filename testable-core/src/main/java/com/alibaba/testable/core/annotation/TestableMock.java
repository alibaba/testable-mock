package com.alibaba.testable.core.annotation;

import com.alibaba.testable.core.model.MockType;

import java.lang.annotation.*;

/**
 * Use marked method to replace the ones in source class
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface TestableMock {

    /**
     * type of mock method
     */
    MockType value() default MockType.MEMBER_METHOD;

    /**
     * mock specified method instead of method with same name
     */
    String targetMethod() default "";

}
