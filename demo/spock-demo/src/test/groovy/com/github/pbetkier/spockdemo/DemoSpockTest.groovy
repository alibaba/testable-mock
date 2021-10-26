package com.github.pbetkier.spockdemo

import com.alibaba.testable.core.annotation.MockConstructor
import com.alibaba.testable.core.annotation.MockMethod
import com.github.pbetkier.spockdemo.model.SpockBox
import spock.lang.Shared
import spock.lang.Specification

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;

class DemoSpockTest extends Specification {

    @Shared
    def demoSpock = new DemoSpock()

    static class Mock {
        @MockConstructor
        SpockBox createBox() {
            SpockBox box = new SpockBox()
            box.put("mock zero")
            return box
        }

        @MockMethod(targetMethod = "put")
        void putBox(SpockBox self, String data) {
            self.put("mock " + data)
        }
    }

    def "should get a box of numbers"() {
        given:
        def box = demoSpock.createBoxOfNum()

        expect:
        box.size() == 4
        box.pop() == "mock 3"
        box.pop() == "mock 2"
        box.pop() == "mock 1"
        box.pop() == "mock zero"
        verify("createBox").withTimes(1)
        verify("putBox").withInOrder("1").withInOrder("2").withInOrder("3")
    }

}
