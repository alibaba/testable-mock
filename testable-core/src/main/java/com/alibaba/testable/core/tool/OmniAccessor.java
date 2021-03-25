package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.error.NoSuchMemberError;
import com.alibaba.testable.core.util.CollectionUtil;
import com.alibaba.testable.core.util.FixSizeMap;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static com.alibaba.testable.core.constant.ConstPool.SLASH;

/**
 * @author flin
 */
public class OmniAccessor {

    private static final FixSizeMap<Class<?>, List<String>> MEMBER_INDEXES = new FixSizeMap<Class<?>, List<String>>(30);
    private static final String THIS_REF_PREFIX = "this$";
    private static final String PATTERN_PREFIX = ".*/";
    private static final String REGEX_ANY_NAME = "[^/]+";
    private static final String REGEX_ANY_FIELD_NAME = "[^{]*";
    private static final String REGEX_ANY_CLASS_NAME = "[^}]*";
    private static final String REGEX_ANY_FIELD = "[^{]+";
    private static final String REGEX_ANY_CLASS = "\\{[^}]+\\}";
    private static final String BRACE_START = "{";
    private static final String ESCAPE = "\\";
    private static final String BRACE_END = "}";
    private static final String BRACKET_START = "[";
    private static final String BRACKET_END = "]";
    private static final String STAR = "*";

    private OmniAccessor() {}

    /**
     * 获取第一个符合搜索路径的成员
     *
     * @param target    目标对象
     * @param queryPath 搜索路径
     * @return 返回目标成员，若不存在则返回null
     */
    public static <T> T getFirst(Object target, String queryPath) {
        List<T> values = get(target, queryPath);
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * 获取所有符合搜索路径的成员
     *
     * @param target    目标对象
     * @param queryPath 搜索路径
     * @return 返回所有匹配的成员
     */
    public static <T> List<T> get(Object target, String queryPath) {
        List<T> values = new ArrayList<T>();
        for (String memberPath : MEMBER_INDEXES.getOrElse(target.getClass(), generateMemberIndex(target.getClass()))) {
            if (memberPath.matches(toPattern(queryPath))) {
                try {
                    List<T> elements = getByPath(target, memberPath, queryPath);
                    if (!elements.isEmpty()) {
                        values.addAll(elements);
                    }
                } catch (NoSuchFieldException e) {
                    // continue
                } catch (IllegalAccessException e) {
                    // continue
                }
            }
        }
        if (values.isEmpty()) {
            throw new NoSuchMemberError("Query \"" + queryPath + "\"" + " does not match any member!");
        }
        return values;
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
                    List<Object> parent = getByPath(target, toParent(memberPath), toParent(queryPath));
                    if (!parent.isEmpty()) {
                        for (Object p : parent) {
                            if (setByPathSegment(p, toChild(memberPath), toChild(queryPath), value)) {
                                count++;
                            }
                        }
                    }
                } catch (NoSuchFieldException e) {
                    // continue
                } catch (IllegalAccessException e) {
                    // continue
                }
            }
        }
        if (count == 0) {
            throw new NoSuchMemberError("Query \"" + queryPath + "\"" + " does not match any member!");
        }
        return count;
    }

    private static List<String> generateMemberIndex(Class<?> clazz) {
        return generateMemberIndex(clazz, "", new HashSet<Class<?>>(6));
    }

    private static List<String> generateMemberIndex(Class<?> clazz, String basePath, Set<Class<?>> classPool) {
        if (TypeUtil.isBasicType(clazz)) {
            return Collections.emptyList();
        }
        classPool.add(clazz);
        List<String> paths = new ArrayList<String>();
        for (Field f : TypeUtil.getAllFields(clazz)) {
            if (!classPool.contains(f.getType()) && !f.getName().startsWith(THIS_REF_PREFIX)) {
                String fullPath = basePath + SLASH + toPath(f);
                paths.add(fullPath);
                paths.addAll(generateMemberIndex(f.getType(), fullPath, classPool));
            }
        }
        classPool.remove(clazz);
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
        return PATTERN_PREFIX + CollectionUtil.join(Arrays.asList(patternSegments), SLASH);
    }

    private static String toSinglePattern(String querySegment) {
        if (querySegment.endsWith(BRACKET_END)) {
            querySegment = querySegment.substring(0, querySegment.lastIndexOf(BRACKET_START));
        }
        if (querySegment.isEmpty()) {
            return "";
        } else if (querySegment.equals(STAR)) {
            return REGEX_ANY_NAME;
        } else if (querySegment.startsWith(BRACE_START)) {
            return REGEX_ANY_FIELD + querySegment.replace(BRACE_START, ESCAPE + BRACE_START)
                .replace(BRACE_END, ESCAPE + BRACE_END)
                .replace(BRACKET_START, ESCAPE + BRACKET_START)
                .replace(BRACKET_END, ESCAPE + BRACKET_END)
                .replace(STAR, REGEX_ANY_CLASS_NAME);
        } else {
            return querySegment.replace(STAR, REGEX_ANY_FIELD_NAME) + REGEX_ANY_CLASS;
        }
    }

    private static String toChild(String memberPath) {
        return memberPath.contains(SLASH) ? memberPath.substring(memberPath.lastIndexOf(SLASH) + 1) : memberPath;
    }

    private static String toParent(String memberPath) {
        return memberPath.contains(SLASH) ? memberPath.substring(0, memberPath.lastIndexOf(SLASH)) : "";
    }

    private static String extraNameFromMemberRecord(String memberSegment) {
        return memberSegment.substring(0, memberSegment.indexOf(BRACE_START));
    }

    private static int extraIndexFromQuery(String query) {
        return query.endsWith(BRACKET_END)
            ? Integer.parseInt(query.substring(query.lastIndexOf(BRACKET_START) + 1, query.length() - 1))
            : -1;
    }

    private static <T> List<T> getByPath(Object target, String memberPath, String queryPath)
        throws NoSuchFieldException, IllegalAccessException {
        String[] memberSegments = memberPath.substring(1).split(SLASH);
        String[] querySegments = queryPath.split(SLASH);
        if (memberSegments.length < querySegments.length) {
            return Collections.emptyList();
        }
        String[] querySegmentsWithPadding = calculateFullQueryPath(querySegments, memberSegments);
        return getBySegment(target, memberSegments, querySegmentsWithPadding, 0);
    }

    private static <T> List<T> getBySegment(Object target, String[] memberSegments, String[] querySegments, int n)
        throws IllegalAccessException {
        if (target == null) {
            return Collections.emptyList();
        }
        if (memberSegments.length == n) {
            int nth = extraIndexFromQuery(querySegments[n]);
            return Collections.singletonList((T)(nth > 0 ? Array.get(target, nth) : target));
        }
        int nth = extraIndexFromQuery(querySegments[n]);
        String fieldName = extraNameFromMemberRecord(memberSegments[n]);
        List<T> nexts = new ArrayList<T>();
        if (target.getClass().isArray()) {
            if (nth < 0) {
                for (int i = 0; i < Array.getLength(target); i++) {
                    List<T> all = getBySegment(getFieldValue(Array.get(target, i), fieldName, i),
                        memberSegments, querySegments, n + 1);
                    nexts.addAll(all);
                }
            } else {
                List <T> all = getBySegment(getFieldValue(Array.get(target, nth), fieldName, nth),
                    memberSegments, querySegments, n + 1);
                nexts.addAll(all);
            }
        } else {
            List <T> all = getBySegment(getFieldValue(target, fieldName, nth), memberSegments, querySegments, n + 1);
            nexts.addAll(all);
        }
        return nexts;
    }

    private static Object getFieldValue(Object obj, String name, int nth) throws IllegalAccessException {
        if (obj == null) {
            return null;
        }
        Field field = TypeUtil.getFieldByName(obj.getClass(), name);
        field.setAccessible(true);
        if (field.getType().isArray() && nth >= 0) {
            Object f = field.get(obj);
            return Array.get(f, nth);
        } else {
            return field.get(obj);
        }
    }

    private static String[] calculateFullQueryPath(String[] querySegments, String[] memberSegments) {
        String[] fullQuerySegments = new String[memberSegments.length + 1];
        for (int i = 0; i <= memberSegments.length - querySegments.length; i++) {
            fullQuerySegments[i] = "";
        }
        System.arraycopy(querySegments, 0, fullQuerySegments, memberSegments.length - querySegments.length + 1,
            querySegments.length);
        return fullQuerySegments;
    }

    private static boolean setByPathSegment(Object target, String memberSegment, String querySegment, Object value)
        throws IllegalAccessException {
        String name = extraNameFromMemberRecord(memberSegment);
        int nth = extraIndexFromQuery(querySegment);
        boolean isFieldMatch = false;
        if (target.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(target); i++) {
                isFieldMatch |= setFieldByName(Array.get(target, i), name, nth, value);
            }
        } else {
            isFieldMatch = setFieldByName(target, name, nth, value);
        }
        return isFieldMatch;
    }

    private static boolean setFieldByName(Object target, String name, int nth, Object value) throws IllegalAccessException {
        Field field = TypeUtil.getFieldByName(target.getClass(), name);
        if (field == null) {
            return false;
        }
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
        return true;
    }

}
