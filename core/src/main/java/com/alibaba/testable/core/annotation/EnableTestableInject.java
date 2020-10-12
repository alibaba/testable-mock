package com.alibaba.testable.core.annotation;

import java.lang.annotation.*;

/**
 * Make the method in this class able to be injected by testable substitution methods
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EnableTestableInject {
}
