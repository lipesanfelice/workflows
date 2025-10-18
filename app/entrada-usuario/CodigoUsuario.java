import java.util.Random;
import java.util.Scanner;

public class JogoAdivinhacao {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        
        int numeroSecreto = random.nextInt(100) + 1;
        int tentativas = 0;
        int palpite;
        
        System.out.println("=== JOGO DE ADIVINHAÇÃO ===");
        System.out.println("Estou pensando em um número entre 1 e 100.");
        System.out.println("Tente adivinhar!");
        
        do {
            System.out.print("\nDigite seu palpite: ");
            palpite = scanner.nextInt();
            tentativas++;
            
            if (palpite < numeroSecreto) {
                System.out.println("Muito baixo! Tente um número maior.");
            } else if (palpite > numeroSecreto) {
                System.out.println("Muito alto! Tente um número menor.");
            } else {
                System.out.println("🎉 Parabéns! Você acertou!");
                System.out.println("O número era: " + numeroSecreto);
                System.out.println("Tentativas: " + tentativas);
            }
            
        } while (palpite != numeroSecreto);
        
        scanner.close();
    }
}