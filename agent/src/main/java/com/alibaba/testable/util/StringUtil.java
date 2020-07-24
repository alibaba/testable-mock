package com.alibaba.testable.util;

/**
 * @author flin
 */
public class StringUtil {

    public static String repeat(String text, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

}
