import java.security.SecureRandom;

public class GeradorSenha {
    private static final String CARACTERES = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    
    public static String gerarSenha(int tamanho) {
        SecureRandom random = new SecureRandom();
        StringBuilder senha = new StringBuilder();
        
        for (int i = 0; i < tamanho; i++) {
            int index = random.nextInt(CARACTERES.length());
            senha.append(CARACTERES.charAt(index));
        }
        
        return senha.toString();
    }
    
    public static void main(String[] args) {
        System.out.println("Senha gerada: " + gerarSenha(12));
    }
}