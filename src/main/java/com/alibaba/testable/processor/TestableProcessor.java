package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.Testable;
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
            info(">>> generating: " + fullQualityTypeName);
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
            if (method.name.toString().equals(ConstPool.CONSTRUCTOR_NAME)) {
                MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);
                for (JCTree.JCVariableDecl p : method.getParameters()) {
                    builder.addParameter(getParameterSpec(p.type));
                }
                methodSpecs.add(builder.build());
            } else {
                MethodSpec.Builder builder = MethodSpec.methodBuilder(method.name.toString())
                    .addModifiers(method.getModifiers().getFlags())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(method.restype.getClass());
                for (JCTree.JCVariableDecl p : method.getParameters()) {
                    builder.addParameter(getParameterSpec(p.type));
                }
                builder.addStatement("$T.out.println($S)", System.class, "Hello, Testable !");
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

    private ParameterSpec getParameterSpec(Type type) {
        return ParameterSpec.builder(String.class, "placeholder")
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

}
