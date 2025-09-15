import java.util.Random;
import java.util.Scanner;

public class CaraCoroa {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        
        System.out.println("Cara ou Coroa? (0-Cara, 1-Coroa)");
        int escolha = scanner.nextInt();
        
        int resultado = random.nextInt(2);
        String resultadoTexto = (resultado == 0) ? "Cara" : "Coroa";
        String escolhaTexto = (escolha == 0) ? "Cara" : "Coroa";
        
        System.out.println("Resultado: " + resultadoTexto);
        System.out.println("Sua escolha: " + escolhaTexto);
        
        if (escolha == resultado) {
            System.out.println("Você ganhou!");
        } else {
            System.out.println("Você perdeu!");
        }
        
        scanner.close();
    }
}