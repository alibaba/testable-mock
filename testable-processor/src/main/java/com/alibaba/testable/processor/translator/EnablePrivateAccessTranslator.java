package com.alibaba.testable.processor.translator;

import com.alibaba.testable.processor.generator.PrivateAccessStatementGenerator;
import com.alibaba.testable.processor.model.MemberRecord;
import com.alibaba.testable.processor.model.MemberType;
import com.alibaba.testable.processor.model.Parameters;
import com.alibaba.testable.processor.model.TestableContext;
import com.alibaba.testable.processor.util.PathUtil;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.testable.processor.constant.ConstPool.TEST_POSTFIX;

/**
 * Travel AST
 *
 * @author flin
 */
public class EnablePrivateAccessTranslator extends BaseTranslator {

    private static final String IDEA_PATHS_SELECTOR = "idea.paths.selector";
    private static final String USER_DIR = "user.dir";
    private static final String GRADLE_CLASS_FOLDER = "/build/classes/java/main/";
    private static final String MAVEN_CLASS_FOLDER = "/target/classes/";

    /**
     * Name of source class
     */
    private final Name sourceClassName;
    /**
     * Fields of source class instance in the test class
     */
    private final ListBuffer<Name> sourceClassIns = new ListBuffer<Name>();
    /**
     * Member information of source class
     */
    private final MemberRecord memberRecord = new MemberRecord();

    private final PrivateAccessStatementGenerator privateAccessStatementGenerator;
    private final PrivateAccessChecker privateAccessChecker;

    public EnablePrivateAccessTranslator(TestableContext cx, Symbol.ClassSymbol clazz, Parameters p) {
        String sourceClassFullName;
        if (p.sourceClassName == null) {
            String testClassFullName = clazz.fullname.toString();
            sourceClassFullName = testClassFullName.substring(0, testClassFullName.length() - TEST_POSTFIX.length());
        } else {
            sourceClassFullName = p.sourceClassName;
        }
        String sourceClassShortName = sourceClassFullName.substring(sourceClassFullName.lastIndexOf('.') + 1);
        this.privateAccessStatementGenerator = new PrivateAccessStatementGenerator(cx);
        this.sourceClassName = cx.names.fromString(sourceClassShortName);
        try {
            Class<?> cls = getSourceClass(clazz, sourceClassFullName);
            if (cls == null) {
                cx.logger.fatal("Failed to load source class \"" + sourceClassFullName + "\"");
            } else {
                findAllPrivateMembers(cls);
            }
        } catch (Exception e) {
            // for any reason, interrupt the compile process
            cx.logger.fatal("Failed to load source class \"" + sourceClassFullName + "\": " + e);
        }
        this.privateAccessChecker = (p.verifyTargetExistence == null || p.verifyTargetExistence) ?
            new PrivateAccessChecker(cx, sourceClassShortName, memberRecord) : null;
    }

    /**
     * var = d.privateMethod(args) → var = PrivateAccessor.invoke(d, "privateMethod", args)
     */
    @Override
    public void visitVarDef(JCVariableDecl jcVariableDecl) {
        jcVariableDecl.init = checkAndExchange(jcVariableDecl.init);
        super.visitVarDef(jcVariableDecl);
        if (jcVariableDecl.vartype instanceof JCIdent &&
            ((JCIdent)jcVariableDecl.vartype).name.equals(sourceClassName)) {
            sourceClassIns.add(jcVariableDecl.name);
        }
    }

    /**
     * d.privateField = val → PrivateAccessor.set(d, "privateField", val)
     * d.privateMethod(args) → PrivateAccessor.invoke(d, "privateMethod", args)
     */
    @Override
    public void visitExec(JCExpressionStatement jcExpressionStatement) {
        // visitExec could be an assign statement to a private field
        if (jcExpressionStatement.expr instanceof JCAssign) {
            MemberType memberType = checkSetterType((JCAssign)jcExpressionStatement.expr);
            if (memberType.equals(MemberType.PRIVATE_OR_FINAL)) {
                jcExpressionStatement.expr = privateAccessStatementGenerator.fetchSetterStatement(
                    (JCAssign)jcExpressionStatement.expr);
            } else if (memberType.equals(MemberType.STATIC_PRIVATE)) {
                jcExpressionStatement.expr = privateAccessStatementGenerator.fetchStaticSetterStatement(
                    (JCAssign)jcExpressionStatement.expr);
            }
        }
        // visitExec could be an invoke
        jcExpressionStatement.expr = checkAndExchange(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * For private invoke invocation break point
     * call(d.privateMethod(args)) → call(PrivateAccessor.invoke(d, "privateMethod", args))
     */
    @Override
    public void visitApply(JCMethodInvocation tree) {
        // parameter of invocation could be an invoke or field access
        tree.args = checkAndExchange(tree.args);
        super.visitApply(tree);
    }

    /**
     * For private setter break point
     */
    @Override
    public void visitAssign(JCAssign jcAssign) {
        super.visitAssign(jcAssign);
    }

    /**
     * For private getter break point
     */
    @Override
    public void visitSelect(JCFieldAccess jcFieldAccess) {
        super.visitSelect(jcFieldAccess);
    }

    @Override
    protected JCExpression checkAndExchange(JCExpression expr) {
        // check is accessing a private field of source class
        if (expr instanceof JCFieldAccess) {
            MemberType memberType = checkGetterType((JCFieldAccess)expr);
            if (memberType.equals(MemberType.PRIVATE_OR_FINAL)) {
                expr = privateAccessStatementGenerator.fetchGetterStatement((JCFieldAccess)expr);
            } else if (memberType.equals(MemberType.STATIC_PRIVATE)) {
                expr = privateAccessStatementGenerator.fetchStaticGetterStatement((JCFieldAccess)expr);
            }
        }
        // check is invoking a private method of source class
        if (expr instanceof JCMethodInvocation) {
            JCMethodInvocation invocation = (JCMethodInvocation)expr;
            MemberType memberType = checkInvokeType(invocation);
            if (memberType.equals(MemberType.PRIVATE_OR_FINAL)) {
                expr = privateAccessStatementGenerator.fetchInvokeStatement(invocation);
            } else if (memberType.equals(MemberType.STATIC_PRIVATE)) {
                expr = privateAccessStatementGenerator.fetchStaticInvokeStatement(invocation);
            }
            if (privateAccessChecker != null) {
                privateAccessChecker.validate((JCMethodInvocation)expr);
            }
        }
        // check the casted expression
        if (expr instanceof JCTypeCast) {
            JCTypeCast typeCast = (JCTypeCast)expr;
            typeCast.expr = checkAndExchange(typeCast.expr);
        }
        return expr;
    }

    private Class<?> getSourceClass(Symbol.ClassSymbol clazz, String sourceClassFullName)
        throws MalformedURLException, ClassNotFoundException {
        Class<?> cls;
        try {
            // maven build goes here
            cls = Class.forName(sourceClassFullName);
        } catch (ClassNotFoundException e) {
            if (System.getProperty(IDEA_PATHS_SELECTOR) != null) {
                // fit for intellij build
                String sourceFileWrapperString = clazz.sourcefile.toString();
                String sourceFilePath = sourceFileWrapperString.substring(
                    sourceFileWrapperString.lastIndexOf("[") + 1, sourceFileWrapperString.indexOf("]"));
                int indexOfSrc = sourceFilePath.lastIndexOf(File.separator + "src" + File.separator);
                String basePath = sourceFilePath.substring(0, indexOfSrc);
                try {
                    String targetFolderPath = PathUtil.fitPathString(basePath + MAVEN_CLASS_FOLDER);
                    cls = loadClass(targetFolderPath, sourceClassFullName);
                } catch (ClassNotFoundException e2) {
                    String buildFolderPath = PathUtil.fitPathString(basePath + GRADLE_CLASS_FOLDER);
                    cls = loadClass(buildFolderPath, sourceClassFullName);
                }
            } else {
                // fit for gradle build
                String path = PathUtil.fitPathString("file:" + System.getProperty(USER_DIR) + GRADLE_CLASS_FOLDER);
                cls = loadClass(path, sourceClassFullName);
            }
        }
        return cls;
    }

    private Class<?> loadClass(String targetFolderPath, String sourceClassFullName)
        throws ClassNotFoundException, MalformedURLException {
        return new URLClassLoader(new URL[] {new URL(targetFolderPath)}).loadClass(sourceClassFullName);
    }

    private void findAllPrivateMembers(Class<?> cls) {
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isFinal(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())
                || Modifier.isProtected(f.getModifiers())) {
                memberRecord.privateOrFinalFields.add(f.getName());
            } else {
                memberRecord.nonPrivateNorFinalFields.add(f.getName());
            }
        }
        Method[] methods = cls.getDeclaredMethods();
        for (final Method m : methods) {
            if (Modifier.isPrivate(m.getModifiers()) || Modifier.isProtected(m.getModifiers())) {
                checkAndAdd(memberRecord.privateMethods, m.getName(), getParameterLength(m));
            } else {
                checkAndAdd(memberRecord.nonPrivateMethods, m.getName(), getParameterLength(m));
            }
        }
        if (cls.getSuperclass() != null) {
            findAllPrivateMembers(cls.getSuperclass());
        }
    }

    private void checkAndAdd(Map<String, List<Integer>> map, String key, final int value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            map.put(key, new ArrayList<Integer>() {{ add(value); }});
        }
    }

    private int getParameterLength(Method m) {
        int length = m.getParameterTypes().length;
        if (length == 0) {
            return 0;
        }
        if (m.getParameterTypes()[length - 1].getName().startsWith("[")) {
            return -(length - 1);
        } else {
            return length;
        }
    }

    private MemberType checkGetterType(JCFieldAccess access) {
        if (access.selected instanceof JCIdent && memberRecord.privateOrFinalFields.contains(access.name.toString())) {
            return checkSourceClassOrIns(((JCIdent)access.selected).name);
        }
        return MemberType.NON_PRIVATE;
    }

    private MemberType checkSetterType(JCAssign assign) {
        if (assign.lhs instanceof JCFieldAccess && ((JCFieldAccess)(assign).lhs).selected instanceof JCIdent &&
            memberRecord.privateOrFinalFields.contains(((JCFieldAccess)(assign).lhs).name.toString())) {
            return checkSourceClassOrIns(((JCIdent)((JCFieldAccess)(assign).lhs).selected).name);
        }
        return MemberType.NON_PRIVATE;
    }

    private MemberType checkInvokeType(JCMethodInvocation expr) {
        if (expr.meth instanceof JCFieldAccess && ((JCFieldAccess)(expr).meth).selected instanceof JCIdent &&
            memberRecord.privateMethods.containsKey(((JCFieldAccess)(expr).meth).name.toString())) {
            return checkSourceClassOrIns(((JCIdent)((JCFieldAccess)(expr).meth).selected).name);
        }
        return MemberType.NON_PRIVATE;
    }

    private MemberType checkSourceClassOrIns(Name name) {
        if (sourceClassName.equals(name)) {
            return MemberType.STATIC_PRIVATE;
        } else if (sourceClassIns.contains(name)) {
            return MemberType.PRIVATE_OR_FINAL;
        }
        return MemberType.NON_PRIVATE;
    }

}
