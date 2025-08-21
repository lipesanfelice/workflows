import java.util.Random;
import java.util.Scanner;

public class SimuladorDados {
    public static void main(String[] args) {
        Random random = new Random();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("🎲 Simulador de Dados 🎲");
        System.out.print("Quantos dados quer lançar? ");
        int quantidade = scanner.nextInt();
        
        System.out.print("Quantas faces tem cada dado? ");
        int faces = scanner.nextInt();
        
        System.out.println("\nResultados:");
        int total = 0;
        
        for (int i = 1; i <= quantidade; i++) {
            int resultado = random.nextInt(faces) + 1;
            total += resultado;
            System.out.println("Dado " + i + ": " + resultado);
        }
        
        System.out.println("Total: " + total);
        scanner.close();
    }
}