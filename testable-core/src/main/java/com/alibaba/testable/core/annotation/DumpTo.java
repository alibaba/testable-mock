package com.alibaba.testable.core.annotation;

import java.lang.annotation.*;

/**
 * Dump byte code for single class
 *
 * @author flin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface DumpTo {

    /**
     * dump class byte code to specified folder
     * @return folder path relate to the project root path
     */
    String value();

}
