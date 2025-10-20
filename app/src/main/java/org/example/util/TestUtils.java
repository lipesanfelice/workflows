// app/src/test/java/org/example/util/TestUtils.java
package org.example.util;

import java.lang.reflect.*;
import java.util.*;

public final class TestUtils {
    private TestUtils() {}

    public static Class<?> load(String fqcn) {
        try { return Class.forName(fqcn); }
        catch (ClassNotFoundException e) { throw new AssertionError("Classe não encontrada: " + fqcn, e); }
    }

    public static Object newInstanceDefault(Class<?> cls) {
        try {
            Constructor<?> ctor = Arrays.stream(cls.getDeclaredConstructors())
                .sorted(Comparator.comparingInt(Constructor::getParameterCount))
                .findFirst().orElseThrow();
            ctor.setAccessible(true);
            Object[] args = new Object[ctor.getParameterCount()];
            return ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Falha ao instanciar " + cls.getName(), e);
        }
    }

    public static Object invoke(Object target, String method, Object... args) {
        Class<?> cls = (target instanceof Class<?> c) ? c : target.getClass();
        Method m = resolveMethod(cls, method, args);
        try {
            m.setAccessible(true);
            return m.invoke((target instanceof Class) ? null : target, args);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Falha ao invocar " + method + " em " + cls.getName(), e);
        }
    }

    private static Method resolveMethod(Class<?> cls, String name, Object[] args) {
        outer:
        for (Method m : cls.getMethods()) {
            if (!m.getName().equals(name)) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length != args.length) continue;
            for (int i = 0; i < p.length; i++) {
                if (args[i] != null && !p[i].isAssignableFrom(args[i].getClass())) continue outer;
            }
            return m;
        }
        throw new AssertionError("Método não encontrado: " + name + " em " + cls.getName());
    }
}
