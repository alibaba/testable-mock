package com.alibaba.testable.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilTest {

    @Test
    void should_able_to_generate_target_desc() {
        assertEquals("(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",
            ClassUtil.getParameterCount("(Ljava/lang/String;)V"));
        assertEquals("(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            ClassUtil.getParameterCount("(Ljava/lang/String;IDLjava/lang/String;ZLjava/net/URL;)V"));
        assertEquals("(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            ClassUtil.getParameterCount("(ZLjava/lang/String;IJFDCSBZ)V"));
        assertEquals("(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            ClassUtil.getParameterCount("(Ljava/lang/String;[I[Ljava/lang/String;)V"));
    }

}
