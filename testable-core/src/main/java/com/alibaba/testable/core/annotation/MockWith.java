package com.alibaba.testable.core.annotation;

import com.alibaba.testable.core.model.MockDiagnose;

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
     * treat current class as a source class
     * @return true - source file, false - test file
     */
    boolean isSrc() default false;

    /**
     * switch of mock diagnose information of current test class
     * @return enable or disable
     */
    MockDiagnose diagnose() default MockDiagnose.DISABLE;

}
