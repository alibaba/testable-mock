package com.alibaba.testable.processor;

import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.util.TestableLogger;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * @author flin
 */
public abstract class BaseProcessor extends AbstractProcessor {

    protected TestableContext cx;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Context context = ((JavacProcessingEnvironment)processingEnv).getContext();
        cx = new TestableContext(new TestableLogger(processingEnv.getMessager()), processingEnv.getFiler(),
            processingEnv.getElementUtils(), processingEnv.getTypeUtils(), JavacTrees.instance(processingEnv),
            TreeMaker.instance(context), Names.instance(context));
    }

}
