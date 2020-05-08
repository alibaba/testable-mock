package com.alibaba.testable.annotation;

import java.lang.annotation.*;

/**
 * On type, make all methods in the class testable
 * On method, make the method testable
 * On field (in test class), fit the variable for unit test
 *
 * @author linfan
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface Testable {

}
