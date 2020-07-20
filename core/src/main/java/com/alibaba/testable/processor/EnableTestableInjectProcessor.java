package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.EnableTestableInject;
import com.alibaba.testable.translator.EnableTestableInjectTranslator;
import com.alibaba.testable.translator.MethodRecordTranslator;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author flin
 */
@SupportedAnnotationTypes("com.alibaba.testable.annotation.EnableTestableInject")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EnableTestableInjectProcessor extends BaseProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableTestableInject.class);
        for (Element element : elements) {
            if (element.getKind().isClass()) {
                processClassElement((Symbol.ClassSymbol)element);
            }
        }
        return true;
    }

    private void processClassElement(Symbol.ClassSymbol clazz) {
        JCTree tree = cx.trees.getTree(clazz);
        MethodRecordTranslator methodRecordTranslator = new MethodRecordTranslator();
        tree.accept(methodRecordTranslator);
        tree.accept(new EnableTestableInjectTranslator(cx, methodRecordTranslator.getMethods()));
    }

}
