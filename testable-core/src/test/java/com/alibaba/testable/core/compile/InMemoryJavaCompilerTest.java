package com.alibaba.testable.core.compile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryJavaCompilerTest {

    @Test
    public void compile_WhenTypical() throws Exception {
        final StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public class HelloClass {\n");
        sourceCode.append("   public String hello() { return \"hello\"; }");
        sourceCode.append("}");

        Class<?> helloClass = InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
        assertNotNull(helloClass);
        assertEquals(1, helloClass.getDeclaredMethods().length);
    }

    @Test
    public void compileAll_WhenTypical() throws Exception {
        String cls1 = "public class A{ public B b() { return new B(); }}";
        String cls2 = "public class B{ public String toString() { return \"B!\"; }}";

        Map<String, Class<?>> compiled = InMemoryJavaCompiler.newInstance().addSource("A", cls1).addSource("B", cls2).compileAll();

        assertNotNull(compiled.get("A"));
        assertNotNull(compiled.get("B"));

        Class<?> aClass = compiled.get("A");
        Object a = aClass.newInstance();
        assertEquals("B!", aClass.getMethod("b").invoke(a).toString());
    }

    @Test
    public void compile_WhenSourceContainsInnerClasses() throws Exception {
        final StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public class HelloClass {\n");
        sourceCode.append("   private static class InnerHelloWorld { int inner; }\n");
        sourceCode.append("   public String hello() { return \"hello\"; }");
        sourceCode.append("}");

        Class<?> helloClass = InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
        assertNotNull(helloClass);
        assertEquals(1, helloClass.getDeclaredMethods().length);
    }

    @Test
    public void compile_whenError() throws Exception {
        final StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public classHelloClass {\n");
        sourceCode.append("   public String hello() { return \"hello\"; }");
        sourceCode.append("}");
        assertThrows(CompilationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
            }
        }, "Unable to compile the source");

    }

    @Test
    public void compile_WhenFailOnWarnings() throws Exception {
        final StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public class HelloClass {\n");
        sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
        sourceCode.append("}");
        assertThrows(CompilationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
            }
        });
    }

    @Test
    public void compile_WhenIgnoreWarnings() throws Exception {
        final StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public class HelloClass {\n");
        sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
        sourceCode.append("}");
        Class<?> helloClass = InMemoryJavaCompiler.newInstance().ignoreWarnings().compile("org.mdkt.HelloClass", sourceCode.toString());
        List<?> res = (List<?>) helloClass.getMethod("hello").invoke(helloClass.newInstance());
        assertEquals(0, res.size());
    }

    @Test
    public void compile_WhenWarningsAndErrors() throws Exception {
        final StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public class HelloClass extends xxx {\n");
        sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
        sourceCode.append("}");
        assertThrows(CompilationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
            }
        });
    }
}
