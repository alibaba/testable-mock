package com.alibaba.testable.core.annotation;

import com.alibaba.testable.core.model.MockDiagnose;

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
     * switch of mock diagnose information of current test class
     * @return enable or disable
     */
    MockDiagnose diagnose() default MockDiagnose.DISABLE;

}
