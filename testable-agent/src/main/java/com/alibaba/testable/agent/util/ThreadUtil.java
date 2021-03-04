package com.alibaba.testable.agent.util;

/**
 * @author flin
 */
public class ThreadUtil {

    private static final String PKG_TESTABLE_AGENT = "com.alibaba.testable.agent.";

    public static String getFirstRelatedStackLine(Throwable t) {
        for (StackTraceElement e : t.getStackTrace()) {
            if (e.getClassName().startsWith(PKG_TESTABLE_AGENT)) {
                return e.toString();
            }
        }
        return "";
    }

}
