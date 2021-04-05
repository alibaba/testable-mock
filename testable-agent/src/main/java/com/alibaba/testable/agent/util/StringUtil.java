package com.alibaba.testable.agent.util;

/**
 * @author flin
 */
public class StringUtil {

    /**
     * repeat a text many times
     * @param text content to repeat
     * @param times count of repeating
     * @return joined string
     */
    public static String repeat(String text, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

}
