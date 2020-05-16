package com.alibaba.testable.generator;

import com.alibaba.testable.generator.model.Statement;
import com.alibaba.testable.generator.statement.CallSuperMethodStatementGenerator;
import com.alibaba.testable.translator.TestableClassDevRoleTranslator;
import com.alibaba.testable.util.ConstPool;
import com.alibaba.testable.util.StringUtil;
import com.squareup.javapoet.*;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generate testable class code
 *
 * @author flin
 */
public class TestableClassDevRoleGenerator {

    private final JavacTrees trees;
    private final TreeMaker treeMaker;
    private final Names names;

    public TestableClassDevRoleGenerator(JavacTrees trees, TreeMaker treeMaker, Names names) {
        this.trees = trees;
        this.treeMaker = treeMaker;
        this.names = names;
    }

    public String fetch(Symbol.ClassSymbol clazz, String packageName, String className) {
        JCTree tree = trees.getTree(clazz);
        TestableClassDevRoleTranslator translator = new TestableClassDevRoleTranslator(treeMaker, names);
        tree.accept(translator);

        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (JCTree.JCMethodDecl method : translator.getMethods()) {
            if (isNoncallableMethod(method)) {
                continue;
            }
            if (isConstructorMethod(method)) {
                methodSpecs.add(buildConstructorMethod(clazz, method));
            } else {
                methodSpecs.add(buildMemberMethod(clazz, method));
            }
        }
        for (JCTree.JCVariableDecl field : translator.getFields()) {
            methodSpecs.add(buildFieldGetter(clazz, field));
            methodSpecs.add(buildFieldSetter(clazz, field));
        }
        methodSpecs.add(buildStubbornFieldMethod(translator.getFields()));

        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(clazz.asType());
        for (MethodSpec m : methodSpecs) {
            builder.addMethod(m);
        }
        TypeSpec testableClass = builder.build();
        JavaFile javaFile = JavaFile.builder(packageName, testableClass).build();
        return javaFile.toString();
    }

    private MethodSpec buildStubbornFieldMethod(List<JCTree.JCVariableDecl> fields) {
        List<String> fieldNames = new ArrayList<>();
        for (JCTree.JCVariableDecl f : fields)  {
            fieldNames.add("\"" + f.name.toString() + "\"");
        }
        return MethodSpec.methodBuilder(ConstPool.STUBBORN_FIELD_METHOD)
            .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.STATIC)
            .addStatement("return new $T[]{" + StringUtil.join(fieldNames, ",") + "}", String.class)
            .returns(ArrayTypeName.of(String.class))
            .build();
    }

    private MethodSpec buildFieldGetter(Symbol.ClassSymbol classElement, JCTree.JCVariableDecl field) {
        String fieldName = field.name.toString();
        return MethodSpec.methodBuilder(fieldName + ConstPool.TESTABLE_GET_METHOD_PREFIX)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.get(field.vartype.type))
            .beginControlFlow("try")
            .addStatement("$T field = $T.class.getDeclaredField(\"$N\")", Field.class, classElement.type, fieldName)
            .addStatement("field.setAccessible(true)")
            .addStatement("return ($T)field.get(this)", field.vartype.type)
            .nextControlFlow("catch ($T e)", Exception.class)
            .addStatement("e.printStackTrace()")
            .addStatement("return null")
            .endControlFlow()
            .build();
    }

    private MethodSpec buildFieldSetter(Symbol.ClassSymbol classElement, JCTree.JCVariableDecl field) {
        String fieldName = field.name.toString();
        return MethodSpec.methodBuilder(fieldName + ConstPool.TESTABLE_SET_METHOD_PREFIX)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(getParameterSpec(field))
            .returns(TypeName.VOID)
            .beginControlFlow("try")
            .addStatement("$T field = $T.class.getDeclaredField(\"$N\")", Field.class, classElement.type, fieldName)
            .addStatement("field.setAccessible(true)")
            .addStatement("field.set(this, $N)", fieldName)
            .nextControlFlow("catch ($T e)", Exception.class)
            .addStatement("e.printStackTrace()")
            .endControlFlow()
            .build();
    }

    private MethodSpec buildMemberMethod(Element classElement, JCTree.JCMethodDecl method) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.name.toString())
            .addModifiers(toPublicFlags(method.getModifiers()))
            .returns(TypeName.get(((Type.MethodType)method.sym.type).restype));
        for (JCTree.JCVariableDecl p : method.getParameters()) {
            builder.addParameter(getParameterSpec(p));
        }
        if (method.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
            builder.addException(Exception.class);
        } else {
            builder.addAnnotation(Override.class);
            for (JCTree.JCExpression exception : method.getThrows()) {
                builder.addException(TypeName.get(exception.type));
            }
        }
        addCallSuperStatements(builder, classElement, method);
        return builder.build();
    }

    private MethodSpec buildConstructorMethod(Element classElement, JCTree.JCMethodDecl method) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        for (JCTree.JCVariableDecl p : method.getParameters()) {
            builder.addParameter(getParameterSpec(p));
        }
        addCallSuperStatements(builder, classElement, method);
        return builder.build();
    }

    private void addCallSuperStatements(MethodSpec.Builder builder, Element classElement, JCTree.JCMethodDecl method) {
        String className = classElement.getSimpleName().toString();
        Statement[] statements = new CallSuperMethodStatementGenerator().fetch(className, method);
        for (Statement s : statements) {
            builder.addStatement(s.getLine(), s.getParams());
        }
    }

    private boolean isConstructorMethod(JCTree.JCMethodDecl method) {
        return method.name.toString().equals(ConstPool.CONSTRUCTOR_NAME);
    }

    private boolean isNoncallableMethod(JCTree.JCMethodDecl method) {
        return method.getModifiers().getFlags().contains(Modifier.ABSTRACT);
    }

    private Set<Modifier> toPublicFlags(JCTree.JCModifiers modifiers) {
        Set<Modifier> flags = new HashSet<>(modifiers.getFlags());
        flags.remove(Modifier.PRIVATE);
        flags.remove(Modifier.PROTECTED);
        flags.add(Modifier.PUBLIC);
        return flags;
    }

    private ParameterSpec getParameterSpec(JCTree.JCVariableDecl type) {
        return ParameterSpec.builder(TypeName.get(type.sym.type), type.name.toString()).build();
    }

}
