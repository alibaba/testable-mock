package com.alibaba.testable.agent.model;

import org.objectweb.asm.tree.AbstractInsnNode;

public class ModifiedInsnNodes {

    public AbstractInsnNode[] nodes;

    public int stackDiff;

    public ModifiedInsnNodes(AbstractInsnNode[] nodes, int stackDiff) {
        this.nodes = nodes;
        this.stackDiff = stackDiff;
    }

}
