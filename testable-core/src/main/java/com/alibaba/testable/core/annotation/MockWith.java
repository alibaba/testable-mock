package com.alibaba.testable.core.annotation;

import com.alibaba.testable.core.model.ClassType;
import com.alibaba.testable.core.model.LogLevel;

import javax.lang.model.type.NullType;
import java.lang.annotation.*;

/**
 * Set extra mock parameter to test class
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MockWith {

    /**
     * explicitly specify mock class
     * @return type of mock class
     */
    Class<?> value() default NullType.class;

    /**
     * treat current class as a source class or test class
     * @return type of current class
     */
    ClassType treatAs() default ClassType.GuessByName;

}
