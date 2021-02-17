package com.alibaba.testable.core.util;


/**
 * @author flin
 */
public class TestableUtil {

    /**
     * [0]Thread.getStackTrace() → [1]currentSourceMethodName() → [2]MockMethod → [3]SourceMethod
     */
    private static final int INDEX_OF_SOURCE_METHOD = 3;
    /**
     * [0]Thread.getStackTrace() → [1]previousStackLocation() → [2]Invoker → [3]Caller of invoker
     */
    private static final int INDEX_OF_CALLER_METHOD = 3;

    /**
     * Get the last visit method in source file
     * @return method name
     */
    public static String currentSourceMethodName() {
        return Thread.currentThread().getStackTrace()[INDEX_OF_SOURCE_METHOD].getMethodName();
    }

    /**
     * Get file name and line number of where current method was called
     * @return in "filename:linenumber" format
     */
    public static String previousStackLocation() {
        StackTraceElement stack = Thread.currentThread().getStackTrace()[INDEX_OF_CALLER_METHOD];
        return stack.getFileName() + ":" + stack.getLineNumber();
    }

}
