package com.alibaba.demo.lambda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author jim
 */
public class Fzo {

    public void objectStaticMethodReference() {
        consumesFunction3(Boolean::logicalAnd);
        //consumesFunction3((v1, v2) -> Boolean.logicalAnd(v1, v2));
    }

    private <R> void consumesFunction3(BiFunction<Boolean, Boolean, R> r) {
        r.apply(true, true);
    }

    public Object zz() {
        List<List<Boolean>> zz = new ArrayList<>();
        zz.add(new ArrayList<>());
        return zz.stream()
                .flatMap(Collection::stream)
                .reduce(Boolean::logicalAnd);
    }

}
