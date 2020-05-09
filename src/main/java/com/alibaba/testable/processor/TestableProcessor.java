package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.Testable;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

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
        MethodSpec main = MethodSpec.methodBuilder("main")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(String[].class, "args")
            .addStatement("$T.out.println($S)", System.class, "Hello, Testable !")
            .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .build();

        TypeSpec testableClass = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(classElement.asType())
            .addMethod(constructor)
            .addMethod(main)
            .build();

        JavaFile javaFile = JavaFile.builder(packageName, testableClass)
            .build();

        return javaFile.toString();
    }

}
