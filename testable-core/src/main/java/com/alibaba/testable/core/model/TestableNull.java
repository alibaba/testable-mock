package com.alibaba.testable.core.model;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author flin
 */
public class TestableNull implements NullType {

    @Override
    public TypeKind getKind() {
        return TypeKind.NULL;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitNull(this, p);
    }

    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return null;
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return null;
    }
}
