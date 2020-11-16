package com.alibaba.testable.demo.service;


import com.alibaba.testable.demo.model.BlackBox;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author flin
 */
@Service
public class DemoMatcherService {

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
        methodToBeMocked(1, 2);
        methodToBeMocked(1L, 2.0);
        Long[] longArray = new Long[]{1L, 2L};
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
