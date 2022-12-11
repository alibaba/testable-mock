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
     * 生成的接口成员方法返回非空对象（某些JDK内置接口不兼容此选项）
     * methods in constructed interface return real object instead of null
     */
    RICH_INTERFACE_METHOD,

    /**
     * 生成的虚拟抽象类调用父构造方法时，使用非空对象作为参数
     * methods in constructed interface return real object instead of null
     */
    RICH_INTERFACE_CONSTRUCTOR

}
