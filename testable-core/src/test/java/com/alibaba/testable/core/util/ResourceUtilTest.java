package com.alibaba.testable.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceUtilTest {

    @Test
    void fetchText() {
        assertTrue(
            ResourceUtil.fetchText("META-INF/services/javax.annotation.processing.Processor").startsWith("com.")
        );
    }

    @Test
    void should_able_to_fetch_binary() {
        assertTrue(
            ResourceUtil.fetchBinary("com/alibaba/testable/core/util/ResourceUtil.class").length > 0
        );
    }

}
