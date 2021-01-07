package com.alibaba.testable.processor.translator;

import com.alibaba.testable.processor.constant.ConstPool;
import com.alibaba.testable.processor.generator.PrivateAccessStatementGenerator;
import com.alibaba.testable.processor.model.MemberType;
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
     * Record private and final fields
     */
    private final ListBuffer<String> privateOrFinalFields = new ListBuffer<String>();
    /**
     * Record private methods
     */
    private final ListBuffer<String> privateMethods = new ListBuffer<String>();

    private final PrivateAccessStatementGenerator privateAccessStatementGenerator;

    public EnablePrivateAccessTranslator(Symbol.ClassSymbol clazz, TestableContext cx) {
        String pkgName = ((Symbol.PackageSymbol)clazz.owner).fullname.toString();
        String testClassName = clazz.getSimpleName().toString();
        String sourceClass = testClassName.substring(0, testClassName.length() - ConstPool.TEST_POSTFIX.length());
        this.privateAccessStatementGenerator = new PrivateAccessStatementGenerator(cx);
        this.sourceClassName = cx.names.fromString(sourceClass);
        try {
            Class<?> cls = null;
            String sourceClassFullName = pkgName + "." + sourceClass;
            try {
                // maven build goes here
                cls = Class.forName(sourceClassFullName);
            } catch (ClassNotFoundException e) {
                if (System.getProperty(IDEA_PATHS_SELECTOR) != null) {
                    // fit for intellij 2020.3+
                    String sourceFileWrapperString = clazz.sourcefile.toString();
                    String sourceFilePath = sourceFileWrapperString.substring(
                        sourceFileWrapperString.lastIndexOf("[") + 1, sourceFileWrapperString.indexOf("]"));
                    int indexOfSrc = sourceFilePath.lastIndexOf(File.separator + "src" + File.separator);
                    String basePath = sourceFilePath.substring(0, indexOfSrc);
                    String targetFolderPath = PathUtil.fitPathString(basePath + MAVEN_CLASS_FOLDER);
                    try {
                        cls = loadClass(targetFolderPath, sourceClassFullName);
                    } catch (ClassNotFoundException e2) {
                        targetFolderPath = PathUtil.fitPathString(basePath + GRADLE_CLASS_FOLDER);
                        cls = loadClass(targetFolderPath, sourceClassFullName);
                    }
                } else {
                    // fit for gradle build
                    String path = PathUtil.fitPathString("file:" + System.getProperty(USER_DIR) + GRADLE_CLASS_FOLDER);
                    cls = loadClass(path, sourceClassFullName);
                }
            }
            if (cls == null) {
                System.err.println("Failed to load source class: " + sourceClassFullName);
                return;
            }
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isFinal(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())) {
                    privateOrFinalFields.add(f.getName());
                }
            }
            Method[] methods = cls.getDeclaredMethods();
            for (Method m : methods) {
                if (Modifier.isPrivate(m.getModifiers())) {
                    privateMethods.add(m.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            // recursively check it parameters
            invocation.args = checkAndExchange(invocation.args);
            MemberType memberType = checkInvokeType(invocation);
            if (memberType.equals(MemberType.PRIVATE_OR_FINAL)) {
                expr = privateAccessStatementGenerator.fetchInvokeStatement(invocation);
            } else if (memberType.equals(MemberType.STATIC_PRIVATE)) {
                expr = privateAccessStatementGenerator.fetchStaticInvokeStatement(invocation);
            }
        }
        // check the casted expression
        if (expr instanceof JCTypeCast) {
            JCTypeCast typeCast = (JCTypeCast)expr;
            typeCast.expr = checkAndExchange(typeCast.expr);
        }
        return expr;
    }

    private Class<?> loadClass(String targetFolderPath, String sourceClassFullName)
        throws ClassNotFoundException, MalformedURLException {
        return new URLClassLoader(new URL[] {new URL(targetFolderPath)}).loadClass(sourceClassFullName);
    }

    private MemberType checkGetterType(JCFieldAccess access) {
        if (access.selected instanceof JCIdent && privateOrFinalFields.contains(access.name.toString())) {
            return checkSourceClassOrIns(((JCIdent)access.selected).name);
        }
        return MemberType.NONE_PRIVATE;
    }

    private MemberType checkSetterType(JCAssign assign) {
        if (assign.lhs instanceof JCFieldAccess && ((JCFieldAccess)(assign).lhs).selected instanceof JCIdent &&
            privateOrFinalFields.contains(((JCFieldAccess)(assign).lhs).name.toString())) {
            return checkSourceClassOrIns(((JCIdent)((JCFieldAccess)(assign).lhs).selected).name);
        }
        return MemberType.NONE_PRIVATE;
    }

    private MemberType checkInvokeType(JCMethodInvocation expr) {
        if (expr.meth instanceof JCFieldAccess && ((JCFieldAccess)(expr).meth).selected instanceof JCIdent &&
            privateMethods.contains(((JCFieldAccess)(expr).meth).name.toString())) {
            return checkSourceClassOrIns(((JCIdent)((JCFieldAccess)(expr).meth).selected).name);
        }
        return MemberType.NONE_PRIVATE;
    }

    private MemberType checkSourceClassOrIns(Name name) {
        if (sourceClassName.equals(name)) {
            return MemberType.STATIC_PRIVATE;
        } else if (sourceClassIns.contains(name)) {
            return MemberType.PRIVATE_OR_FINAL;
        }
        return MemberType.NONE_PRIVATE;
    }

}
