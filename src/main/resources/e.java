package n;

import com.sun.tools.javac.util.Pair;

import java.lang.Class;
import java.lang.Object;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public final class e {

  public static class k {
    public Class c;   // target instance type
    public Class[] a; // constructor parameter types

    public k(Class c, Class[] a) {
      this.c = c;
      this.a = a;
    }

    @Override
    public boolean equals(Object o) {
      return o.getClass().equals(e.k.class) && c.equals(((e.k)o).c) && Arrays.equals(a, ((e.k)o).a);
    }

    @Override
    public int hashCode() {
      return 31 * c.hashCode() + Arrays.hashCode(a);
    }
  }

  public static class v {
    public Object o;  // testable object
    public String m;  // method to create instance

    public v(Object o, String m) {
      this.o = o;
      this.m = m;
    }
  }

  private static Map<k, v> pool = new HashMap<>();

  public static void a(k ki, v vi) {
    pool.put(ki, vi);
  }

  public static <T> T w(Class<T> ct, Object... as) {
    Class[] cs = new Class[as.length];
    for (int i = 0; i < cs.length; i++) {
      cs[i] = as[i].getClass();
    }
    if (!pool.isEmpty()) {
      try {
        Pair<k, v> p = getFromPool(new k(ct, cs));
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
      Constructor c = getFromConstructors(ct.getConstructors(), cs);
      if (c != null) {
        return (T)c.newInstance(as);
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  private static Constructor getFromConstructors(Constructor<?>[] constructors, Class[] cs) {
    for (Constructor c : constructors) {
      if (typeEquals(c.getParameterTypes(), cs)) {
        return c;
      }
    }
    return null;
  }

  private static Pair<k, v> getFromPool(k k1) {
    for (k k2 : pool.keySet()) {
      if (k1.c.equals(k2.c) && typeEquals(k2.a, k1.a)) {
        return Pair.of(k2, pool.get(k2));
      }
    }
    return null;
  }

  /**
   * @param c1 fact types (can be primary type)
   * @param c2 user types
   */
  private static boolean typeEquals(Class[] c1, Class[] c2) {
    if (c1.length != c2.length) {
      return false;
    }
    for (int i = 0; i < c1.length; i++) {
      if (c1[i].equals(c2[i])) {
        continue;
      } else if (c1[i].equals(int.class) && c2[i].equals(Integer.class)) {
        continue;
      } else if (c1[i].equals(long.class) && c2[i].equals(Long.class)) {
        continue;
      } else if (c1[i].equals(short.class) && c2[i].equals(Short.class)) {
        continue;
      } else if (c1[i].equals(boolean.class) && c2[i].equals(Boolean.class)) {
        continue;
      } else if (c1[i].equals(char.class) && c2[i].equals(Character.class)) {
        continue;
      } else if (c1[i].equals(byte.class) && c2[i].equals(Byte.class)) {
        continue;
      } else if (c1[i].equals(float.class) && c2[i].equals(Float.class)) {
        continue;
      } else if (c1[i].equals(double.class) && c2[i].equals(Double.class)) {
        continue;
      } else {
        return false;
      }
    }
    return true;
  }

}
