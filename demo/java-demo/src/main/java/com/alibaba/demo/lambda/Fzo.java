package com.alibaba.demo.lambda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author jim
 */
public class Fzo {

    public void objectStaticMethodReference() {
        Boolean aBoolean = zz().get();
        System.out.println(aBoolean);
    }

    public Optional<Boolean> zz() {
        List<List<Boolean>> zz = new ArrayList<>();
        List<Boolean> f = new ArrayList<>();
        f.add(false);
        f.add(false);
        f.add(false);
        zz.add(f);
        return zz.stream()
                .flatMap(Collection::stream)
                .reduce(Boolean::logicalAnd);
    }

}
