package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.EnableTestableInject;
import com.alibaba.testable.generator.StaticNewClassGenerator;
import com.alibaba.testable.generator.TestableClassDevRoleGenerator;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.code.Symbol;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import static javax.tools.StandardLocation.SOURCE_OUTPUT;

/**
 * @author flin
 */
@SupportedAnnotationTypes("com.alibaba.testable.annotation.EnableTestableInject")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EnableTestableInjectProcessor extends BaseProcessor {

    private static final String JAVA_POSTFIX = ".java";
    private static final String GENERATED_TEST_SOURCES = "generated-test-sources";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableTestableInject.class);
        createStaticNewClass();
        for (Element element : elements) {
            if (element.getKind().isClass()) {
                processClassElement((Symbol.ClassSymbol)element);
            }
        }
        return true;
    }

    private void createStaticNewClass() {
        if (!isStaticNewClassExist()) {
            try {
                writeSourceFile(ConstPool.NE_PKG_CLS, new StaticNewClassGenerator(cx).fetch());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isStaticNewClassExist() {
        try {
            FileObject staticNewClassFile = cx.filter.getResource(SOURCE_OUTPUT, ConstPool.NE_PKG,
                ConstPool.NE_CLS + JAVA_POSTFIX);
            return isCompilingTestClass(staticNewClassFile) || staticNewClassFile.getLastModified() > 0;
        } catch (FilerException e) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isCompilingTestClass(FileObject staticNewClassFile) {
        return staticNewClassFile.getName().contains(GENERATED_TEST_SOURCES);
    }

    private void processClassElement(Symbol.ClassSymbol clazz) {
        String packageName = cx.elementUtils.getPackageOf(clazz).getQualifiedName().toString();
        String testableTypeName = getTestableClassName(clazz.getSimpleName());
        String fullQualityTypeName =  packageName + "." + testableTypeName;
        try {
            writeSourceFile(fullQualityTypeName,
                new TestableClassDevRoleGenerator(cx).fetch(clazz, packageName, testableTypeName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSourceFile(String fullQualityTypeName, String content) throws IOException {
        JavaFileObject jfo = cx.filter.createSourceFile(fullQualityTypeName);
        Writer writer = jfo.openWriter();
        writer.write(content);
        writer.close();
    }

    private String getTestableClassName(Name className) {
        return className.toString().replace(".", "_") + ConstPool.TESTABLE;
    }

}
