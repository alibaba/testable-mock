package com.alibaba.testable.core.util;

import java.io.*;

/**
 * @author flin
 */
public class ResourceUtil {

    public static String fetchText(String filePath) {
        InputStream in = ResourceUtil.class.getResourceAsStream("/" + filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder buffer = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            System.err.println("Failed to fetch text file: " + filePath);
            return "";
        }
    }

    public static byte[] fetchBinary(String filePath) {
        InputStream in = ResourceUtil.class.getResourceAsStream("/" + filePath);
        if (in == null) {
            System.err.println("Resource " + filePath + " not exist");
            return new byte[] {};
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final int bufLen = 4 * 1024;
        byte[] buf = new byte[bufLen];
        int readLen;
        try {
            while ((readLen = in.read(buf, 0, bufLen)) != -1) {
                buffer.write(buf, 0, readLen);
            }
            buffer.close();
            return buffer.toByteArray();
        } catch (IOException e) {
            System.err.println("Failed to fetch file: " + filePath);
            return new byte[] {};
        }
    }

}
