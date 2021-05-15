package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.constant.ByteCodeConst;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.testable.core.constant.ConstPool.*;
import static com.alibaba.testable.core.constant.ConstPool.UNDERLINE;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author flin
 */
public class BytecodeUtil {

    /**
     * refer to https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings
     */
    private static Map<Integer, Integer> bytecodeStackEffect = new HashMap<Integer, Integer>() {{
        put(NOP, 0);
        put(ACONST_NULL, 1);
        put(ICONST_M1, 1);
        put(ICONST_0, 1);
        put(ICONST_1, 1);
        put(ICONST_2, 1);
        put(ICONST_3, 1);
        put(ICONST_4, 1);
        put(ICONST_5, 1);
        put(LCONST_0, 1);
        put(LCONST_1, 1);
        put(FCONST_0, 1);
        put(FCONST_1, 1);
        put(FCONST_2, 1);
        put(DCONST_0, 1);
        put(DCONST_1, 1);
        put(BIPUSH, 1);
        put(SIPUSH, 1);
        put(LDC, 1);
        put(ILOAD, 1);
        put(LLOAD, 1);
        put(FLOAD, 1);
        put(DLOAD, 1);
        put(ALOAD, 1);
        put(IALOAD, 1);
        put(LALOAD, 1);
        put(FALOAD, 1);
        put(DALOAD, 1);
        put(AALOAD, 1);
        put(BALOAD, 1);
        put(CALOAD, 1);
        put(SALOAD, 1);
        put(ISTORE, -1);
        put(LSTORE, -1);
        put(FSTORE, -1);
        put(DSTORE, -1);
        put(ASTORE, -1);
        put(IASTORE, -3);
        put(LASTORE, -3);
        put(FASTORE, -3);
        put(DASTORE, -3);
        put(AASTORE, -3);
        put(BASTORE, -3);
        put(CASTORE, -3);
        put(SASTORE, -3);
        put(POP, -1);
        put(POP2, -2);
        put(DUP, 1);
        put(DUP_X1, 1);
        put(DUP_X2, 1);
        put(DUP2, 2);
        put(DUP2_X1, 2);
        put(DUP2_X2, 2);
        put(SWAP, 0);
        put(IADD, -1);
        put(LADD, -1);
        put(FADD, -1);
        put(DADD, -1);
        put(ISUB, -1);
        put(LSUB, -1);
        put(FSUB, -1);
        put(DSUB, -1);
        put(IMUL, -1);
        put(LMUL, -1);
        put(FMUL, -1);
        put(DMUL, -1);
        put(IDIV, -1);
        put(LDIV, -1);
        put(FDIV, -1);
        put(DDIV, -1);
        put(IREM, -1);
        put(LREM, -1);
        put(FREM, -1);
        put(DREM, -1);
        put(INEG, 0);
        put(LNEG, 0);
        put(FNEG, 0);
        put(DNEG, 0);
        put(ISHL, -1);
        put(LSHL, -1);
        put(ISHR, -1);
        put(LSHR, -1);
        put(IUSHR, -1);
        put(LUSHR, -1);
        put(IAND, -1);
        put(LAND, -1);
        put(IOR, -1);
        put(LOR, -1);
        put(IXOR, -1);
        put(LXOR, -1);
        put(IINC, 0);
        put(I2L, 0);
        put(I2F, 0);
        put(I2D, 0);
        put(L2I, 0);
        put(L2F, 0);
        put(L2D, 0);
        put(F2I, 0);
        put(F2L, 0);
        put(F2D, 0);
        put(D2I, 0);
        put(D2L, 0);
        put(D2F, 0);
        put(I2B, 0);
        put(I2C, 0);
        put(I2S, 0);
        put(LCMP, -1);
        put(FCMPL, -1);
        put(FCMPG, -1);
        put(DCMPL, -1);
        put(DCMPG, -1);
        put(IFEQ, -1);
        put(IFNE, -1);
        put(IFLT, -1);
        put(IFGE, -1);
        put(IFGT, -1);
        put(IFLE, -1);
        put(IF_ICMPEQ, -2);
        put(IF_ICMPNE, -2);
        put(IF_ICMPLT, -2);
        put(IF_ICMPGE, -2);
        put(IF_ICMPGT, -2);
        put(IF_ICMPLE, -2);
        put(IF_ACMPEQ, -2);
        put(IF_ACMPNE, -2);
        put(GOTO, 0);
        put(JSR, 1);
        put(RET, 0);
        put(TABLESWITCH, -1);
        put(LOOKUPSWITCH, -1);
        put(IRETURN, 0);
        put(LRETURN, 0);
        put(FRETURN, 0);
        put(DRETURN, 0);
        put(ARETURN, 0);
        put(RETURN, 1);
        put(GETSTATIC, 1);
        put(PUTSTATIC, -1);
        put(GETFIELD, 0);
        put(PUTFIELD, -2);
        put(INVOKEVIRTUAL, 0); // variable
        put(INVOKESPECIAL, 0); // variable
        put(INVOKESTATIC, 0); // variable
        put(INVOKEINTERFACE, 0); // variable
        put(INVOKEDYNAMIC, 0); // variable
        put(NEW, 1);
        put(NEWARRAY, 0);
        put(ANEWARRAY, 0);
        put(ARRAYLENGTH, 0);
        put(ATHROW, 1);
        put(CHECKCAST, 0);
        put(INSTANCEOF, 0);
        put(MONITORENTER, -1);
        put(MONITOREXIT, -1);
        put(MULTIANEWARRAY, 0); // variable
        put(IFNULL, -1);
        put(IFNONNULL, -1);
    }};

    /**
     * Get stack impact of a specified ops code
     * @param bytecode ops code to check
     * @return stack change
     */
    public static int stackEffect(int bytecode) {
        return bytecodeStackEffect.get(bytecode);
    }

    /**
     * Make sure method has public access
     * @param access original access mark
     * @return access mark with public flag
     */
    public static int toPublicAccess(int access) {
        access &= ~ACC_PRIVATE;
        access &= ~ACC_PROTECTED;
        access |= ACC_PUBLIC;
        return access;
    }

    /**
     * Dump byte code to specified class file
     * @param className original class name
     * @param dumpPath folder to store class file
     * @param bytes original class bytes
     */
    public static void dumpByte(String className, String dumpPath, byte[] bytes) {
        if (dumpPath == null) {
            return;
        }
        try {
            String dumpFile = PathUtil.join(dumpPath,
                className.replace(SLASH, DOT).replace(DOLLAR, UNDERLINE) + ".class");
            LogUtil.verbose("Dump class: " + dumpFile);
            FileOutputStream stream = new FileOutputStream(dumpFile);
            stream.write(bytes);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get load ops-code of specified type
     * @param type type symbol
     * @return pair of [ops-code, stack occupation]
     */
    public static ImmutablePair<Integer, Integer> getLoadParameterByteCode(Byte type) {
        switch (type) {
            case ByteCodeConst.TYPE_BYTE:
            case ByteCodeConst.TYPE_CHAR:
            case ByteCodeConst.TYPE_SHORT:
            case ByteCodeConst.TYPE_INT:
            case ByteCodeConst.TYPE_BOOL:
                return ImmutablePair.of(ILOAD, 1);
            case ByteCodeConst.TYPE_DOUBLE:
                return ImmutablePair.of(DLOAD, 2);
            case ByteCodeConst.TYPE_FLOAT:
                return ImmutablePair.of(FLOAD, 1);
            case ByteCodeConst.TYPE_LONG:
                return ImmutablePair.of(LLOAD, 2);
            default:
                return ImmutablePair.of(ALOAD, 1);
        }
    }

    /**
     * get ops code of load a int number
     * @param num number to load
     * @return ops code
     */
    public static AbstractInsnNode getIntInsn(int num) {
        switch (num) {
            case 0:
                return new InsnNode(ICONST_0);
            case 1:
                return new InsnNode(ICONST_1);
            case 2:
                return new InsnNode(ICONST_2);
            case 3:
                return new InsnNode(ICONST_3);
            case 4:
                return new InsnNode(ICONST_4);
            case 5:
                return new InsnNode(ICONST_5);
            default:
                return new IntInsnNode(BIPUSH, num);
        }
    }
}
