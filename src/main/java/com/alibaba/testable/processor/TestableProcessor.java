package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.Testable;
import com.alibaba.testable.generator.CallSuperMethod;
import com.alibaba.testable.translator.TestableTreeTranslator;
import com.alibaba.testable.util.ConstPool;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author flin
 */
@SupportedAnnotationTypes("com.alibaba.testable.annotation.Testable")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class TestableProcessor extends BaseProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> clazz = roundEnv.getElementsAnnotatedWith(Testable.class);
        for (Element classElement : clazz) {
            String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
            String testableTypeName = classElement.getSimpleName().toString().replace(".", "_") + "Testable";
            String fullQualityTypeName =  packageName + "." + testableTypeName;
            try {
                JavaFileObject jfo = filter.createSourceFile(fullQualityTypeName);
                Writer writer = jfo.openWriter();
                writer.write(createTestableClass(classElement, packageName, testableTypeName));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private String createTestableClass(Element classElement, String packageName, String className) {

        JCTree tree = trees.getTree(classElement);
        TestableTreeTranslator translator = new TestableTreeTranslator();
        tree.accept(translator);

        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (JCTree.JCMethodDecl method : translator.getMethods()) {
            if (method.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
                continue;
            }
            if (method.name.toString().equals(ConstPool.CONSTRUCTOR_NAME)) {
                MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);
                for (JCTree.JCVariableDecl p : method.getParameters()) {
                    builder.addParameter(getParameterSpec(p));
                }
                CallSuperMethod callSuperMethod = new CallSuperMethod(classElement.getSimpleName().toString(), method).invoke();
                builder.addStatement(callSuperMethod.getStatement(), callSuperMethod.getParams());
                methodSpecs.add(builder.build());
            } else {
                MethodSpec.Builder builder = MethodSpec.methodBuilder(method.name.toString())
                    .addModifiers(toPublicFlags(method.getModifiers()))
                    .returns(TypeName.get(((Type.MethodType)method.sym.type).restype));
                for (JCTree.JCVariableDecl p : method.getParameters()) {
                    builder.addParameter(getParameterSpec(p));
                }
                CallSuperMethod callSuperMethod = new CallSuperMethod(classElement.getSimpleName().toString(), method).invoke();
                String statement = callSuperMethod.getStatement();
                if (!method.restype.toString().equals(ConstPool.CONSTRUCTOR_VOID)) {
                    statement = "return " + statement;
                }
                if (method.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                    builder.addException(Exception.class);
                } else {
                    builder.addAnnotation(Override.class);
                    for (JCTree.JCExpression exception : method.getThrows()) {
                        builder.addException(TypeName.get(exception.type));
                    }
                }
                builder.addStatement(statement, callSuperMethod.getParams());
                methodSpecs.add(builder.build());
            }
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(classElement.asType());
        for (MethodSpec m : methodSpecs) {
            builder.addMethod(m);
        }
        TypeSpec testableClass = builder.build();
        JavaFile javaFile = JavaFile.builder(packageName, testableClass).build();
        return javaFile.toString();
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
