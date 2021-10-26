package com.alibaba.testable.agent.model;

import static org.objectweb.asm.Opcodes.*;

/**
 * simplified of java.lang.invoke.LambdaForm.BasicType
 */
public enum BasicType {

    /**
     * all reference types
     */
    L_TYPE('L', Object.class, WrapperType.OBJECT, ALOAD, ARETURN),
    /**
     * all primitive types
     */
    I_TYPE('I', int.class,    WrapperType.INT, ILOAD, IRETURN),
    J_TYPE('J', long.class,   WrapperType.LONG, LLOAD, LRETURN),
    F_TYPE('F', float.class,  WrapperType.FLOAT, FLOAD, FRETURN),
    D_TYPE('D', double.class, WrapperType.DOUBLE, DLOAD, DRETURN),
    V_TYPE('V', void.class,   WrapperType.VOID, null, RETURN),
    A_TYPE('[', Object[].class,   WrapperType.OBJECT, ALOAD, ARETURN);

    public final char btChar;
    public final Class<?> btClass;
    public final WrapperType btWrapper;
    public final Integer loadVarInsn;
    public final Integer returnInsn;

    BasicType(char btChar, Class<?> btClass, WrapperType btWrapper, Integer loadVarInsn, Integer returnInsn) {
        this.btChar = btChar;
        this.btClass = btClass;
        this.btWrapper = btWrapper;
        this.loadVarInsn = loadVarInsn;
        this.returnInsn = returnInsn;
    }

    public boolean isPrimitive() {
        return this != L_TYPE && this != A_TYPE;
    }

    public static BasicType basicType(char type) {
        switch (type) {
            case 'L': return L_TYPE;
            case 'I': return I_TYPE;
            case 'J': return J_TYPE;
            case 'F': return F_TYPE;
            case 'D': return D_TYPE;
            case 'V': return V_TYPE;
            case '[': return A_TYPE;
            // all subword types are represented as ints
            case 'Z':
            case 'B':
            case 'S':
            case 'C':
                return I_TYPE;
            default:
                throw new InternalError("Unknown type char: '"+type+"'");
        }
    }
}
