package com.alibaba.testable.core.matcher;

import com.alibaba.testable.core.function.MatchFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author flin
 */
public class InvocationMatcher {

    public MatchFunction matchFunction;

    private InvocationMatcher(MatchFunction matchFunction) {
        this.matchFunction = matchFunction;
    }

    public static InvocationMatcher any(MatchFunction matcher) {
        return new InvocationMatcher(matcher);
    }

    public static InvocationMatcher any() {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return true;
            }
        });
    }

    public static InvocationMatcher anyString() {
        return any(String.class);
    }

    public static InvocationMatcher anyNumber() {
        return anyTypeOf(Short.class, Integer.class, Long.class, Float.class, Double.class);
    }

    public static InvocationMatcher anyBoolean() {
        return any(Boolean.class);
    }

    public static InvocationMatcher anyByte() {
        return any(Byte.class);
    }

    public static InvocationMatcher anyChar() {
        return any(Character.class);
    }

    public static InvocationMatcher anyInt() {
        return any(Integer.class);
    }

    public static InvocationMatcher anyLong() {
        return any(Long.class);
    }

    public static InvocationMatcher anyFloat() {
        return any(Float.class);
    }

    public static InvocationMatcher anyDouble() {
        return any(Double.class);
    }

    public static InvocationMatcher anyShort() {
        return any(Short.class);
    }

    public static InvocationMatcher anyArray() {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null &&
                    value.getClass().isArray();
            }
        });
    }

    public static InvocationMatcher anyArrayOf(final Class<?> clazz) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null &&
                    value.getClass().isArray() &&
                    value.getClass().getComponentType().equals(clazz);
            }
        });
    }

    public static InvocationMatcher anyList() {
        return any(List.class);
    }

    public static InvocationMatcher anyListOf(final Class<?> clazz) {
        return anyClassWithCollectionOf(List.class, clazz);
    }

    public static InvocationMatcher anySet() {
        return any(Set.class);
    }

    public static InvocationMatcher anySetOf(final Class<?> clazz) {
        return anyClassWithCollectionOf(Set.class, clazz);
    }

    public static InvocationMatcher anyMap() {
        return any(Map.class);
    }

    public static InvocationMatcher anyMapOf(final Class<?> keyClass, final Class<?> valueClass) {
        return anyClassWithMapOf(keyClass, valueClass);
    }

    public static InvocationMatcher anyCollection() {
        return any(Collection.class);
    }

    public static InvocationMatcher anyCollectionOf(final Class<?> clazz) {
        return anyClassWithCollectionOf(Collection.class, clazz);
    }

    public static InvocationMatcher anyIterable() {
        return any(Iterable.class);
    }

    public static InvocationMatcher anyIterableOf(final Class<?> clazz) {
        return anyClassWithCollectionOf(Iterable.class, clazz);
    }

    public static InvocationMatcher any(final Class<?> clazz) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null && clazz.isAssignableFrom(value.getClass());
            }
        });
    }

    public static InvocationMatcher anyTypeOf(final Class<?>... classes) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                if (value == null) {
                    return false;
                }
                for (Class<?> c : classes) {
                    if (value.getClass().equals(c)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public static InvocationMatcher eq(final Object obj) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return obj.equals(value);
            }
        });
    }

    public static InvocationMatcher refEq(final Object obj) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return obj == value;
            }
        });
    }

    public static InvocationMatcher isNull() {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value == null;
            }
        });
    }

    public static InvocationMatcher notNull() {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null;
            }
        });
    }

    public static InvocationMatcher nullable(final Class<?> clazz) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value == null || clazz.isAssignableFrom(value.getClass());
            }
        });
    }

    public static InvocationMatcher contains(final String substring) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value instanceof String && ((String)value).contains(substring);
            }
        });
    }

    public static InvocationMatcher matches(final String regex) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value instanceof String && ((String)value).matches(regex);
            }
        });
    }

    public static InvocationMatcher endsWith(final String suffix) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value instanceof String && ((String)value).endsWith(suffix);
            }
        });
    }

    public static InvocationMatcher startsWith(final String prefix) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value instanceof String && ((String)value).startsWith(prefix);
            }
        });
    }

    private static InvocationMatcher anyClassWithCollectionOf(final Class<?> collectionClass, final Class<?> clazz) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null &&
                    collectionClass.isAssignableFrom(value.getClass()) &&
                    allElementsHasType((Collection<?>)value, clazz);
            }
        });
    }

    private static InvocationMatcher anyClassWithMapOf(final Class<?> keyClass, final Class<?> valueClass) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null &&
                    Map.class.isAssignableFrom(value.getClass()) &&
                    allElementsHasType((Map<?, ?>)value, keyClass, valueClass);
            }
        });
    }

    /**
     * Because of type erase, there's no way to directly fetch original type of collection template
     * this could be a temporary solution
     */
    private static boolean allElementsHasType(Map<?, ?> items, Class<?> keyClass, Class<?> valueClass) {
        for (Map.Entry<?, ?> e : items.entrySet()) {
            if (!(keyClass.isAssignableFrom(e.getKey().getClass()) &&
                valueClass.isAssignableFrom(e.getValue().getClass()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Because of type erase, there's no way to directly fetch original type of collection template
     * this could be a temporary solution
     */
    private static boolean allElementsHasType(Collection<?> values, Class<?> clazz) {
        for (Object v : values.toArray()) {
            if (!clazz.isAssignableFrom(v.getClass())) {
                return false;
            }
        }
        return true;
    }

}
