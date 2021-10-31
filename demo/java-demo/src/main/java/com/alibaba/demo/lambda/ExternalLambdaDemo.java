package com.alibaba.demo.lambda;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author jimca
 */
@SuppressWarnings({"WrapperTypeMayBePrimitive", "ResultOfMethodCallIgnored", "MismatchedReadAndWriteOfArray", "unused"})
public class ExternalLambdaDemo {

    public void string1() {
        String s = "";
        consumesFunction2(s::contains);
    }

    public void string2() {
        String s = "";
        consumesFunction2(s::charAt);

    }

    public void string3() {
        String s = "";
        consumesFunction0(s::notify);
    }

    public void byte1() {
        Byte s = 1;
        consumesSupplier(s::floatValue);
    }

    public void byte2() {
        Byte s = 1;
        consumesFunction2(s::compareTo);
    }

    public void byte3() {
        Byte s = 1;
        consumesFunction0(s::notify);
    }

    public void char1() {
        Character s = 1;
        consumesFunction0(s::toString);
    }

    public void char2() {
        Character s = 1;
        consumesFunction2(s::compareTo);
    }

    public void char3() {
        Character s = 1;
        consumesFunction0(s::notify);
    }

    public void short1() {
        Short s = 1;
        consumesFunction0(s::toString);
    }

    public void short2() {
        Short s = 1;
        consumesFunction2(s::compareTo);
    }

    public void short3() {
        Short s = 1;
        consumesFunction0(s::notify);
    }

    public void int1() {
        Integer s = 1;
        consumesFunction0(s::toString);
    }

    public void int2() {
        Integer s = 1;
        consumesFunction2(s::compareTo);
    }

    public void int3() {
        Integer s = 1;
        consumesFunction0(s::notify);
    }

    public void long1() {
        Long s = 1L;
        consumesFunction0(s::toString);
    }

    public void long2() {
        Long s = 1L;
        consumesFunction2(s::compareTo);
    }

    public void long3() {
        Long s = 1L;
        consumesFunction0(s::notify);
    }

    public void float1() {
        Float s = 1f;
        consumesFunction0(s::toString);
    }

    public void float2() {
        Float s = 1f;
        consumesFunction2(s::compareTo);
    }

    public void float3() {
        Float s = 1f;
        consumesFunction0(s::notify);
    }

    public void double1() {
        Double s = 1d;
        consumesFunction0(s::toString);
    }

    public void double2() {
        Double s = 1d;
        consumesFunction2(s::compareTo);
    }

    public void double3() {
        Double s = 1d;
        consumesFunction0(s::notify);
    }

    public void bool1() {
        Boolean s = true;
        consumesFunction0(s::toString);
    }

    public void bool2() {
        Boolean s = true;
        consumesFunction2(s::compareTo);
    }

    public void bool3() {
        Boolean s = true;
        consumesFunction0(s::notify);
    }

    public void stringArray1() {
        String[] array = new String[]{""};
        consumesFunction0(array::toString);
    }

    public void stringArray2() {
        String[] array = new String[]{""};
        consumesFunction2(array::equals);
    }

    public void stringArray3() {
        String[] array = new String[]{""};
        consumesFunction0(array::notify);
    }

    public void intArray1() {
        int[] array = new int[]{1};
        consumesFunction0(array::toString);
    }

    public void intArray2() {
        int[] array = new int[]{1};
        consumesFunction2(array::equals);
    }

    public void intArray3() {
        int[] array = new int[]{1};
        consumesFunction0(array::notify);
    }


    public void mul() {
        String s = "";
        consumesTwoFunction2(s::contains, s::contains);
    }


    public void externalClass() {
        LambdaDemo lambdaDemo = new LambdaDemo();
        consumesFunction0(lambdaDemo::methodReference0);
    }


    public void interClass() {
        A a = new A();
        consumesFunction2(a::m1);
        consumesFunction2(a::m2);
    }

    public void function3() {
        ExternalLambdaDemo externalLambdaDemo = new ExternalLambdaDemo();
        consumesFunction3(externalLambdaDemo::f3);
    }

    public Boolean f3(String s1, Long l) {
        return false;
    }

    private void consumesFunction0(Runnable f) {
        f.run();
    }

    private <T> void consumesFunction1(Consumer<T> f) {
        f.accept(null);
    }

    private <T, R> void consumesFunction2(Function<T, R> f) {
        f.apply(null);
    }

    private <T1, T2, R> void consumesFunction3(BiFunction<T1, T2, R> f) {
        f.apply(null, null);
    }

    private <T> void consumesSupplier(Supplier<T> supplier) {
        supplier.get();
    }

    private <T, R> void consumesTwoFunction2(Function<T, R> f1, Function<T, R> f2) {
        f1.apply(null);
        f2.apply(null);
    }

    public static class A {
        public String m1(int i) {
            return "";
        }

        public String m2(Integer i) {
            return "";
        }

    }
}
