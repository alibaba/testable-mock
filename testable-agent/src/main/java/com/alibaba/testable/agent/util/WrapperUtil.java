package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.model.BasicType;
import com.alibaba.testable.agent.model.WrapperType;

/**
 * @author jim
 */
public class WrapperUtil {

    public static Boxed wrapper2Primitive(String from, String to) {
        /*if ("Z".equals(to) && "Ljava/lang/Boolean;".equals(from)) {
            return Boxed.BOOL;
        }*/
        return null;
    }

    public static Boxed primitive2Wrapper(String from, String to) {
        /*if ("Ljava/lang/Boolean;".equals(to) && "Z".equals(from)) {
            return Boxed.BOOL;
        }*/
        return null;
    }


    public enum Boxed {

        BOOL(boolean.class, Boolean.class, "java/lang/Boolean", "booleanValue", "()Z", "valueOf", "(Z)Ljava/lang/Boolean;"),
        BYTE(byte.class, Byte.class, "java/lang/Byte","byteValue", "()B", "valueOf", "(B)Ljava/lang/Byte;"),
        SHORT(short.class, Short.class, "java/lang/Short","shortValue", "()S", "valueOf", "(S)Ljava/lang/Short;"),
        /*CHAR(boolean.class, Boolean.class, "booleanValue", "()Z", "valueOf", "(Z)Ljava/lang/Boolean;"),
        BOOL(boolean.class, Boolean.class, "booleanValue", "()Z", "valueOf", "(Z)Ljava/lang/Boolean;"),
        BOOL(boolean.class, Boolean.class, "booleanValue", "()Z", "valueOf", "(Z)Ljava/lang/Boolean;"),
        BOOL(boolean.class, Boolean.class, "booleanValue", "()Z", "valueOf", "(Z)Ljava/lang/Boolean;"),
        BOOL(boolean.class, Boolean.class, "booleanValue", "()Z", "valueOf", "(Z)Ljava/lang/Boolean;"),
        BYTE(byte.class, Byte.class, "byteValue", "()B", "valueOf", "(B)Ljava/lang/Byte;")*/;

        private Class<?> primitive;
        private Class<?> wrapper;
        private String owner;
        private String w2pMethod;
        private String w2pMethodDesc;

        private String p2wMethod;
        private String p2wMethodDesc;

        Boxed(Class<?> primitive, Class<?> wrapper, String owner, String w2pMethod, String w2pMethodDesc, String p2wMethod, String p2wMethodDesc) {
            this.primitive = primitive;
            this.wrapper = wrapper;
            this.owner = owner;
            this.w2pMethod = w2pMethod;
            this.w2pMethodDesc = w2pMethodDesc;
            this.p2wMethod = p2wMethod;
            this.p2wMethodDesc = p2wMethodDesc;
        }

        public Class<?> getPrimitive() {
            return primitive;
        }

        public Class<?> getWrapper() {
            return wrapper;
        }

        public String getOwner() {
            return owner;
        }

        public String getW2pMethod() {
            return w2pMethod;
        }

        public String getW2pMethodDesc() {
            return w2pMethodDesc;
        }

        public String getP2wMethod() {
            return p2wMethod;
        }

        public String getP2wMethodDesc() {
            return p2wMethodDesc;
        }
    }
}
