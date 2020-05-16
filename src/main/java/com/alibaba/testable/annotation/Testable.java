package com.alibaba.testable.annotation;

import java.lang.annotation.*;

/**
 * On type, make all methods in the class testable
 * On method, make the method testable
 * On field (in test class), fit the variable for unit test
 *
 * @author flin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface Testable {

}
