package n;

import java.lang.Class;
import java.lang.Object;
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
      return o.getClass().equals(k.class) && c.equals(((k)o).c) && Arrays.equals(a, ((k)o).a);
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
    Class[] cs = new Class[as.length / 2];
    Object[] ps = new Object[as.length / 2];
    for (int i = 0; i < cs.length; i++) {
      cs[i] = (Class)as[i];
    }
    System.arraycopy(as, ps.length, ps, 0, ps.length);
    if (!pool.isEmpty()) {
      try {
        v p = pool.get(new k(ct, cs));
        if (p != null) {
          Method m = p.o.getClass().getDeclaredMethod(p.m, cs);
          m.setAccessible(true);
          return (T)m.invoke(p.o, ps);
        }
      } catch (Exception e) {
        return null;
      }
    }
    try {
      return ct.getConstructor(cs).newInstance(ps);
    } catch (Exception e) {
      return null;
    }
  }
}
