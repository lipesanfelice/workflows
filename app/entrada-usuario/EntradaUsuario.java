import java.util.Random;

public class GeradorAleatorio {
    public static void main(String[] args) {
        Random random = new Random();
        
        // Número inteiro entre 1 e 100
        int numero = random.nextInt(100) + 1;
        System.out.println("Número aleatório: " + numero);
        
        // Boolean aleatório
        boolean verdadeiroOuFalso = random.nextBoolean();
        System.out.println("Boolean aleatório: " + verdadeiroOuFalso);
        
        // Double entre 0.0 e 1.0
        double decimal = random.nextDouble();
        System.out.println("Decimal aleatório: " + decimal);
    }
}