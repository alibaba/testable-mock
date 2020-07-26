package com.alibaba.testable.core.processor;

import com.alibaba.testable.core.annotation.EnableTestable;
import com.alibaba.testable.core.translator.EnableTestableTranslator;
import com.alibaba.testable.core.util.ConstPool;
import com.alibaba.testable.core.util.ResourceUtil;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * @author flin
 */
@SupportedAnnotationTypes("com.alibaba.testable.core.annotation.EnableTestable")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EnableTestableProcessor extends BaseProcessor {

    private static final String TESTABLE_AGENT_JAR = "testable-agent.jar";
    private static boolean hasFirstClassCompiled = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        createTestableAgentJar();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableTestable.class);
        for (Element element : elements) {
            if (element.getKind().isClass() && isTestClass(element.getSimpleName())) {
                processClassElement((Symbol.ClassSymbol)element);
            }
        }
        return true;
    }

    private boolean isTestClass(Name name) {
        return name.toString().endsWith(ConstPool.TEST_POSTFIX);
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
        return testClassName.substring(0, testClassName.length() - ConstPool.TEST_POSTFIX.length());
    }

    private void createTestableAgentJar() {
        if (!checkFirstClassCompiled()) {
            byte[] bytes = ResourceUtil.fetchBinary(TESTABLE_AGENT_JAR);
            if (bytes.length == 0) {
                cx.logger.error("Failed to generate testable agent jar");
            }
            writeBinaryFile("", TESTABLE_AGENT_JAR, bytes);
        }
    }

    private boolean checkFirstClassCompiled() {
        if (!hasFirstClassCompiled) {
            hasFirstClassCompiled = true;
            return false;
        }
        return true;
    }

    private void writeBinaryFile(String path, String fileName, byte[] content) {
        try {
            FileObject resource = cx.filter.createResource(StandardLocation.SOURCE_OUTPUT, path, fileName);
            OutputStream out = resource.openOutputStream();
            out.write(content);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            cx.logger.error("Failed to write " + fileName);
        }
    }
}
