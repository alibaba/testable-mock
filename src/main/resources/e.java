package n;

import com.sun.tools.javac.util.Pair;

import java.lang.Class;
import java.lang.Object;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public final class e {

  /**
   * key for contructor pool
   */
  public static class wk {
    public Class c;   // target instance type to new
    public Class[] a; // constructor parameter types

    public wk(Class c, Class[] a) {
      this.c = c;
      this.a = a;
    }

    @Override
    public boolean equals(Object o) {
      return o.getClass().equals(e.wk.class) && c.equals(((e.wk)o).c) && Arrays.equals(a, ((e.wk)o).a);
    }

    @Override
    public int hashCode() {
      return 31 * c.hashCode() + Arrays.hashCode(a);
    }
  }

  /**
   * key for method pool
   */
  public static class xk {
    public String m;  // original member method name
    public Class[] a; // member method parameter types

    public xk(String m, Class[] a) {
      this.m = m;
      this.a = a;
    }

    @Override
    public boolean equals(Object o) {
      return o.getClass().equals(e.xk.class) && m.equals(((e.xk)o).m) && Arrays.equals(a, ((e.xk)o).a);
    }

    @Override
    public int hashCode() {
      return 31 * m.hashCode() + Arrays.hashCode(a);
    }
  }

  /**
   * value for contructor pool
   */
  public static class wv {
    public Object o;  // object which provides substitution
    public String m;  // substitutional method name

    public wv(Object o, String m) {
      this.o = o;
      this.m = m;
    }
  }

  /**
   * value for member method pool
   */
  public static class xv {
    public Object o;  // object which provides substitution
    public Class c;   // method return type

    public xv(Object o, Class c) {
      this.o = o;
      this.c = c;
    }
  }

  private static Map<wk, wv> wp = new HashMap<>();
  private static Map<xk, xv> xp = new HashMap<>();

  /**
   * add item to contructor pool
   */
  public static void wa(wk k, wv v) {
    wp.put(k, v);
  }

  /**
   * add item to method pool
   */
  public static void xa(xk k, xv v) {
    xp.put(k, v);
  }

  /**
   * subsitituion entry for new
   */
  public static <T> T w(Class<T> ct, Object... as) {
    Class[] cs = new Class[as.length];
    for (int i = 0; i < cs.length; i++) {
      cs[i] = as[i].getClass();
    }
    if (!wp.isEmpty()) {
      try {
        Pair<wk, wv> p = gwp(new wk(ct, cs));
        if (p != null) {
          Method m = p.snd.o.getClass().getDeclaredMethod(p.snd.m, p.fst.a);
          m.setAccessible(true);
          return (T)m.invoke(p.snd.o, as);
        }
      } catch (Exception e) {
        return null;
      }
    }
    try {
      Constructor c = gc(ct.getConstructors(), cs);
      if (c != null) {
        return (T)c.newInstance(as);
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  /**
   * subsitituion entry for member call
   */
  public static <T> T x(Object obj, String method, Object... as) {
    Class[] cs = gcs(as);
    if (!xp.isEmpty()) {
      try {
        Pair<xk, xv> p = gxp(new xk(method, cs));
        if (p != null) {
          Method m = p.snd.o.getClass().getDeclaredMethod(p.fst.m, p.fst.a);
          m.setAccessible(true);
          return (T)m.invoke(p.snd.o, as);
        }
      } catch (Exception e) {
        return null;
      }
    }
    try {
      Method m = gm(obj.getClass().getDeclaredMethods(), method, cs);
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
   * get classes of parameter objects
   */
  private static Class[] gcs(Object[] as) {
    Class[] cs = new Class[as.length];
    for (int i = 0; i < cs.length; i++) {
      cs[i] = as[i].getClass();
    }
    return cs;
  }

  /**
   * get method by parameter matching
   */
  private static Method gm(Method[] methods, String name, Class[] cs) {
    for (Method m : methods) {
      if (m.getName().equals(name) && te(m.getParameterTypes(), cs)) {
        return m;
      }
    }
    return null;
  }

  /**
   * get from method pool by key
   */
  private static Pair<xk, xv> gxp(xk k1) {
    for (xk k2 : xp.keySet()) {
      if (k1.m.equals(k2.m) && te(k2.a, k1.a)) {
        return Pair.of(k2, xp.get(k2));
      }
    }
    return null;
  }

  /**
   * get contructor by parameter matching
   */
  private static Constructor gc(Constructor<?>[] constructors, Class[] cs) {
    for (Constructor c : constructors) {
      if (te(c.getParameterTypes(), cs)) {
        return c;
      }
    }
    return null;
  }

  /**
   * get from contructor pool by key
   */
  private static Pair<wk, wv> gwp(wk k1) {
    for (wk k2 : wp.keySet()) {
      if (k1.c.equals(k2.c) && te(k2.a, k1.a)) {
        return Pair.of(k2, wp.get(k2));
      }
    }
    return null;
  }

  /**
   * type equeals
   */
  private static boolean te(Class[] c1, Class[] c2) {
    if (c1.length != c2.length) {
      return false;
    }
    for (int i = 0; i < c1.length; i++) {
      if (!c1[i].equals(c2[i]) && !fe(c1[i], c2[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * fuzzy equal
   * @param c1 fact types (can be primary type)
   * @param c2 user types
   */
  private static boolean fe(Class c1, Class c2) {
     return (c1.equals(int.class) && c2.equals(Integer.class)) ||
         (c1.equals(long.class) && c2.equals(Long.class)) ||
         (c1.equals(short.class) && c2.equals(Short.class)) ||
         (c1.equals(boolean.class) && c2.equals(Boolean.class)) ||
         (c1.equals(char.class) && c2.equals(Character.class)) ||
         (c1.equals(byte.class) && c2.equals(Byte.class)) ||
         (c1.equals(float.class) && c2.equals(Float.class)) ||
         (c1.equals(double.class) && c2.equals(Double.class));
  }

}
