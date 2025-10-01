public class ProcessadorTexto {
    
    public static int contarPalavras(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0;
        }
        return texto.trim().split("\\s+").length;
    }
    
    public static String inverterString(String texto) {
        return new StringBuilder(texto).reverse().toString();
    }
    
    public static boolean ehPalindromo(String texto) {
        String limpo = texto.replaceAll("[^a-zA-Z]", "").toLowerCase();
        return limpo.equals(inverterString(limpo));
    }
    
    public static void main(String[] args) {
        String texto = "Java é uma linguagem de programação";
        System.out.println("Palavras: " + contarPalavras(texto));
        System.out.println("Invertido: " + inverterString("Java"));
        System.out.println("É palíndromo? " + ehPalindromo("radar"));
    }
}