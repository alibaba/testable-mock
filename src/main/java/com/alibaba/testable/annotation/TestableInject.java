package com.alibaba.testable.annotation;

import java.lang.annotation.*;

/**
 * Use marked variable replace the ones in testable class
 *
 * @author linfan
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface TestableInject {
}
