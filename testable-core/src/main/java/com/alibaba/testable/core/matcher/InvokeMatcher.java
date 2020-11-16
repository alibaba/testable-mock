package com.alibaba.testable.core.matcher;

import com.alibaba.testable.core.function.MatchFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author flin
 */
public class InvokeMatcher {

    public MatchFunction matchFunction;

    private InvokeMatcher(MatchFunction matchFunction) {
        this.matchFunction = matchFunction;
    }

    public static InvokeMatcher any(MatchFunction matcher) {
        return new InvokeMatcher(matcher);
    }

    public static InvokeMatcher any() {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return true;
            }
        });
    }

    public static InvokeMatcher anyString() {
        return any(String.class);
    }

    public static InvokeMatcher anyNumber() {
        return anyTypeOf(Short.class, Integer.class, Long.class, Float.class, Double.class);
    }

    public static InvokeMatcher anyBoolean() {
        return any(Boolean.class);
    }

    public static InvokeMatcher anyByte() {
        return any(Byte.class);
    }

    public static InvokeMatcher anyChar() {
        return any(Character.class);
    }

    public static InvokeMatcher anyInt() {
        return any(Integer.class);
    }

    public static InvokeMatcher anyLong() {
        return any(Long.class);
    }

    public static InvokeMatcher anyFloat() {
        return any(Float.class);
    }

    public static InvokeMatcher anyDouble() {
        return any(Double.class);
    }

    public static InvokeMatcher anyShort() {
        return any(Short.class);
    }

    public static InvokeMatcher anyArray() {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null &&
                    value.getClass().isArray();
            }
        });
    }

    public static InvokeMatcher anyArrayOf(final Class<?> clazz) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null &&
                    value.getClass().isArray() &&
                    value.getClass().getComponentType().equals(clazz);
            }
        });
    }

    public static InvokeMatcher anyList() {
        return any(List.class);
    }

    public static InvokeMatcher anyListOf(final Class<?> clazz) {
        return anyClassWithTemplateOf(List.class, clazz);
    }

    public static InvokeMatcher anySet() {
        return any(Set.class);
    }

    public static InvokeMatcher anySetOf(final Class<?> clazz) {
        return anyClassWithTemplateOf(Set.class, clazz);
    }

    public static InvokeMatcher anyMap() {
        return any(Map.class);
    }

    public static InvokeMatcher anyMapOf(final Class<?> keyClass, final Class<?> valueClass) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null &&
                    Map.class.isAssignableFrom(value.getClass()) &&
                    value.getClass().getTypeParameters().length == 2 &&
                    keyClass.isAssignableFrom(value.getClass().getTypeParameters()[0].getGenericDeclaration()) &&
                    valueClass.isAssignableFrom(value.getClass().getTypeParameters()[1].getGenericDeclaration());
            }
        });
    }

    public static InvokeMatcher anyCollection() {
        return any(Collection.class);
    }

    public static InvokeMatcher anyCollectionOf(final Class<?> clazz) {
        return anyClassWithTemplateOf(Collection.class, clazz);
    }

    public static InvokeMatcher anyIterable() {
        return any(Iterable.class);
    }

    public static InvokeMatcher anyIterableOf(final Class<?> clazz) {
        return anyClassWithTemplateOf(Iterable.class, clazz);
    }

    public static InvokeMatcher any(final Class<?> clazz) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null && clazz.isAssignableFrom(value.getClass());
            }
        });
    }

    public static InvokeMatcher anyTypeOf(final Class<?>... classes) {
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

    public static InvokeMatcher eq(final Object obj) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return obj.equals(value);
            }
        });
    }

    public static InvokeMatcher refEq(final Object obj) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return obj == value;
            }
        });
    }

    public static InvokeMatcher isNull() {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value == null;
            }
        });
    }

    public static InvokeMatcher notNull() {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null;
            }
        });
    }

    public static InvokeMatcher nullable(final Class<?> clazz) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value == null || clazz.isAssignableFrom(value.getClass());
            }
        });
    }

    public static InvokeMatcher contains(final String substring) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value instanceof String && ((String)value).contains(substring);
            }
        });
    }

    public static InvokeMatcher matches(final String regex) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value instanceof String && ((String)value).matches(regex);
            }
        });
    }

    public static InvokeMatcher endsWith(final String suffix) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value instanceof String && ((String)value).endsWith(suffix);
            }
        });
    }

    public static InvokeMatcher startsWith(final String prefix) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value instanceof String && ((String)value).startsWith(prefix);
            }
        });
    }

    private static InvokeMatcher anyClassWithTemplateOf(final Class<?> collectionClass, final Class<?> clazz) {
        return any(new MatchFunction() {
            @Override
            public boolean check(Object value) {
                return value != null &&
                    collectionClass.isAssignableFrom(value.getClass()) &&
                    value.getClass().getTypeParameters().length == 1 &&
                    clazz.isAssignableFrom(value.getClass().getTypeParameters()[0].getGenericDeclaration());
            }
        });
    }
}
