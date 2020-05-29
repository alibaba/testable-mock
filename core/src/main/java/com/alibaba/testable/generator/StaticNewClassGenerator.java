package com.alibaba.testable.generator;

import com.alibaba.testable.model.TestableContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Generate global n.e class code
 *
 * @author flin
 */
public class StaticNewClassGenerator extends BaseGenerator {

    public StaticNewClassGenerator(TestableContext cx) {
        super(cx);
    }

    public String fetch() {
        InputStream in = getClass().getResourceAsStream("/e.java");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null)
            {
                buffer.append(line).append('\n');
            }
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            cx.logger.error("Failed to generate testable new stand-in.");
            return "";
        }
    }

}
