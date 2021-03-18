package com.alibaba.demo.basic;


import com.alibaba.demo.basic.model.mock.BlackBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 演示Mock方法调用校验器
 * Demonstrate mock method invocation verifier
 */
public class DemoMatcher {

    /**
     * Method to be mocked
     */
    private void methodToBeMocked() {
        // pretend to have some code here
    }

    /**
     * Method to be mocked
     */
    private void methodToBeMocked(Object a1, Object a2) {
        // pretend to have some code here
    }

    /**
     * Method to be mocked
     */
    private void methodToBeMocked(Object[] a) {
        // pretend to have some code here
    }

    public void callMethodWithoutArgument() {
        methodToBeMocked();
    }

    public void callMethodWithNumberArguments() {
        // named variable and lambda variable will be recorded as different type
        // should have them both in test case
        List<Float> floatList = new ArrayList<>();
        floatList.add(1.0F);
        floatList.add(2.0F);
        Long[] longArray = new Long[]{1L, 2L};
        methodToBeMocked(1, 2);
        methodToBeMocked(1L, 2.0);
        methodToBeMocked(new ArrayList<Integer>(){{ add(1); }}, new HashSet<Float>(){{ add(1.0F); }});
        methodToBeMocked(1.0, new HashMap<Integer, Float>(2){{ put(1, 1.0F); }});
        methodToBeMocked(floatList, floatList);
        methodToBeMocked(longArray);
        methodToBeMocked(new Double[]{1.0, 2.0});
    }

    public void callMethodWithStringArgument() {
        methodToBeMocked("hello", "world");
        methodToBeMocked("testable", "mock");
        methodToBeMocked(new String[]{"demo"});
    }

    public void callMethodWithObjectArgument() {
        methodToBeMocked(new BlackBox("hello"), new BlackBox("world"));
        methodToBeMocked(new BlackBox("demo"), null);
        methodToBeMocked(null, new BlackBox("demo"));
    }

}
