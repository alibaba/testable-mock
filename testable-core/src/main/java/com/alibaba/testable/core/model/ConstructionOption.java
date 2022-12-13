package com.alibaba.testable.core.model;

public enum ConstructionOption {

    /**
     * 不初始化循环嵌套类型的成员
     * allow members with same type nested inside itself be initialized as null
     */
    EXCEPT_LOOP_NESTING,

    /**
     * 不初始化接口和抽象类型的成员
     * allow members of interface or abstract class type be initialized as null
     */
    EXCEPT_INTERFACE,

    /**
     * 生成的接口成员方法不返回初始化过的对象
     * allow return value of methods in constructed interface be null
     */
    EXCEPT_RETURN_VALUE,

    /**
     * 调用虚拟抽象类调用父构造方法时，不使用初始化过的参数对象
     * allow to invoke constructor of abstract class with null parameter
     */
    EXCEPT_CONSTRUCTOR_PARAMETER

}
