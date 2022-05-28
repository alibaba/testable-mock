package com.alibaba.demo.lambda;

/**
 * @author jim
 */
public class StaticInstanceReference implements BaseDemo {

    private static final StaticClassA STATIC_CLASS_A = new StaticClassA();

    public void staticMethodReference() {
        consumesRun(STATIC_CLASS_A::doIt);
        consumesFunction1(STATIC_CLASS_A::function1);
        consumesFunction2(STATIC_CLASS_A::function2);
        consumesFunction3(STATIC_CLASS_A::function3);
    }

    public static class StaticClassA {
        public void doIt() {
        }

        public void function1(String s) {

        }

        public Integer function2(String s) {
            return 1;
        }

        public Integer function3(String s, Double d) {
            return 1;
        }
    }
}
