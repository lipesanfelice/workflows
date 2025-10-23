import java.util.Random;

public class SimuladorDados {
    private static final Random random = new Random();
    
    public static int lancarDado(int faces) {
        return random.nextInt(faces) + 1;
    }
    
    public static int[] lancarVariosDados(int quantidade, int faces) {
        int[] resultados = new int[quantidade];
        for (int i = 0; i < quantidade; i++) {
            resultados[i] = lancarDado(faces);
        }
        return resultados;
    }
    
    public static void main(String[] args) {
        System.out.println("Dado de 6 faces: " + lancarDado(6));
        
        int[] dados = lancarVariosDados(4, 20);
        System.out.print("4 dados de 20 faces: ");
        for (int resultado : dados) {
            System.out.print(resultado + " ");
        }
    }
}