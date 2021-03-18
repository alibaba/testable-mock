package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.util.FixSizeMap;
import com.alibaba.testable.core.util.TypeUtil;
import com.sun.deploy.util.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.alibaba.testable.core.constant.ConstPool.SLASH;

/**
 * @author flin
 */
public class OmniAccessor {

    private static final FixSizeMap<Class<?>, List<String>> MEMBER_INDEXES = new FixSizeMap<Class<?>, List<String>>(30);
    private static final String THIS_REF_PREFIX = "this$";
    private static final String REGEX_ANY_CLASS = "\\{[^}]+\\}";
    private static final String REGEX_ANY_NAME = "[^{]+";
    private static final String BRACE_START = "{";
    private static final String ESCAPE = "\\";
    private static final String BRACE_END = "}";
    private static final String BRACKET_START = "[";
    private static final String BRACKET_END = "]";

    private OmniAccessor() {}

    /**
     * 获取第一个符合搜索路径的成员
     *
     * @param target    目标对象
     * @param queryPath 搜索路径
     * @return 返回目标成员，若不存在则返回null
     */
    public static <T> T getFirst(Object target, String queryPath) {
        T[] values = get(target, queryPath);
        return values.length == 0 ? null : values[0];
    }

    /**
     * 获取所有符合搜索路径的成员
     *
     * @param target    目标对象
     * @param queryPath 搜索路径
     * @return 返回所有匹配的成员
     */
    public static <T> T[] get(Object target, String queryPath) {
        List<T> values = new ArrayList<T>();
        for (String memberPath : MEMBER_INDEXES.getOrElse(target.getClass(), generateMemberIndex(target.getClass()))) {
            if (memberPath.matches(toPattern(queryPath))) {
                try {
                    T val = (T)getByPath(target, memberPath, queryPath);
                    if (val != null) {
                        values.add(val);
                    }
                } catch (NoSuchFieldException e) {
                    // continue
                } catch (IllegalAccessException e) {
                    // continue
                }
            }
        }
        return (T[])values.toArray();
    }

    /**
     * 为符合搜索路径的成员赋值
     *
     * @param target    目标对象
     * @param queryPath 搜索路径
     * @param value     新的值
     * @return 实际影响的成员个数
     */
    public static int set(Object target, String queryPath, Object value) {
        int count = 0;
        for (String memberPath : MEMBER_INDEXES.getOrElse(target.getClass(), generateMemberIndex(target.getClass()))) {
            if (memberPath.matches(toPattern(queryPath))) {
                try {
                    Object parent = getByPath(target, toParent(memberPath), toParent(queryPath));
                    if (parent != null) {
                        setByPathSegment(parent, toChild(memberPath), toChild(queryPath), value);
                        count++;
                    }
                } catch (NoSuchFieldException e) {
                    // continue
                } catch (IllegalAccessException e) {
                    // continue
                }
            }
        }
        return count;
    }

    private static List<String> generateMemberIndex(Class<?> clazz) {
        return generateMemberIndex("", clazz);
    }

    private static List<String> generateMemberIndex(String basePath, Class<?> clazz) {
        if (clazz.isEnum()) {
            return Collections.emptyList();
        }
        List<Field> fields = TypeUtil.getAllFields(clazz);
        List<String> paths = new ArrayList<String>();
        for (Field f : fields) {
            if (!f.getName().startsWith(THIS_REF_PREFIX)) {
                String fullPath = basePath + SLASH + toPath(f);
                paths.add(fullPath);
                paths.addAll(generateMemberIndex(fullPath, f.getType()));
            }
        }
        return paths;
    }

    private static String toPath(Field field) {
        return field.getName() + BRACE_START + field.getType().getSimpleName() + BRACE_END;
    }

    private static String toPattern(String queryPath) {
        String[] querySegments = queryPath.split(SLASH);
        String[] patternSegments = new String[querySegments.length];
        for (int i = 0; i < querySegments.length; i++) {
            patternSegments[i] = toSinglePattern(querySegments[i]);
        }
        return StringUtils.join(Arrays.asList(patternSegments), SLASH);
    }

    private static String toSinglePattern(String querySegment) {
        if (querySegment.endsWith(BRACKET_END)) {
            querySegment = querySegment.substring(0, querySegment.lastIndexOf(BRACKET_START));
        }
        if (querySegment.isEmpty()) {
            return "";
        } else if (querySegment.startsWith(BRACE_START)) {
            return REGEX_ANY_NAME + querySegment.replace(BRACE_START, ESCAPE + BRACE_START)
                .replace(BRACE_END, ESCAPE + BRACE_END)
                .replace(BRACKET_START, ESCAPE + BRACKET_START)
                .replace(BRACKET_END, ESCAPE + BRACKET_END);
        } else {
            return querySegment + REGEX_ANY_CLASS;
        }
    }

    private static String toChild(String memberPath) {
        return memberPath.contains(SLASH) ? memberPath.substring(memberPath.lastIndexOf(SLASH) + 1) : memberPath;
    }

    private static String toParent(String memberPath) {
        return memberPath.contains(SLASH) ? memberPath.substring(0, memberPath.lastIndexOf(SLASH)) : "";
    }

    private static int extraIndexFromQuery(String query) {
        return query.endsWith(BRACKET_END)
            ? Integer.parseInt(query.substring(query.lastIndexOf(BRACKET_START) + 1, query.length() - 1))
            : -1;
    }

    private static Object getByPath(Object target, String memberPath, String queryPath)
        throws NoSuchFieldException, IllegalAccessException {
        String[] memberSegments = memberPath.split(SLASH);
        String[] querySegments = calculateFullQueryPath(queryPath.split(SLASH), memberSegments);
        Object obj = target;
        String name;
        int nth;
        Field field;
        for (int i = 0; i < memberSegments.length; i++) {
            name = memberSegments[i].substring(0, memberSegments[i].indexOf(BRACE_START));
            nth = extraIndexFromQuery(querySegments[i]);
            field = TypeUtil.getFieldByName(obj.getClass(), name);
            field.setAccessible(true);
            if (field.getType().isArray() && nth >= 0) {
                Object f = field.get(obj);
                obj = Array.get(f, nth);
            } else {
                obj = field.get(obj);
            }
        }
        return obj;
    }

    private static String[] calculateFullQueryPath(String[] querySegments, String[] memberSegments) {
        assert memberSegments.length >= querySegments.length;
        ;
        if (memberSegments.length > querySegments.length) {
            String[] fullQuerySegments = new String[memberSegments.length];
            for (int i = 0; i < querySegments.length; i++) {
                fullQuerySegments[i] = "";
            }
            System.arraycopy(querySegments, 0, fullQuerySegments, querySegments.length,
                memberSegments.length - querySegments.length);
            return fullQuerySegments;
        }
        return querySegments;
    }

    private static void setByPathSegment(Object target, String memberSegment, String querySegment, Object value)
        throws IllegalAccessException {
        String name = memberSegment.substring(0, memberSegment.indexOf(BRACE_START));
        int nth = extraIndexFromQuery(querySegment);
        Field field = TypeUtil.getFieldByName(target.getClass(), name);
        field.setAccessible(true);
        if (field.getType().isArray()) {
            Object f = field.get(target);
            if (nth >= 0) {
                Array.set(f, nth, value);
            } else {
                for (int i = 0; i < Array.getLength(f); i++) {
                    Array.set(f, i, value);
                }
            }
        } else {
            field.set(target, value);
        }
    }

}
