package generated_testable.n;

import com.sun.tools.javac.util.Pair;

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
   * add item to contructor pool
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

  /**
   * subsitituion entry for new
   */
  public static <T> T w(Class<T> ct, Object... as) {
    Class[] cs = new Class[as.length];
    for (int i = 0; i < cs.length; i++) {
      cs[i] = as[i].getClass();
    }
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
  public static <T> T f(Object obj, String mn, Object... as) {
    Class[] cs = gcs(as);
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
      Method m = gm(obj.getClass().getDeclaredMethods(), mn, cs);
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
   * get method by name and parameter matching
   */
  private static Method gm(Method[] mds, String mn, Class[] cs) {
    for (Method m : mds) {
      if (m.getName().equals(mn) && te(m.getParameterTypes(), cs)) {
        return m;
      }
    }
    return null;
  }

  /**
   * get from method pool by key
   */
  private static p gpf(String mn, Class[] cs) {
    for (p f : pf) {
      if (f.m.equals(mn) && te(f.a, cs)) {
        return f;
      }
    }
    return null;
  }

  /**
   * get contructor by parameter matching
   */
  private static Constructor gc(Constructor<?>[] cons, Class[] cs) {
    for (Constructor c : cons) {
      if (te(c.getParameterTypes(), cs)) {
        return c;
      }
    }
    return null;
  }

  /**
   * get from contructor pool by key
   */
  private static p gpw(Class ct, Class[] cs) {
    for (p w : pw) {
      if (w.c.equals(ct) && te(w.a, cs)) {
        return w;
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
