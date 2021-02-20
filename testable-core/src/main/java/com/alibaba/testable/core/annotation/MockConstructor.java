package com.alibaba.testable.core.annotation;

import com.alibaba.testable.core.model.MockScope;

import java.lang.annotation.*;

/**
 * Mark method as mock constructor
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MockConstructor {

    /**
     * specify the effective scope of the mock method
     * @return global or associated
     */
    MockScope scope() default MockScope.GLOBAL;

}
