package com.alibaba.testable.processor.util;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * practical logger
 *
 * @author flin
 */
public class TestableLogger {

    private Messager messager;

    public TestableLogger(Messager messager) {
        this.messager = messager;
    }

    public void info(String msg) {
        // Message level lower than warning is not shown by default, use stdout instead
        System.out.println("[INFO] " + msg);
    }

    public void warn(String msg) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg);
    }

    public void error(String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }
}
