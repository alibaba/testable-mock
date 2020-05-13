package com.alibaba.testable.generator;

import com.alibaba.testable.generator.model.Statement;
import com.alibaba.testable.translator.TestableClassTranslator;
import com.alibaba.testable.util.ConstPool;
import com.squareup.javapoet.*;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

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
public class TestableClassGenerator {

    private final JavacTrees trees;

    public TestableClassGenerator(JavacTrees trees) {
        this.trees = trees;
    }

    public String fetch(Element clazz, String packageName, String className) {
        JCTree tree = trees.getTree(clazz);
        TestableClassTranslator translator = new TestableClassTranslator();
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
        addStatements(builder, classElement, method);
        return builder.build();
    }

    private MethodSpec buildConstructorMethod(Element classElement, JCTree.JCMethodDecl method) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC);
        for (JCTree.JCVariableDecl p : method.getParameters()) {
            builder.addParameter(getParameterSpec(p));
        }
        addStatements(builder, classElement, method);
        return builder.build();
    }

    private void addStatements(MethodSpec.Builder builder, Element classElement, JCTree.JCMethodDecl method) {
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
