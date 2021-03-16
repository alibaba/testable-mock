package com.alibaba.testable.core.tool;

public class DemoChild {

    public DemoGrandChild gc;

    private DemoGrandChild[] gcs;

    public class SubChild {
        private DemoGrandChild gc;
    }

    public static class StaticSubChild {
        private DemoGrandChild gc;
    }

}
