package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.constant.ConstPool;

/**
 * @author flin
 */
public class StringUtil {

    /**
     * repeat a text many times
     * @param text content to repeat
     * @param times count of repeating
     */
    public static String repeat(String text, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

    /**
     * get test class name from source class name
     * @param sourceClassName source class name
     */
    public static String getTestClassName(String sourceClassName) {
        return sourceClassName + ConstPool.TEST_POSTFIX;
    }

    /**
     * get source class name from test class name
     * @param testClassName test class name
     */
    public static String getSourceClassName(String testClassName) {
        return testClassName.substring(0, testClassName.length() - ConstPool.TEST_POSTFIX.length());
    }

}
