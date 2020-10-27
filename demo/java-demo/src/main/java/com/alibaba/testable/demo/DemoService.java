package com.alibaba.testable.demo;

import org.springframework.stereotype.Service;
import sun.net.www.http.HttpClient;

import java.net.URL;

@Service
public class DemoService {

    private int count;

    /**
     * Target 1 - private method
     */
    private String privateFunc(String s, int i) {
        return s + " - " + i;
    }

    /**
     * Target 2 - method with private field access
     */
    public String privateFieldAccessFunc() {
        count += 2;
        return String.valueOf(count);
    }

    /**
     * Target 3 - method with new operation
     */
    public String newFunc() {
        BlackBox component = new BlackBox("something");
        return component.get();
    }

    /**
     * Target 4 - method with member method invoke
     */
    public String outerFunc(String s) throws Exception {
        return "{ \"res\": \"" + innerFunc(s) + "\"}";
    }

    /**
     * Target 5 - method with common method invoke
     */
    public String commonFunc() {
        return "anything".trim() + "__" + "anything".substring(1, 2) + "__" + "abc".startsWith("ab");
    }

    /**
     * Target 6 - method with static method invoke
     */
    public BlackBox getBox() {
        return BlackBox.secretBox();
    }

    /**
     * Target 7 - method with override method invoke
     */
    public Box putBox() {
        Box box = new BlackBox("");
        box.put("data");
        return box;
    }

    /**
     * Target 8 - two methods invoke same private method
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
