package com.alibaba.testable.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * @author flin
 */
public abstract class BaseProcessor extends AbstractProcessor {

    /**
     * Messager used for printing log during compilation
     */
    protected Messager messager;

    /**
     * Filer used for generate source file
     */
    protected Filer filter;

    /**
     * JavacTrees provide the source AST
     */
    protected JavacTrees trees;

    /**
     * TreeMaker used for creating AST node
     */
    protected TreeMaker treeMaker;

    /**
     * Names used for creating resource name
     */
    protected Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Context context = ((JavacProcessingEnvironment)processingEnv).getContext();
        messager = processingEnv.getMessager();
        filter = processingEnv.getFiler();
        trees = JavacTrees.instance(processingEnv);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
    }

}
