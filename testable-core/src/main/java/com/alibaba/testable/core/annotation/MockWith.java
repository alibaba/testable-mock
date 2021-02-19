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
     * @note this parameter will become mandatory in v0.6
     * @return type of mock class
     */
    Class<?> value() default NullType.class;

    /**
     * treat current class as a source class or test class
     * @return type of current class
     */
    ClassType treatAs() default ClassType.GuessByName;

    /**
     * switch of mock diagnose information of current test class
     * @deprecated to be removed in v0.6, use @MockDiagnose annotation instead
     * @return enable or disable
     */
    LogLevel diagnose() default LogLevel.DISABLE;

}
