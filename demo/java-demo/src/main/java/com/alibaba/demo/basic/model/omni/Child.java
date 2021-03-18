package com.alibaba.demo.basic.model.omni;

public class Child {

    /**
     * An inner class
     */
    public class SubChild {
        private GrandChild gc;

        public GrandChild getGrandChild() {
            return gc;
        }

        public void setGrandChild(GrandChild grandChild) {
            this.gc = grandChild;
        }
    }

    // ---------- Member fields ----------

    private GrandChild gc;

    private EnumChild ec;

    // ---------- Getters and Setters ----------

    public GrandChild getGrandChild() {
        return gc;
    }

    public void setGrandChild(GrandChild grandChild) {
        this.gc = grandChild;
    }

    public EnumChild getEnumChild() {
        return ec;
    }

    public void setEnumChild(EnumChild enumChild) {
        this.ec = enumChild;
    }
}
