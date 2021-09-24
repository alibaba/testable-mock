package com.alibaba.demo.lambda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author jim
 */
@SuppressWarnings("unused")
public class LambdaDemo {

    public void methodReference() {
        consumesRun(this::run);
    }

    private void consumesRun(Runnable r) {
        r.run();
    }

    private void run() {
        blackHole();
    }

    public String methodReference0() {
        return consumes0(this::function0);
    }

    private String consumes0(Supplier<String> function0) {
        return function0.get();
    }

    private String function0() {
        return "Hello";
    }

    public String methodReference1() {
        return consumes1(this::function1);
    }

    private String consumes1(Function<Integer, String> function) {
        return function.apply(1);
    }

    private String function1(Integer i) {
        return String.valueOf(i);
    }

    public String methodReferenceThrows() {
        return consumesThrows(this::function1Throwable);
    }

    private String consumesThrows(Function1Throwable<Integer, String> function) {
        try {
            return function.apply(1);
        }catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("RedundantThrows")
    private String function1Throwable(Integer i) throws Throwable{
        return String.valueOf(i);
    }

    public String methodReference2() {
        return consumes2(this::function2);
    }

    private String consumes2(BiFunction<Integer, Double, String> function) {
        return function.apply(1, .2);
    }

    private String function2(Integer i, Double d) {
        return i + String.valueOf(d);
    }

    public String staticMethodReference1() {
        return consumes1(StaticMethod::function1);
    }

    public String staticMethodReference2() {
        return consumes2(StaticMethod::function2);
    }

    public void lambdaRun() {
        consumes(() -> System.out.println("lambdaRun"));
    }

    private void consumes(Runnable o) {
        o.run();
    }

    public void methodReferenceNew() {
        Object o = consumes(Object::new);
        blackHole(o);
    }

    private <T> T consumes(Supplier<T> s) {
        return s.get();
    }

    private void blackHole(Object... ignore) {}

    public void array() {
        Function<Boolean[], Boolean[]> arrayBooleanFunction = this::arrayBooleanFunction;
        Function<boolean[], boolean[]> arrayBooleanFunction1 = this::arrayBoolFunction;
        Function<Byte[], Byte[]> byteFunction = this::arrayByteFunction;
        Function<byte[], byte[]> byteFunction1 = this::arrayByteFunction;
        Function<Character[], Character[]> charFunction = this::arrayCharFunction;
        Function<char[], char[]> charFunction1 = this::arrayCharFunction;
        Function<Short[], Short[]> shortFunction = this::arrayShortFunction;
        Function<short[], short[]> shortFunction1 = this::arrayShortFunction;
        Function<int[], int[]> intFunction = this::arrayIntFunction;
        Function<Integer[], Integer[]> intFunction1 = this::arrayIntegerFunction;
        Function<long[], long[]> longFunction = this::arrayLongFunction;
        Function<Long[], Long[]> longFunction1 = this::arrayLongFunction;
        Function<Float[], Float[]> floatFunction = this::arrayFloatFunction;
        Function<float[], float[]> floatFunction1 = this::arrayFloatFunction;
        Function<Double[], Double[]> doubleFunction = this::arrayDoubleFunction;
        Function<double[], double[]> doubleFunction1 = this::arrayDoubleFunction;
        blackHole(arrayBooleanFunction, arrayBooleanFunction1,
                byteFunction, byteFunction1, charFunction, charFunction1, shortFunction, shortFunction1,
                intFunction, intFunction1, longFunction, longFunction1, floatFunction, floatFunction1, doubleFunction,
                doubleFunction1
        );
    }

    private int[] arrayIntFunction(int[] arg) {
        return arg;
    }

    private Integer[] arrayIntegerFunction(Integer[] arg) {
        return arg;
    }

    private boolean[] arrayBoolFunction(boolean[] arg) {
        return arg;
    }

    private Boolean[] arrayBooleanFunction(Boolean[] arg) {
        return arg;
    }

    private byte[] arrayByteFunction(byte[] arg) {
        return arg;
    }

    private Byte[] arrayByteFunction(Byte[] arg) {
        return arg;
    }

    private char[] arrayCharFunction(char[] arg) {
        return arg;
    }

    private Character[] arrayCharFunction(Character[] arg) {
        return arg;
    }

    private short[] arrayShortFunction(short[] arg) {
        return arg;
    }

    private Short[] arrayShortFunction(Short[] arg) {
        return arg;
    }

    private long[] arrayLongFunction(long[] arg) {
        return arg;
    }

    private Long[] arrayLongFunction(Long[] arg) {
        return arg;
    }

    private float[] arrayFloatFunction(float[] arg) {
        return arg;
    }

    private Float[] arrayFloatFunction(Float[] arg) {
        return arg;
    }

    private double[] arrayDoubleFunction(double[] arg) {
        return arg;
    }

    private Double[] arrayDoubleFunction(Double[] arg) {
        return arg;
    }

    public void generic() {
        Function<?, ?> genericFunction = this::genericFunction;
        blackHole(genericFunction);
    }

    public <T, R> R genericFunction(T arg) {
        //noinspection unchecked
        return (R)arg;
    }

    private void collects() {
        long l = Stream.of("1", "2", "3")
                .filter(v -> !"2".equals(v))
                .map(Long::parseLong)
                .peek(this::blackHole)
                .map(v -> new ArrayList<Long>(){{add(v);}})
                .flatMap(Collection::stream)
                .mapToLong(Long::valueOf)
                .sum();
        blackHole(l);
    }
}
