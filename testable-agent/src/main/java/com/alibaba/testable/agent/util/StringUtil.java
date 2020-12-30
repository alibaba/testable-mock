package com.alibaba.testable.agent.util;

import java.io.File;

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

    /**
     * join a path text and a file name to full file path
     * @param folder path text
     * @param file file name
     * @return joined full file path
     */
    public static String joinPath(String folder, String file) {
        return (folder.endsWith(File.separator) ? folder : (folder + File.separator)) + file;
    }

}
