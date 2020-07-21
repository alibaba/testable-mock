package testable_internal.n;

import com.alibaba.testable.util.TypeUtil;

import java.lang.Class;
import java.lang.Object;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public final class e {

    public static class p {
        public Class c;   // target instance type to new / method return type
        public Class[] a; // constructor parameter types / member method parameter types
        public Object o;  // object which provides substitution / object which provides substitution
        public String m;  // substitutional method name / original member method name

        public p(Class c, String m, Class[] a, Object o) {
            this.c = c;
            this.m = m;
            this.a = a;
            this.o = o;
        }
    }

    private static List<p> pw = new ArrayList<>();
    private static List<p> pf = new ArrayList<>();

    /**
     * add item to constructor pool
     */
    public static void aw(p np) {
        pw.add(np);
    }

    /**
     * add item to method pool
     */
    public static void af(p np) {
        pf.add(np);
    }

    public static <T> T w(Class<T> ct) {
        return wi(ct);
    }

    public static <T> T w(Class<T> ct, Object a1) {
        return wi(ct, a1);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2) {
        return wi(ct, a1, a2);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3) {
        return wi(ct, a1, a2, a3);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4) {
        return wi(ct, a1, a2, a3, a4);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4, Object a5) {
        return wi(ct, a1, a2, a3, a4, a5);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) {
        return wi(ct, a1, a2, a3, a4, a5, a6);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) {
        return wi(ct, a1, a2, a3, a4, a5, a6, a7);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
                          Object a8) {
        return wi(ct, a1, a2, a3, a4, a5, a6, a7, a8);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
                          Object a8, Object a9) {
        return wi(ct, a1, a2, a3, a4, a5, a6, a7, a8, a9);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
                          Object a8, Object a9, Object a10) {
        return wi(ct, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
                          Object a8, Object a9, Object a10, Object a11) {
        return wi(ct, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11);
    }

    public static <T> T w(Class<T> ct, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
                          Object a8, Object a9, Object a10, Object a11, Object a12) {
        return wi(ct, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12);
    }

    /**
     * substitution entry for new
     */
    public static <T> T wi(Class<T> ct, Object... as) {
        Class[] cs = TypeUtil.gcs(as);
        if (!pw.isEmpty()) {
            try {
                p pi = gpw(ct, cs);
                if (pi != null) {
                    Method m = pi.o.getClass().getDeclaredMethod(pi.m, pi.a);
                    m.setAccessible(true);
                    return (T)m.invoke(pi.o, as);
                }
            } catch (Exception e) {
                return null;
            }
        }
        try {
            Constructor c = TypeUtil.gc(ct.getConstructors(), cs);
            if (c != null) {
                return (T)c.newInstance(as);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * substitution entry for member call
     */
    public static <T> T f(Object obj, String mn, Object... as) {
        Class[] cs = TypeUtil.gcs(as);
        if (!pf.isEmpty()) {
            try {
                p pi = gpf(mn, cs);
                if (pi != null) {
                    Method m = pi.o.getClass().getDeclaredMethod(pi.m, pi.a);
                    m.setAccessible(true);
                    return (T)m.invoke(pi.o, as);
                }
            } catch (Exception e) {
                return null;
            }
        }
        try {
            Method m = TypeUtil.gm(obj.getClass().getDeclaredMethods(), mn, cs);
            if (m != null) {
                m.setAccessible(true);
                return (T)m.invoke(obj, as);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * get from method pool by key
     */
    private static p gpf(String mn, Class[] cs) {
        for (p f : pf) {
            if (f.m.equals(mn) && TypeUtil.te(f.a, cs)) {
                return f;
            }
        }
        return null;
    }

    /**
     * get from constructor pool by key
     */
    private static p gpw(Class ct, Class[] cs) {
        for (p w : pw) {
            if (w.c.equals(ct) && TypeUtil.te(w.a, cs)) {
                return w;
            }
        }
        return null;
    }

}
