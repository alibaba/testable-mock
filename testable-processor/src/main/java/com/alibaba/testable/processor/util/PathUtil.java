package com.alibaba.testable.processor.util;

public class PathUtil {

    private static final String PREFIX_WIN = "win";
    private static final String PROPERTY_OS_NAME = "os.name";
    private static final String PATH_SPLIT_UNIX = "/";
    private static final String PATH_SPLIT_WIN = "\\\\";
    private static final String PROTOCOL_FILE = "file:";

    /**
     * Fit path according to operation system type
     *
     * @param path original path
     * @return fitted path
     */
    public static String fitPathString(String path) {
        String os = System.getProperty(PROPERTY_OS_NAME);
        if (os.toLowerCase().startsWith(PREFIX_WIN)) {
            path = path.replaceAll(PATH_SPLIT_UNIX, PATH_SPLIT_WIN);
        }
        return path.startsWith(PROTOCOL_FILE) ? path : (PROTOCOL_FILE + path);
    }

}
