package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockInvoke;
import org.junit.jupiter.api.Test;


/**
 * @author jim
 */
public class CollectionListCodeDemoTest {

    private final CollectionListCodeDemo instance = new CollectionListCodeDemo();

    public static class Mock {
        @MockInvoke(targetClass = String.class, targetMethod = "contains")
        public boolean mockContains(CharSequence s) {
            return false;
        }
    }

    @Test
    public void listTest() {
        instance.list();
    }
}
