package com.alibaba.testable.core.annotation;

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
}
