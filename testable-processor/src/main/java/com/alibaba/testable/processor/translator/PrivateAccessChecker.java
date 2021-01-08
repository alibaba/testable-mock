package com.alibaba.testable.processor.translator;

import com.alibaba.testable.processor.exception.MemberNotExistException;
import com.sun.tools.javac.tree.JCTree;

import java.util.Arrays;
import java.util.List;

/**
 * Validate parameter of PrivateAccessor methods to prevent broken by refactor
 *
 * @author flin
 */
public class PrivateAccessChecker {

    private static final String CLASS_NAME_PRIVATE_ACCESSOR = "PrivateAccessor";
    private static final List<String> FIELD_ACCESS_METHOD = Arrays.asList(new String[]
        { "get", "set", "getStatic", "setStatic" }.clone());
    private static final List<String> FIELD_INVOKE_METHOD = Arrays.asList(new String[]
        { "invoke", "invokeStatic" }.clone());
    private static final String TYPE_FIELD = "Field";
    private static final String TYPE_METHOD = "Method";

    private final String className;
    private final List<String> privateOrFinalFields;
    private final List<String> privateMethods;

    public PrivateAccessChecker(String className, List<String> privateOrFinalFields, List<String> privateMethods) {
        this.className = className;
        this.privateOrFinalFields = privateOrFinalFields;
        this.privateMethods = privateMethods;
    }

    public void validate(JCTree.JCMethodInvocation invocation) {
        if (invocation.meth instanceof JCTree.JCFieldAccess && invocation.args.length() >= 2) {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess)invocation.meth;
            if (fieldAccess.selected instanceof JCTree.JCIdent && invocation.args.get(1) instanceof JCTree.JCLiteral &&
                ((JCTree.JCIdent)fieldAccess.selected).name.toString().equals(CLASS_NAME_PRIVATE_ACCESSOR)) {
                Object target = ((JCTree.JCLiteral)invocation.args.get(1)).getValue();
                if (target instanceof String) {
                    String methodName = fieldAccess.name.toString();
                    if (FIELD_ACCESS_METHOD.contains(methodName) && !privateOrFinalFields.contains(target)) {
                        throw new MemberNotExistException(TYPE_FIELD, className, (String)target);
                    } else if (FIELD_INVOKE_METHOD.contains(methodName) && !privateMethods.contains(target)) {
                        throw new MemberNotExistException(TYPE_METHOD, className, (String)target);
                    }
                }
            }
        }
    }

}
