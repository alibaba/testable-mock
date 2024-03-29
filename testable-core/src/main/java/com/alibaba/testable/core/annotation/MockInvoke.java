package com.alibaba.testable.core.annotation;

import com.alibaba.testable.core.model.MockScope;

import javax.lang.model.type.NullType;
import java.lang.annotation.*;

/**
 * Mark method as mock method
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MockInvoke {

    /**
     * mock specified method instead of method with same name
     * @return target method name
     */
    String targetMethod() default "";

    /**
     * explicit set target class instead of adding to parameter list
     * @return target class type
     */
    Class<?> targetClass() default NullType.class;

    /**
     * alternative to 'targetClass' field for private internal class
     * @return target class full name
     */
    String targetClassName() default "";

    /**
     * specify the effective scope of the mock method
     * @return global or associated
     */
    MockScope scope() default MockScope.GLOBAL;

}
