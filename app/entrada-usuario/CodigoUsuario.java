import java.util.Scanner;

public class JogoForca {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] palavras = {"java", "programacao", "computador", "algoritmo", "desenvolvimento"};
        String palavraSecreta = palavras[(int) (Math.random() * palavras.length)];
        char[] letrasDescobertas = new char[palavraSecreta.length()];
        int tentativas = 6;
        
        // Inicializa com underscores
        for (int i = 0; i < letrasDescobertas.length; i++) {
            letrasDescobertas[i] = '_';
        }
        
        System.out.println("=== JOGO DA FORCA ===");
        
        while (tentativas > 0) {
            System.out.println("\nPalavra: " + String.valueOf(letrasDescobertas));
            System.out.println("Tentativas restantes: " + tentativas);
            System.out.print("Digite uma letra: ");
            char letra = scanner.next().toLowerCase().charAt(0);
            
            boolean acertou = false;
            for (int i = 0; i < palavraSecreta.length(); i++) {
                if (palavraSecreta.charAt(i) == letra) {
                    letrasDescobertas[i] = letra;
                    acertou = true;
                }
            }
            
            if (!acertou) {
                tentativas--;
                System.out.println("Letra não encontrada!");
            }
            
            if (String.valueOf(letrasDescobertas).equals(palavraSecreta)) {
                System.out.println("\nParabéns! Você ganhou! A palavra era: " + palavraSecreta);
                break;
            }
        }
        
        if (tentativas == 0) {
            System.out.println("\nGame Over! A palavra era: " + palavraSecreta);
        }
        
        scanner.close();
    }
}