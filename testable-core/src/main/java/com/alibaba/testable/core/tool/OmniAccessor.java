package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.util.UnnullableMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.testable.core.constant.ConstPool.SLASH;

/**
 * @author flin
 */
public class OmniAccessor {

    private static final UnnullableMap<Class<?>, List<String>> MEMBER_INDEXES = UnnullableMap.of(new ArrayList<String>());
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
     */
    public static void set(Object target, String queryPath, Object value) {
        for (String memberPath : MEMBER_INDEXES.getOrElse(target.getClass(), generateMemberIndex(target.getClass()))) {
            if (memberPath.matches(toPattern(queryPath))) {
                Object parent = getByPath(target, toParent(memberPath));
                if (parent != null) {
                    setByPath(parent, toChild(memberPath), value);
                }
            }
        }
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
        List<Field> fields = new ArrayList<Field>(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            fields.addAll(getAllFields(clazz.getSuperclass()));
        }
        return fields;
    }

    private static String toPath(Field field) {
        return field.getName() + "{" + field.getType().getSimpleName() + "}";
    }

    private static String toPattern(String queryPath) {
        return "";
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

    private static void setByPath(Object target, String memberPath, Object value) {

    }

}
