package com.alibaba.testable.core.util;

import org.junit.jupiter.api.Test;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;
import sun.reflect.generics.scope.MethodScope;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeArgument;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static com.alibaba.testable.core.tool.PrivateAccessor.construct;
import static com.alibaba.testable.core.tool.PrivateAccessor.invokeStatic;
import static com.alibaba.testable.core.util.CollectionUtil.arrayOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConstructionUtilTest {

    public interface EmptyInterface {}

    public interface RealInterface {
        void noParameterMethod();
        int primaryTypeParameterMethod(double d, boolean b);
        String clazzAndArrayParameterMethod(String s, byte[] b);
        EmptyInterface interfaceAndSelfReferenceParameterMethod(RealInterface i, EmptyInterface[] e);
        <T> T templatedParameterMethod(T i);
    }

    public static abstract class AbstractClazz implements RealInterface {
        @Override
        public void noParameterMethod() {}
        public abstract <T extends String> T getByName(T name);
        public abstract String getByTags(List<? extends String> tags);
        public static <T> T useless() { return null; }
    }

    public static abstract class ParameterizedClazz<S extends String, P> extends AbstractClazz implements RealInterface, EmptyInterface {
        public abstract S getById(S id);
        public abstract S getByIds(List<S> ids);
        public abstract P getByMap(Map<String, P> m);
    }

    public interface StringMap extends Map<String, Object> {}

    public interface Inner$Interface {}

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

    @Test
    void should_generate_parameterized_class() throws Exception {
        RealInterface ins = ConstructionUtil.generateSubClassOf(ParameterizedClazz.class);
        assertNotNull(ins);
    }

    @Test
    void should_generate_typed_interface() throws Exception {
        StringMap ins = ConstructionUtil.generateSubClassOf(StringMap.class);
        assertNotNull(ins);
    }

    @Test
    void should_generate_name_with_dollar() throws Exception {
        Inner$Interface ins = ConstructionUtil.generateSubClassOf(Inner$Interface.class);
        assertNotNull(ins);
    }

    @Test
    void should_get_class_name() throws Exception {
        // common class, e.g. String
        assertEquals("java.lang.String",
                invokeStatic(ConstructionUtil.class, "getClassName", String.class));
        // array class, e.g. String[]
        assertEquals("java.lang.String[]",
                invokeStatic(ConstructionUtil.class, "getClassName", GenericArrayTypeImpl.make(String.class)));
        // typed class, e.g. T
        Method fakeMethod = construct(Method.class, String.class, "fake", new Class[0], String.class, new Class[0], Modifier.PUBLIC, 0, "", null, null, null);
        assertEquals("T", invokeStatic(ConstructionUtil.class, "getClassName", TypeVariableImpl.make(
                        fakeMethod, "T", arrayOf(SimpleClassTypeSignature.make("java.lang.String", false, new TypeArgument[0])),
                        CoreReflectionFactory.make(fakeMethod, MethodScope.make(fakeMethod)))));
        // wildcard class, e.g. ? extends String
        assertEquals("? extends java.lang.String", invokeStatic(ConstructionUtil.class, "getClassName", WildcardTypeImpl.make(
                        arrayOf(SimpleClassTypeSignature.make("java.lang.String", false, new TypeArgument[0])),
                        new SimpleClassTypeSignature[0],
                        CoreReflectionFactory.make(String.class, MethodScope.make(fakeMethod)))));
        // parameterized class, e.g. Map<String, Object>
        assertEquals("java.util.Map<java.lang.String, java.lang.Object>",
                invokeStatic(ConstructionUtil.class, "getClassName", ParameterizedTypeImpl.make(Map.class,
                        arrayOf(String.class, Object.class), null)));
    }
}
