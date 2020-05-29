package com.alibaba.testable.annotation;

import java.lang.annotation.*;

/**
 * Make the method in this class able to be injected by testable substitution methods
 *
 * @author flin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface EnableTestableInject {

}
