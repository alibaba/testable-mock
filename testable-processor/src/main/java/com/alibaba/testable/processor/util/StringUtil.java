package com.alibaba.testable.processor.util;

import java.util.List;

/**
 * @author flin
 */
public class StringUtil {

    private static final String PREFIX_WIN = "win";
    private static final String PROPERTY_OS_NAME = "os.name";
    private static final String PATH_SPLIT_UNIX = "/";
    private static final String PATH_SPLIT_WIN = "\\";

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

    /**
     * Fit path according to operation system type
     * @param path original path
     * @return fitted path
     */
    static public String fitPathString(String path) {
        String os = System.getProperty(PROPERTY_OS_NAME);
        if (os.toLowerCase().startsWith(PREFIX_WIN)) {
            return path.replaceAll(PATH_SPLIT_UNIX, PATH_SPLIT_WIN);
        }
        return path;
    }

}
