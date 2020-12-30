package com.alibaba.testable.processor.util;

import java.util.List;

/**
 * @author flin
 */
public class StringUtil {

    /**
     * Join strings
     * @param list        strings to join
     * @param conjunction connection character
     * @return joined string
     */
    static public String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                first = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

}
