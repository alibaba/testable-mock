package com.alibaba.testable.processor.annotation;

import javax.lang.model.type.NullType;
import java.lang.annotation.*;

/**
 * Make test class able to access private field and method in source class
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EnablePrivateAccess {

    /**
     * explicit specify the source class to be tested
     * @return
     */
    Class<?> srcClass() default NullType.class;

    /**
     * whether enable compile-time existence verification for the private members accessed
     * @return
     */
    boolean verifyTargetOnCompile() default true;

}
