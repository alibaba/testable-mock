package com.alibaba.testable.core.util;

import java.io.File;

public class PathUtil {

    /**
     * Create folder recursively
     * @param folderPath folder to create
     * @return whether creation success
     */
    public static boolean createFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.isDirectory()) {
            return true;
        }
        return folder.mkdirs();
    }

    /**
     * Get parent folder of specified path
     * @param path path of file or folder
     * @return parent folder path
     */
    public static String getFolder(String path) {
        return new File(path).getParent();
    }

}
