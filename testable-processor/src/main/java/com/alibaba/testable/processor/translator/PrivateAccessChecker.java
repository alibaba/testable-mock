package com.alibaba.testable.processor.translator;

import com.alibaba.testable.processor.exception.MemberNotExistException;
import com.alibaba.testable.processor.model.MemberRecord;
import com.alibaba.testable.processor.model.TestableContext;
import com.sun.tools.javac.tree.JCTree;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.alibaba.testable.processor.constant.ConstPool.TESTABLE_PRIVATE_ACCESSOR;

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

    private final TestableContext cx;
    private final String className;
    private final MemberRecord sourceMembers;

    public PrivateAccessChecker(TestableContext cx, String className, MemberRecord memberRecord) {
        this.cx = cx;
        this.className = className;
        this.sourceMembers = memberRecord;
    }

    public void validate(JCTree.JCMethodInvocation invocation) {
        if (invocation.meth instanceof JCTree.JCFieldAccess && invocation.args.length() >= 2) {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess)invocation.meth;
            if (invocation.args.get(1) instanceof JCTree.JCLiteral && isPrivateAccessor(fieldAccess)) {
                Object target = ((JCTree.JCLiteral)invocation.args.get(1)).getValue();
                if (target instanceof String) {
                    String methodName = fieldAccess.name.toString();
                    if (FIELD_ACCESS_METHOD.contains(methodName)) {
                        if (sourceMembers.nonPrivateNorFinalFields.contains(target)) {
                            cx.logger.warn("Field " + className + "::" + target + " is neither private nor final.");
                        } else if (!sourceMembers.privateOrFinalFields.contains(target)) {
                            throw new MemberNotExistException(TYPE_FIELD, className, (String)target);
                        }
                    } else if (FIELD_INVOKE_METHOD.contains(methodName)) {
                        int parameterCount = invocation.args.length() - 2;
                        // Because of override, check private method list first
                        if (sourceMembers.privateMethods.containsKey(target) &&
                            checkParameterCount(sourceMembers.privateMethods, (String)target, parameterCount)) {
                            // Let it go
                        } else if (sourceMembers.nonPrivateMethods.containsKey(target) &&
                            checkParameterCount(sourceMembers.nonPrivateMethods, (String)target, parameterCount)) {
                            cx.logger.warn("Method " + className + "::" + target + " is not private.");
                        } else {
                            throw new MemberNotExistException(TYPE_METHOD, className, (String)target, parameterCount);
                        }
                    }
                }
            }
        }
    }

    private boolean isPrivateAccessor(JCTree.JCFieldAccess fieldAccess) {
        return isPrivateAccessorWithShortRef(fieldAccess) || isPrivateAccessorWithFullRef(fieldAccess);
    }

    private boolean isPrivateAccessorWithShortRef(JCTree.JCFieldAccess fieldAccess) {
        return fieldAccess.selected instanceof JCTree.JCIdent &&
            ((JCTree.JCIdent)fieldAccess.selected).name.toString().equals(CLASS_NAME_PRIVATE_ACCESSOR);
    }

    private boolean isPrivateAccessorWithFullRef(JCTree.JCFieldAccess fieldAccess) {
        return fieldAccess.selected instanceof JCTree.JCFieldAccess &&
            fieldAccess.selected.toString().equals(TESTABLE_PRIVATE_ACCESSOR);
    }

    private boolean checkParameterCount(Map<String, List<Integer>> methods, String target, int parameterCount) {
        for (Integer expectCount : methods.get(target)) {
            if (countMatch(parameterCount, expectCount)) {
                return true;
            }
        }
        return false;
    }

    private boolean countMatch(int parameterCount, Integer expectCount) {
        return expectCount == parameterCount || (expectCount < 0 && parameterCount >= -expectCount);
    }

}
