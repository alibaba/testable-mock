package com.alibaba.testable.processor.model;

import com.alibaba.testable.processor.util.TestableLogger;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author flin
 */
public class TestableContext {

    /**
     * TestableLogger used for printing log during compilation
     */
    public final TestableLogger logger;

    /**
     * Filer used for generate source file
     */
    public final Filer filter;

    /**
     * Elements used for operator element
     */
    public final Elements elementUtils;

    /**
     * Types used for operator type
     */
    public final Types typeUtils;

    /**
     * JavacTrees provide the source AST
     */
    public final JavacTrees trees;

    /**
     * TreeMaker used for creating AST node
     */
    public final TreeMaker treeMaker;

    /**
     * Names used for creating resource name
     */
    public final Names names;

    public TestableContext(TestableLogger logger, Filer filter, Elements elementUtils,
                           Types typeUtils, JavacTrees trees, TreeMaker treeMaker, Names names) {
        this.logger = logger;
        this.filter = filter;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.trees = trees;
        this.treeMaker = treeMaker;
        this.names = names;
    }

    public TestableContext(TestableLogger logger, Filer filter) {
        this.logger = logger;
        this.filter = filter;
        this.elementUtils = null;
        this.typeUtils = null;
        this.trees = null;
        this.treeMaker = null;
        this.names = null;
    }

}
