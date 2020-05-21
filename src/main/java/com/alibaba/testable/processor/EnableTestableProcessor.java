package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.EnableTestable;
import com.alibaba.testable.translator.EnableTestableTranslator;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author flin
 */
@SupportedAnnotationTypes("com.alibaba.testable.annotation.EnableTestable")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EnableTestableProcessor extends BaseProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableTestable.class);
        for (Element element : elements) {
            if (element.getKind().isClass() && isTestClass(element.getSimpleName())) {
                processClassElement((Symbol.ClassSymbol)element);
            }
        }
        return true;
    }

    private boolean isTestClass(Name name) {
        return name.toString().endsWith("Test");
    }

    private void processClassElement(Symbol.ClassSymbol clazz) {
        JCTree tree = cx.trees.getTree(clazz);
        tree.accept(new EnableTestableTranslator(getPkgName(clazz), getOriginClassName(clazz), cx));
    }

    private String getPkgName(Symbol.ClassSymbol clazz) {
        return ((Symbol.PackageSymbol)clazz.owner).fullname.toString();
    }

    private String getOriginClassName(Symbol.ClassSymbol clazz) {
        String testClassName = clazz.getSimpleName().toString();
        return testClassName.substring(0, testClassName.length() - "Test".length());
    }

}
