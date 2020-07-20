package com.alibaba.testable.processor;

import com.alibaba.testable.annotation.EnableTestableInject;
import com.alibaba.testable.translator.EnableTestableInjectTranslator;
import com.alibaba.testable.translator.MethodRecordTranslator;
import com.alibaba.testable.util.ConstPool;
import com.alibaba.testable.util.ResourceUtil;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Set;

/**
 * @author flin
 */
@SupportedAnnotationTypes("com.alibaba.testable.annotation.EnableTestableInject")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EnableTestableInjectProcessor extends BaseProcessor {

    private static final String JAVA_POSTFIX = ".java";
    private static final String AGENT_TARGET_FOLDER = "generated_testable";
    private static final String AGENT_TARGET_FILE = "agent.jar";
    private static final String AGENT_SOURCE_FILE = "testable-agent.jar";
    private static final String NE_SOURCE_FILE = ConstPool.NE_CLS + JAVA_POSTFIX;
    private static boolean hasFirstClassCompiled = false;

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
        if (!checkFirstClassCompiled()) {
            try {
                writeBinaryFile(AGENT_TARGET_FOLDER, AGENT_TARGET_FILE, ResourceUtil.fetchBinary(AGENT_SOURCE_FILE));
                writeSourceFile(ConstPool.NE_PKG_CLS, ResourceUtil.fetchText(NE_SOURCE_FILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkFirstClassCompiled() {
        if (!hasFirstClassCompiled) {
            hasFirstClassCompiled = true;
            return false;
        }
        return true;
    }

    private void processClassElement(Symbol.ClassSymbol clazz) {
        JCTree tree = cx.trees.getTree(clazz);
        MethodRecordTranslator methodRecordTranslator = new MethodRecordTranslator();
        tree.accept(methodRecordTranslator);
        tree.accept(new EnableTestableInjectTranslator(cx, methodRecordTranslator.getMethods()));
    }

    private void writeSourceFile(String fullQualityTypeName, String content) throws IOException {
        JavaFileObject jfo = cx.filter.createSourceFile(fullQualityTypeName);
        Writer writer = jfo.openWriter();
        writer.write(content);
        writer.flush();
        writer.close();
    }

    private void writeBinaryFile(String path, String fileName, byte[] content) throws IOException {
        FileObject resource = cx.filter.createResource(StandardLocation.SOURCE_OUTPUT, path, fileName);
        OutputStream out = resource.openOutputStream();
        out.write(content);
        out.flush();
        out.close();
    }

}
