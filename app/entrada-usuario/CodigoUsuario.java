import java.util.Arrays;

public class CalculadoraEstatistica {
    
    public static double calcularMedia(double[] numeros) {
        return Arrays.stream(numeros).average().orElse(0.0);
    }
    
    public static double calcularMediana(double[] numeros) {
        Arrays.sort(numeros);
        int meio = numeros.length / 2;
        return numeros.length % 2 == 0 ? 
            (numeros[meio - 1] + numeros[meio]) / 2.0 : 
            numeros[meio];
    }
    
    public static void main(String[] args) {
        double[] dados = {10.5, 20.3, 15.7, 8.9, 25.1};
        System.out.println("Média: " + calcularMedia(dados));
        System.out.println("Mediana: " + calcularMediana(dados));
    }
}