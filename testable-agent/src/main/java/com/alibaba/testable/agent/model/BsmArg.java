package com.alibaba.testable.agent.model;

import com.alibaba.testable.core.util.StringUtil;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author jim
 */
public class BsmArg {
    private Type handleDesc;
    private Type methodDesc;

    private Handle handle;

    private final Object[] bsmArgs;

    private final String originalHandleDesc;

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

        return "(" + StringUtil.join("", handleArgs) + ")" + handleDesc.getReturnType().getDescriptor();
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
        bsmArgs[1] = new Handle(isStatic()? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKEVIRTUAL, owner, methodName, handleDesc.getDescriptor(), false);
    }
}
