package com.alibaba.testable.generator;

import com.alibaba.testable.model.TestableContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("e.java").getFile());
        if (!file.exists()) {
            cx.logger.error("Failed to fetch testable new stand-in.");
        }
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            cx.logger.error("Failed to generate testable new stand-in.");
            return "";
        }
    }

}
