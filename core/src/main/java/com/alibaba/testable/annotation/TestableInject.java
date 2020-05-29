package com.alibaba.testable.annotation;

import java.lang.annotation.*;

/**
 * Use marked method to replace the ones in source class
 *
 * @author flin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@Documented
public @interface TestableInject {
}
