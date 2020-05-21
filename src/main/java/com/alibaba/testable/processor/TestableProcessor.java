package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.Testable;
import com.alibaba.testable.generator.StaticNewClassGenerator;
import com.alibaba.testable.generator.TestableClassDevRoleGenerator;
import com.alibaba.testable.translator.TestableClassTestRoleTranslator;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.code.Symbol;
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
                    processTestRoleClassElement((Symbol.ClassSymbol)element);
                } else {
                    processDevRoleClassElement((Symbol.ClassSymbol)element);
                }
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

    private boolean isTestClass(Name name) {
        return name.toString().endsWith("Test");
    }

    private boolean isCompilingTestClass(FileObject staticNewClassFile) {
        return staticNewClassFile.getName().contains(GENERATED_TEST_SOURCES);
    }

    private void processDevRoleClassElement(Symbol.ClassSymbol clazz) {
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

    private void processTestRoleClassElement(Symbol.ClassSymbol clazz) {
        JCTree tree = cx.trees.getTree(clazz);
        tree.accept(new TestableClassTestRoleTranslator(getPkgName(clazz), getOriginClassName(clazz), cx));
    }

    private String getPkgName(Symbol.ClassSymbol clazz) {
        return ((Symbol.PackageSymbol)clazz.owner).fullname.toString();
    }

    private String getOriginClassName(Symbol.ClassSymbol clazz) {
        String testClassName = clazz.getSimpleName().toString();
        return testClassName.substring(0, testClassName.length() - "Test".length());
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
