package com.alibaba.testable.demo.service;

import com.alibaba.testable.demo.model.BlackBox;
import com.alibaba.testable.demo.model.Box;
import org.springframework.stereotype.Service;
import sun.net.www.http.HttpClient;

import java.net.URL;

@Service
public class DemoMockService {

    /**
     * method with new operation
     */
    public String newFunc() {
        BlackBox component = new BlackBox("something");
        return component.get();
    }

    /**
     * method with member method invoke
     */
    public String outerFunc(String s) throws Exception {
        return "{ \"res\": \"" + innerFunc(s) + "\"}";
    }

    /**
     * method with common method invoke
     */
    public String commonFunc() {
        return "anything".trim() + "__" + "anything".substring(1, 2) + "__" + "abc".startsWith("ab");
    }

    /**
     * method with static method invoke
     */
    public BlackBox getBox() {
        return BlackBox.secretBox();
    }

    /**
     * method with override method invoke
     */
    public Box putBox() {
        Box box = new BlackBox("");
        box.put("data");
        return box;
    }

    /**
     * two methods invoke same private method
     */
    public String callerOne() {
        return callFromDifferentMethod();
    }

    public String callerTwo() {
        return callFromDifferentMethod();
    }

    private String innerFunc(String s) throws Exception {
        return HttpClient.New(new URL("http:/xxx/" + s)).getURLFile();
    }

    private String callFromDifferentMethod() {
        return "realOne";
    }

}
