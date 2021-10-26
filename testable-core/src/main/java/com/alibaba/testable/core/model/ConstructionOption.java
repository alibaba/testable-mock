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
    EXCEPT_INTERFACE

}
