package com.alibaba.testable.core.util;

public class StringUtil {

    /**
     * repeat a text many times
     * @param text content to repeat
     * @param times count of repeating
     * @return repeated string
     */
    public static String repeat(String text, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

    /**
     * concat several strings into a single string
     * @param delimiter symbol to add between provided strings
     * @param s an array of string to join
     * @return joined string
     */
    public static String join(String delimiter, String... s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            stringBuilder.append(s[i]);
            if (i != s.length - 1) {
                stringBuilder.append(delimiter);
            }
        }
        return stringBuilder.toString();
    }

}
