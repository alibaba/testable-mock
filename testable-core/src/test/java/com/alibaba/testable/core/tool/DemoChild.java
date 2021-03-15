package com.alibaba.testable.core.tool;

public class DemoChild {

    public DemoGrandChild gc1;

    private DemoGrandChild gc2;

    public class SubChild {
        private DemoGrandChild gc;
    }

    public static class StaticSubChild {
        private DemoGrandChild gc;
    }

}
