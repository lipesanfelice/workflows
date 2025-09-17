import java.util.Random;

public class GeradorAleatorio {
    public static void main(String[] args) {
        Random random = new Random();
        
        // Número inteiro entre 0 e 100
        int numeroInteiro = random.nextInt(101);
        System.out.println("Número inteiro: " + numeroInteiro);
        
        // Número decimal entre 0.0 e 1.0
        double numeroDecimal = random.nextDouble();
        System.out.println("Número decimal: " + numeroDecimal);
        
        // Boolean aleatório
        boolean valorBooleano = random.nextBoolean();
        System.out.println("Valor booleano: " + valorBooleano);
    }
}