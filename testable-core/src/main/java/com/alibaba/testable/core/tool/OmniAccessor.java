package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.util.FixSizeMap;
import com.sun.deploy.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.testable.core.constant.ConstPool.SLASH;

/**
 * @author flin
 */
public class OmniAccessor {

    private static final FixSizeMap<Class<?>, List<String>> MEMBER_INDEXES = new FixSizeMap<Class<?>, List<String>>(30);
    private static final String THIS_REF_PREFIX = "this$";

    /**
     * 获取第一个符合搜索路径的成员
     * @param target 目标对象
     * @param queryPath 搜索路径
     * @param clazz 目标成员类型
     * @return 返回目标成员，若不存在则返回null
     */
    public static <T> T getFirst(Object target, String queryPath, Class<T> clazz) {
        List<T> values = get(target, queryPath, clazz);
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * 获取所有符合搜索路径的成员
     * @param target 目标对象
     * @param queryPath 搜索路径
     * @param clazz 目标成员类型
     * @return 返回所有匹配的成员
     */
    public static <T> List<T> get(Object target, String queryPath, Class<T> clazz) {
        List<T> values = new ArrayList<T>();
        for (String memberPath : MEMBER_INDEXES.getOrElse(target.getClass(), generateMemberIndex(target.getClass()))) {
            if (memberPath.matches(toPattern(queryPath))) {
                T val = (T)getByPath(target, memberPath);
                if (val != null) {
                    values.add(val);
                }
            }
        }
        return values;
    }

    /**
     * 为符合搜索路径的成员赋值
     * @param target 目标对象
     * @param queryPath 搜索路径
     * @param value 新的值
     * @return 实际影响的成员个数
     */
    public static int set(Object target, String queryPath, Object value) {
        int count = 0;
        for (String memberPath : MEMBER_INDEXES.getOrElse(target.getClass(), generateMemberIndex(target.getClass()))) {
            if (memberPath.matches(toPattern(queryPath))) {
                Object parent = getByPath(target, toParent(memberPath));
                if (parent != null && setByPath(parent, toChild(memberPath), value)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static List<String> generateMemberIndex(Class<?> clazz) {
        return generateMemberIndex("", clazz);
    }

    private static List<String> generateMemberIndex(String basePath, Class<?> clazz) {
        List<Field> fields = getAllFields(clazz);
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

    private static List<Field> getAllFields(Class<?> clazz) {
        Class<?> rawClass = clazz.isArray() ? clazz.getComponentType() : clazz;
        List<Field> fields = new ArrayList<Field>(Arrays.asList(rawClass.getDeclaredFields()));
        if (rawClass.getSuperclass() != null) {
            fields.addAll(getAllFields(rawClass.getSuperclass()));
        }
        return fields;
    }

    private static String toPath(Field field) {
        return field.getName() + "{" + field.getType().getSimpleName() + "}";
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
        if (querySegment.isEmpty()) {
            return "";
        } else if (querySegment.startsWith("{")) {
            return "[^{]+" + querySegment.replace("{", "\\{").replace("}", "\\}")
                .replace("[", "\\[").replace("]", "\\]");
        } else {
            return querySegment + "\\{[^}]+\\}";
        }
    }

    private static String toChild(String memberPath) {
        return memberPath.contains(SLASH) ? memberPath.substring(memberPath.lastIndexOf(SLASH) + 1) : memberPath;
    }

    private static String toParent(String memberPath) {
        return memberPath.contains(SLASH) ? memberPath.substring(0, memberPath.lastIndexOf(SLASH)) : "";
    }

    private static Object getByPath(Object target, String memberPath) {
        return null;
    }

    private static boolean setByPath(Object target, String memberPath, Object value) {
        return true;
    }

}
