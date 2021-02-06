package com.alibaba.testable.processor;

import com.alibaba.testable.processor.annotation.EnablePrivateAccess;
import com.alibaba.testable.processor.constant.ConstPool;
import com.alibaba.testable.processor.model.Parameters;
import com.alibaba.testable.processor.model.TestableContext;
import com.alibaba.testable.processor.translator.EnablePrivateAccessTranslator;
import com.alibaba.testable.processor.util.JavacUtil;
import com.alibaba.testable.processor.util.TestableLogger;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author flin
 */
@SupportedAnnotationTypes("com.alibaba.testable.processor.annotation.EnablePrivateAccess")
public class EnablePrivateAccessProcessor extends AbstractProcessor {

    private static final String SRC_CLASS = "srcClass";
    private static final String VERIFY_ON_COMPILE = "verifyTargetOnCompile";

    private TestableContext cx;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JavacProcessingEnvironment javacProcessingContext = getJavacProcessingEnvironment(processingEnv);
        if (javacProcessingContext == null) {
            cx = new TestableContext(new TestableLogger(processingEnv.getMessager()), processingEnv.getFiler());
            cx.logger.info("Skip testable compile time processing");
        } else {
            cx = new TestableContext(new TestableLogger(processingEnv.getMessager()), processingEnv.getFiler(),
                processingEnv.getElementUtils(), processingEnv.getTypeUtils(), JavacTrees.instance(javacProcessingContext),
                TreeMaker.instance(javacProcessingContext.getContext()), Names.instance(javacProcessingContext.getContext()));
        }
        cx.logger.info("Testable processor initialized");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (cx.names == null) {
            return true;
        }
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnablePrivateAccess.class);
        for (Element element : elements) {
            if (element.getKind().isClass()) {
                Symbol.ClassSymbol testClass = (Symbol.ClassSymbol)element;
                Parameters parameters = getAnnotationParameters(testClass);
                processClassElement(testClass, parameters);
            }
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // always return the latest version
        return SourceVersion.values()[SourceVersion.values().length - 1];
    }

    private Parameters getAnnotationParameters(Symbol.ClassSymbol testClass) {
        Parameters parameters = new Parameters();
        for (Attribute.Compound annotation : testClass.getMetadata().getDeclarationAttributes()) {
            if (ConstPool.ENABLE_PRIVATE_ACCESS.equals(annotation.type.tsym.toString())) {
                for (Pair<Symbol.MethodSymbol, Attribute> p : annotation.values) {
                    if (SRC_CLASS.equals(p.fst.name.toString())) {
                        parameters.sourceClassName = p.snd.getValue().toString();
                    } else if (VERIFY_ON_COMPILE.equals(p.fst.name.toString())) {
                        parameters.verifyTargetExistence = (Boolean)p.snd.getValue();
                    }
                }
            }
        }
        return parameters;
    }

    private JavacProcessingEnvironment getJavacProcessingEnvironment(ProcessingEnvironment processingEnv) {
        try {
            return JavacUtil.getJavacProcessingEnvironment(processingEnv);
        } catch (Exception e) {
            return null;
        }
    }

    private void processClassElement(Symbol.ClassSymbol testClass, Parameters parameters) {
        if (cx.trees != null) {
            JCTree tree = cx.trees.getTree(testClass);
            tree.accept(new EnablePrivateAccessTranslator(cx, testClass, parameters));
        }
    }

}
