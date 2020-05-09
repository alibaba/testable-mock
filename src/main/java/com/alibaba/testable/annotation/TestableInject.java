package com.alibaba.testable.annotation;

import java.lang.annotation.*;

/**
 * Use marked variable replace the ones in testable class
 *
 * @author flin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface TestableInject {
}
