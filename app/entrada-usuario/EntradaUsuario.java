import java.util.Random;
import java.util.Scanner;

public class JogoDados {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        
        System.out.println("Jogo de Dados - Quantos dados quer lançar?");
        int quantidade = scanner.nextInt();
        
        int total = 0;
        System.out.println("Resultados:");
        for (int i = 0; i < quantidade; i++) {
            int dado = random.nextInt(6) + 1;
            System.out.println("Dado " + (i+1) + ": " + dado);
            total += dado;
        }
        
        System.out.println("Total: " + total);
        scanner.close();
    }
}