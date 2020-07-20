package com.alibaba.testable.util;

import java.io.*;

/**
 * Generate global n.e class code
 *
 * @author flin
 */
public class ResourceUtil {

    public static String fetchText(String fileName) {
        InputStream in = ResourceUtil.class.getResourceAsStream("/" + fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder buffer = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null)
            {
                buffer.append(line).append('\n');
            }
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            System.err.println("Failed to fetch text file: " + fileName);
            return "";
        }
    }

    public static byte[] fetchBinary(String fileName) {
        InputStream in = ResourceUtil.class.getResourceAsStream("/" + fileName);
        if (in == null) {
            System.err.println("Resource " + fileName + " not exist");
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
            System.err.println("Failed to fetch file: " + fileName);
            return new byte[] {};
        }
    }

}
