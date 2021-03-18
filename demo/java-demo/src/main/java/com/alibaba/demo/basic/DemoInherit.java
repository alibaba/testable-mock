package com.alibaba.demo.basic;

import com.alibaba.demo.basic.model.mock.BlackBox;
import com.alibaba.demo.basic.model.mock.Box;
import com.alibaba.demo.basic.model.mock.Color;

/**
 * 演示父类变量引用子类对象时的Mock场景
 * Demonstrate scenario of mocking method from sub-type object referred by parent-type variable
 */
public class DemoInherit {

    /**
     * call method overridden by sub class via parent class variable
     */
    public Box putIntoBox() {
        Box box = new BlackBox("");
        box.put("data");
        return box;
    }

    /**
     * call method overridden by sub class via sub class variable
     */
    public BlackBox putIntoBlackBox() {
        BlackBox box = new BlackBox("");
        box.put("data");
        return box;
    }

    /**
     * call method defined in parent class via parent class variable
     */
    public String getFromBox() {
        Box box = new BlackBox("data");
        return box.get();
    }

    /**
     * call method defined in parent class via sub class variable
     */
    public String getFromBlackBox() {
        BlackBox box = new BlackBox("data");
        return box.get();
    }

    /**
     * call method defined in interface via interface variable
     */
    public String getColorViaColor() {
        Color color = new BlackBox("");
        return color.getColor();
    }

    /**
     * call method defined in interface via sub class variable
     */
    public String getColorViaBox() {
        BlackBox box = new BlackBox("");
        return box.getColor();
    }
}
