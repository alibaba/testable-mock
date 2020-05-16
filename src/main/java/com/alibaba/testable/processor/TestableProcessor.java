package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.Testable;
import com.alibaba.testable.generator.StaticNewClassGenerator;
import com.alibaba.testable.generator.TestableClassDevRoleGenerator;
import com.alibaba.testable.translator.TestableClassTestRoleTranslator;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;

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
@SupportedAnnotationTypes("com.alibaba.testable.annotation.Testable")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class TestableProcessor extends BaseProcessor {

    private static final String JAVA_POSTFIX = ".java";
    private static final String GENERATED_TEST_SOURCES = "generated-test-sources";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Testable.class);
        createStaticNewClass();
        for (Element element : elements) {
            if (element.getKind().isClass()) {
                if (isTestClass(element.getSimpleName())) {
                    processTestRoleClassElement(element);
                } else {
                    processDevRoleClassElement(element);
                }
            }
        }
        return true;
    }

    private void createStaticNewClass() {
        if (!isStaticNewClassExist()) {
            try {
                writeSourceFile(ConstPool.SN_PKG_CLS, new StaticNewClassGenerator().fetch());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isStaticNewClassExist() {
        try {
            FileObject staticNewClassFile = filter.getResource(SOURCE_OUTPUT, ConstPool.SN_PKG,
                ConstPool.SN_CLS + JAVA_POSTFIX);
            return isCompilingTestClass(staticNewClassFile) || staticNewClassFile.getLastModified() > 0;
        } catch (FilerException e) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isTestClass(Name name) {
        return name.toString().endsWith("Test");
    }

    private boolean isCompilingTestClass(FileObject staticNewClassFile) {
        return staticNewClassFile.getName().contains(GENERATED_TEST_SOURCES);
    }

    private void processDevRoleClassElement(Element clazz) {
        String packageName = elementUtils.getPackageOf(clazz).getQualifiedName().toString();
        String testableTypeName = getTestableClassName(clazz.getSimpleName());
        String fullQualityTypeName =  packageName + "." + testableTypeName;
        try {
            writeSourceFile(fullQualityTypeName,
                new TestableClassDevRoleGenerator(trees, treeMaker).fetch(clazz, packageName, testableTypeName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processTestRoleClassElement(Element field) {
        JCTree tree = trees.getTree(field);
        tree.accept(new TestableClassTestRoleTranslator(treeMaker));
    }

    private void writeSourceFile(String fullQualityTypeName, String content) throws IOException {
        JavaFileObject jfo = filter.createSourceFile(fullQualityTypeName);
        Writer writer = jfo.openWriter();
        writer.write(content);
        writer.close();
    }

    private String getTestableClassName(Name className) {
        return className.toString().replace(".", "_") + ConstPool.TESTABLE;
    }

}
