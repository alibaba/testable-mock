package com.alibaba.testable.core.generator;

import com.alibaba.testable.core.model.TestableContext;
import com.alibaba.testable.core.util.ConstPool;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Generate test class setup method definition
 *
 * @author flin
 */
public class TestSetupMethodGenerator extends BaseGenerator {

    /**
     * MethodName -> (ResultType -> ParameterTypes)
     */
    public ListBuffer<Pair<Name, Pair<JCExpression, List<JCExpression>>>> injectMethods = new ListBuffer<>();
    public final ListBuffer<Method> memberMethods = new ListBuffer<>();

    public TestSetupMethodGenerator(TestableContext cx) {
        super(cx);
    }

    public JCMethodDecl fetch() {
        JCModifiers mods = cx.treeMaker.Modifiers(Modifier.PUBLIC);
        return cx.treeMaker.MethodDef(mods, cx.names.fromString(ConstPool.TESTABLE_SETUP_METHOD_NAME),
            cx.treeMaker.Type(new Type.JCVoidType()), List.<JCTypeParameter>nil(),
            List.<JCVariableDecl>nil(), List.<JCExpression>nil(), testableSetupBlock(), null);
    }

    private JCBlock testableSetupBlock() {
        ListBuffer<JCStatement> statements = new ListBuffer<>();
        for (Pair<Name, Pair<JCExpression, List<JCExpression>>> m : injectMethods.toList()) {
            if (isMemberMethod(m)) {
                statements.append(addToPoolStatement(m, ConstPool.METHOD_ADD_TO_MEM_POOL));
            } else {
                statements.append(addToPoolStatement(m, ConstPool.METHOD_ADD_TO_CON_POLL));
            }
        }
        return cx.treeMaker.Block(0, statements.toList());
    }

    private boolean isMemberMethod(Pair<Name, Pair<JCExpression, List<JCExpression>>> m) {
        for (Method method : memberMethods) {
            if (method.getName().equals(m.fst.toString()) && parameterEquals(m.snd.snd, method.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

    private boolean parameterEquals(List<JCExpression> injectMethodArgs, Class<?>[] memberMethodArgs) {
        if (injectMethodArgs.length() != memberMethodArgs.length) {
            return false;
        }
        for (int i = 0; i < injectMethodArgs.length(); i++) {
            if (!memberMethodArgs[i].getName().equals(((JCFieldAccess)injectMethodArgs.get(i)).selected.type
                .toString())) {
                return false;
            }
        }
        return true;
    }

    private JCStatement addToPoolStatement(Pair<Name, Pair<JCExpression, List<JCExpression>>> m, String addPoolMethod) {
        JCExpression pool = nameToExpression(ConstPool.CLASS_SUBSTITUTION);
        JCExpression classType = m.snd.fst;
        JCExpression methodName = cx.treeMaker.Literal(m.fst.toString());
        JCExpression parameterTypes = cx.treeMaker.NewArray(cx.treeMaker.Ident(cx.names.fromString(ConstPool.TYPE_CLASS)),
            List.<JCExpression>nil(), m.snd.snd);
        JCExpression thisIns = cx.treeMaker.Ident(cx.names.fromString(ConstPool.REF_THIS));
        JCNewClass poolClass = cx.treeMaker.NewClass(null, List.<JCExpression>nil(), pool,
            List.of(classType, methodName, parameterTypes, thisIns), null);
        JCExpression addInjectMethod = nameToExpression(addPoolMethod);
        JCMethodInvocation apply = cx.treeMaker.Apply(List.<JCExpression>nil(), addInjectMethod,
            List.from(new JCExpression[] {poolClass}));
        return cx.treeMaker.Exec(apply);
    }

}
