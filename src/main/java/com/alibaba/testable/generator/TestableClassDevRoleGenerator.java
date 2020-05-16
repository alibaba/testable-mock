package com.alibaba.testable.generator;

import com.alibaba.testable.generator.model.Statement;
import com.alibaba.testable.generator.statement.CallSuperMethodStatementGenerator;
import com.alibaba.testable.generator.statement.FieldGetterStatementGenerator;
import com.alibaba.testable.generator.statement.FieldSetterStatementGenerator;
import com.alibaba.testable.generator.statement.FieldStatementGenerator;
import com.alibaba.testable.translator.TestableClassDevRoleTranslator;
import com.alibaba.testable.util.ConstPool;
import com.squareup.javapoet.*;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
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

    public TestableClassDevRoleGenerator(JavacTrees trees, TreeMaker treeMaker) {
        this.trees = trees;
        this.treeMaker = treeMaker;
    }

    public String fetch(Element clazz, String packageName, String className) {
        JCTree tree = trees.getTree(clazz);
        TestableClassDevRoleTranslator translator = new TestableClassDevRoleTranslator(treeMaker);
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
        for (JCTree.JCVariableDecl field : translator.getPrivateFields()) {
            methodSpecs.add(buildFieldGetter(clazz, field));
            methodSpecs.add(buildFieldSetter(clazz, field));
        }

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

    private MethodSpec buildFieldGetter(Element classElement, JCTree.JCVariableDecl field) {
        return buildFieldAccessor(classElement, field, "TestableGet",
            TypeName.get(((Type.MethodType)field.type).restype), new FieldGetterStatementGenerator());
    }

    private MethodSpec buildFieldSetter(Element classElement, JCTree.JCVariableDecl field) {
        return buildFieldAccessor(classElement, field, "TestableSet",
            TypeName.VOID, new FieldSetterStatementGenerator());
    }

    private MethodSpec buildFieldAccessor(Element classElement, JCTree.JCVariableDecl field, String prefix,
                                          TypeName returnType, FieldStatementGenerator generator) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(field.name.toString() + prefix)
            .addModifiers(Modifier.PUBLIC)
            .returns(returnType);
        String className = classElement.getSimpleName().toString();
        Statement[] statements = generator.fetch(className, field);
        for (Statement s : statements) {
            builder.addStatement(s.getLine(), s.getParams());
        }
        return builder.build();
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
