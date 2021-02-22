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

    /**
     * Print hint message
     */
    public void info(String msg) {
        // Message level lower than warning is not shown by default, use stdout instead
        System.out.println("[INFO] " + msg);
    }

    /**
     * Print warning message
     */
    public void warn(String msg) {
        // Message level WARNING won't show, use MANDATORY_WARNING instead
        messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg);
    }

    /**
     * Print fatal message
     * Note: this will stop current compile process
     */
    public void fatal(String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }
}
