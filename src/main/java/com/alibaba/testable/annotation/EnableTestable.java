package com.alibaba.testable.annotation;

import java.lang.annotation.*;

/**
 * Make test class able to access private field and method in source class
 *
 * @author flin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface EnableTestable {

}
