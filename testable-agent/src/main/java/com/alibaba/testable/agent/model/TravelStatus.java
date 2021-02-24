package com.alibaba.testable.agent.model;

/**
 * @author flin
 */
public enum TravelStatus {

    /**
     * traveling common bytecode
     */
    Normal,

    /**
     * looking for label opcode
     */
    LookingForLabel,

    /**
     * looking for jump opcode
     */
    LookingForJump

}
