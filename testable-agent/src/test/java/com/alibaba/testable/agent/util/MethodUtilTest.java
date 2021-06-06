package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.tool.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static com.alibaba.testable.core.tool.PrivateAccessor.invokeStatic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodUtilTest {

    @Test
    void should_get_parameter_count() {
        assertEquals(0, MethodUtil.getParameterTypes("()V").size());
        assertEquals(1, MethodUtil.getParameterTypes("(Ljava/lang/String;)V").size());
        assertEquals(6, MethodUtil.getParameterTypes("(Ljava/lang/String;IDLjava/lang/String;ZLjava/net/URL;)V").size());
        assertEquals(10, MethodUtil.getParameterTypes("(ZLjava/lang/String;IJFDCSBZ)V").size());
        assertEquals(3, MethodUtil.getParameterTypes("(Ljava/lang/String;[I[Ljava/lang/String;)V").size());
    }

    @Test
    void should_extract_parameter() {
        assertEquals("", MethodUtil.extractParameters("()I"));
        assertEquals("Ljava/lang/String;", MethodUtil.extractParameters("(Ljava/lang/String;)I"));
    }

    @Test
    void should_get_return_type() {
        assertEquals("V", MethodUtil.getReturnType("(Ljava/lang/String;)V"));
        assertEquals("I", MethodUtil.getReturnType("(Ljava/lang/String;)I"));
        assertEquals("[I", MethodUtil.getReturnType("(Ljava/lang/String;)[I"));
        assertEquals("Ljava/lang/String;", MethodUtil.getReturnType("(Ljava/lang/String;)Ljava/lang/String;"));
        assertEquals("[Ljava/lang/String;", MethodUtil.getReturnType("(Ljava/lang/String;)[Ljava/lang/String;"));
    }

    @Test
    void should_get_parameters() {
        assertEquals("Ljava/lang/String;", MethodUtil.getParameters("(Ljava/lang/String;)V"));
        assertEquals("I", MethodUtil.getParameters("(I)Ljava/lang/String;"));
        assertEquals("", MethodUtil.getParameters("()Ljava/lang/String;"));
    }

    @Test
    void should_split_parameters() {
        ImmutablePair<String, String> parameters = MethodUtil.splitFirstAndRestParameters("()");
        assertEquals("", parameters.left);
        assertEquals("", parameters.right);
        parameters = MethodUtil.splitFirstAndRestParameters("(ZZILjava/lang/String;Z)");
        assertEquals("Z", parameters.left);
        assertEquals("(ZILjava/lang/String;Z)", parameters.right);
        parameters = MethodUtil.splitFirstAndRestParameters("(Lcom/alibaba/demo/Class;ILjava/lang/String;Z)");
        assertEquals("com/alibaba/demo/Class", parameters.left);
        assertEquals("(ILjava/lang/String;Z)", parameters.right);
    }

    @Test
    void should_get_first_parameter() {
        assertEquals("Ljava/lang/String;", MethodUtil.getFirstParameter("(Ljava/lang/String;Ljava/lang/Object;I)V"));
        assertEquals("Ljava/lang/String;", MethodUtil.getFirstParameter("(Ljava/lang/String;)V"));
        assertThrows(IndexOutOfBoundsException.class, new Executable() {
            @Override
            public void execute() {
                MethodUtil.getFirstParameter("()V");
            }
        });
    }

    @Test
    void should_convert_bytecode_parameters() {
        assertEquals("", invokeStatic(MethodUtil.class, "toJavaParameterDesc", ""));
        assertEquals("void", invokeStatic(MethodUtil.class, "toJavaParameterDesc", "V"));
        assertEquals("int, long", invokeStatic(MethodUtil.class, "toJavaParameterDesc", "IJ"));
        assertEquals("int[], long[]", invokeStatic(MethodUtil.class, "toJavaParameterDesc", "[I[J"));
        assertEquals("int, java.lang.String", invokeStatic(MethodUtil.class, "toJavaParameterDesc", "ILjava/lang/String;"));
        assertEquals("java.lang.String, int, long[]", invokeStatic(MethodUtil.class, "toJavaParameterDesc", "Ljava/lang/String;I[J"));
    }

}

