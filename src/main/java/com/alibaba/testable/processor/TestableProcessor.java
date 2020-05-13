package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.Testable;
import com.alibaba.testable.generator.TestableClassGenerator;
import com.alibaba.testable.translator.TestableFieldTranslator;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
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
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Testable.class);
        for (Element element : elements) {
            if (element.getKind().isClass()) {
                processClassElement(element);
            } else if (element.getKind().isField()) {
                processFieldElement(element);
            }
        }
        return true;
    }

    private void processFieldElement(Element field) {
        JCTree tree = trees.getTree(field);
        tree.accept(new TestableFieldTranslator(treeMaker));
    }

    private void processClassElement(Element clazz) {
        String packageName = elementUtils.getPackageOf(clazz).getQualifiedName().toString();
        String testableTypeName = getTestableClassName(clazz.getSimpleName());
        String fullQualityTypeName =  packageName + "." + testableTypeName;
        try {
            JavaFileObject jfo = filter.createSourceFile(fullQualityTypeName);
            Writer writer = jfo.openWriter();
            writer.write(new TestableClassGenerator(trees).fetch(clazz, packageName, testableTypeName));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTestableClassName(Name className) {
        return className.toString().replace(".", "_") + ConstPool.TESTABLE;
    }

}
