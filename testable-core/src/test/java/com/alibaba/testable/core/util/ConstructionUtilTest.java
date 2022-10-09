package com.alibaba.testable.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstructionUtilTest {

    public interface EmptyInterface {}

    public interface RealInterface {
        void fun1();
        int func2(double d, boolean b);
        String fun3(String s, byte[] b);
        EmptyInterface fun4(RealInterface i);
        <T> T fun5(T i);
    }

    public static abstract class AbstractClazz implements RealInterface {
        @Override
        public void fun1() {}
        public static <T> T useless() { return null; }
    }

    @Test
    void should_generate_empty_interface() throws Exception {
        EmptyInterface ins = ConstructionUtil.generateSubClassOf(EmptyInterface.class);
        assertNotNull(ins);
    }

    @Test
    void should_generate_real_interface() throws Exception {
        RealInterface ins = ConstructionUtil.generateSubClassOf(RealInterface.class);
        assertNotNull(ins);
    }

    @Test
    void should_generate_abstract_class() throws Exception {
        RealInterface ins = ConstructionUtil.generateSubClassOf(AbstractClazz.class);
        assertNotNull(ins);
    }
}
