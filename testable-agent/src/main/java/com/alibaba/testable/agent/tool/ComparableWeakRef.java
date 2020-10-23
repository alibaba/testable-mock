package com.alibaba.testable.agent.tool;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author flin
 */
public class ComparableWeakRef<T> extends WeakReference<T> {

    public ComparableWeakRef(T referent) {
        super(referent);
    }

    static public <T> Set<ComparableWeakRef<T>> getWeekHashSet() {
        return Collections.newSetFromMap(new WeakHashMap<ComparableWeakRef<T>, Boolean>());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WeakReference) {
            return this.get().equals(((WeakReference)obj).get());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.get().hashCode();
    }
}
