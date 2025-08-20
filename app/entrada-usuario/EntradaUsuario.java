import java.util.Random;

public class SorteioNomes {
    public static void main(String[] args) {
        String[] nomes = {
            "Ana", "Carlos", "Maria", "João", "Pedro",
            "Laura", "Ricardo", "Fernanda", "Lucas", "Juliana"
        };
        
        Random random = new Random();
        int indiceSorteado = random.nextInt(nomes.length);
        
        System.out.println("Nome sorteado: " + nomes[indiceSorteado]);
        
        // Misturar array
        for (int i = 0; i < nomes.length; i++) {
            int randomIndex = random.nextInt(nomes.length);
            String temp = nomes[i];
            nomes[i] = nomes[randomIndex];
            nomes[randomIndex] = temp;
        }
        
        System.out.println("\nLista embaralhada:");
        for (String nome : nomes) {
            System.out.println(nome);
        }
    }
}