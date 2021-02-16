package com.alibaba.testable.core.annotation;

import com.alibaba.testable.core.model.LogLevel;

import java.lang.annotation.*;

/**
 * Set extra mock parameter to test class
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MockDiagnose {

    /**
     * switch of mock diagnose information of current test class
     * @return enable or disable
     */
    LogLevel value() default LogLevel.DISABLE;

}
