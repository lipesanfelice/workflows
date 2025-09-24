import java.util.Arrays;
import java.util.Random;

public class ArrayAleatorio {
    public static void main(String[] args) {
        int[] numeros = new int[10];
        Random random = new Random();
        
        // Preenche array com valores aleatórios
        for (int i = 0; i < numeros.length; i++) {
            numeros[i] = random.nextInt(1000);
        }
        
        System.out.println("Array original: " + Arrays.toString(numeros));
        
        // Ordena o array
        Arrays.sort(numeros);
        System.out.println("Array ordenado: " + Arrays.toString(numeros));
        
        // Encontra maior e menor valor
        int maior = numeros[numeros.length - 1];
        int menor = numeros[0];
        System.out.println("Maior: " + maior + ", Menor: " + menor);
    }
}