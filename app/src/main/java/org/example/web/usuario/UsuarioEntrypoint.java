package org.example.web.usuario;

public final class UsuarioEntrypoint {
    private UsuarioEntrypoint() {}
    public static void rodarMain(String nomeClasse, String... args) {
        try {
            Class<?> c = Class.forName(nomeClasse);
            var m = c.getMethod("main", String[].class);
            m.invoke(null, (Object) args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
