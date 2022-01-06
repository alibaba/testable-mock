package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockDiagnose;
import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.model.LogLevel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author jim
 */
public class FzoTest {
    Fzo f = new Fzo();
    @MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {
        @MockInvoke(targetClass = Double.class, targetMethod = "hashCode")
        private int cc() {
            return 1;
        }

        @MockInvoke(targetClass = Collection.class, targetMethod = "stream")
        <E> Stream<E> mockStream() {
            List<E> l = new ArrayList<>();
            l.add((E)Boolean.TRUE);
            l.add((E)Boolean.TRUE);
            l.add((E)Boolean.TRUE);
            System.out.println("mockStreammockStreammockStreammockStreammockStreammockStreammockStream");
            return l.stream();
        }

        @MockInvoke(targetClass = Boolean.class, targetMethod = "logicalAnd")
        public static boolean mockLogicalAnd(boolean a, boolean b) {
            System.out.println("ZPPPPPPPPPPPPPPPPPPPPPP");
            return false;
        }
    }

    @Test
    public void shouldMockInterfaceStatic() {
        f.objectStaticMethodReference();
    }
}
