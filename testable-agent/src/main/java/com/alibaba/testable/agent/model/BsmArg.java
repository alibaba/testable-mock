package com.alibaba.testable.agent.model;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author jim
 */
public class BsmArg {
    private Type handleDesc;
    private Type methodDesc;

    private Handle handle;

    private Object[] bsmArgs;

    private String originalHandleDesc;

    public BsmArg(Object[] bsmArgs) {
        this.bsmArgs = bsmArgs;
        handle = (Handle) bsmArgs[1];
        methodDesc = (Type) bsmArgs[2];
        handleDesc = Type.getType(handle.getDesc());
        originalHandleDesc = handle.getDesc();

        // H_INVOKEVIRTUAL: String s = "";consumes(s::contains);
        // H_INVOKEINTERFACE: list.stream().flatMap(Collection::stream)
        if (handle.getTag() == Opcodes.H_INVOKEVIRTUAL || handle.getTag() == Opcodes.H_INVOKEINTERFACE) {
            Type[] argumentTypes = handleDesc.getArgumentTypes();
            Type thisArgument = Type.getType("L" + handle.getOwner() + ";");

            String handleDescString = refineHandle(thisArgument, argumentTypes);
            handle = new Handle(handle.getTag(), handle.getOwner(), handle.getName(), handleDescString, handle.isInterface());
            handleDesc = Type.getType(handleDescString);

            if (/*handle.getTag() == Opcodes.H_INVOKEVIRTUAL || */methodDesc.getArgumentTypes().length == handleDesc.getArgumentTypes().length - 1) {
                Type[] methodArguments = methodDesc.getArgumentTypes();
                String methodDescString = refineHandle(thisArgument, methodArguments);
                methodDesc = Type.getType(methodDescString);
            }

        }
    }

    private String refineHandle(Type thisArgument, Type[] argumentTypes) {
        Type[] handleArguments = new Type[argumentTypes.length + 1];
        String[] handleArgs = new String[argumentTypes.length + 1];
        handleArguments[0] = thisArgument;
        System.arraycopy(argumentTypes, 0, handleArguments, 1, argumentTypes.length);

        for (int i = 0; i < handleArguments.length; i++) {
            handleArgs[i] = handleArguments[i].getDescriptor();
        }

        return "(" + join("", handleArgs) + ")" + handleDesc.getReturnType().getDescriptor();
    }

    private String join(String delimiter, String[] s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            stringBuilder.append(s[i]);
            if (i != s.length - 1) {
                stringBuilder.append(delimiter);
            }
        }
        return stringBuilder.toString();
    }


    public Type getHandleDesc() {
        return handleDesc;
    }

    public Type getMethodDesc() {
        return methodDesc;
    }

    public Handle getHandle() {
        return handle;
    }

    public String getOriginalHandleDesc() {
        return originalHandleDesc;
    }

    public boolean isStatic() {
        int tag = handle.getTag();
        return tag == Opcodes.H_INVOKESTATIC || tag == Opcodes.H_INVOKEVIRTUAL || tag == Opcodes.H_INVOKEINTERFACE;
    }

    public void complete(String owner, String methodName) {
        //bsmArgs[1] = new Handle(isStatic()? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKEVIRTUAL, owner, methodName, methodDesc.getDescriptor(), false);

        //bsmArgs[1] = new Handle(isStatic()? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKEVIRTUAL, owner, methodName, handleDesc.getDescriptor(), false);
        Handle h = (Handle) bsmArgs[1];
        try {
            setFinalValue(Handle.class.getDeclaredField("tag"), h, isStatic()? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKEVIRTUAL);
            setFinalValue(Handle.class.getDeclaredField("owner"), h, owner);
            setFinalValue(Handle.class.getDeclaredField("name"), h, methodName);
            setFinalValue(Handle.class.getDeclaredField("descriptor"), h, handleDesc.getDescriptor());
            setFinalValue(Handle.class.getDeclaredField("isInterface"), h, false);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFinalValue(Field ownerField, Object obj, Object value) throws Exception {
        ownerField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(ownerField, ownerField.getModifiers() & ~Modifier.FINAL);
        ownerField.set(obj, value);
    }
}
