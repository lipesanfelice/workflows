import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SorteioNomes {
    public static void main(String[] args) {
        List<String> nomes = new ArrayList<>();
        nomes.add("Ana");
        nomes.add("Carlos");
        nomes.add("Maria");
        nomes.add("João");
        nomes.add("Pedro");
        nomes.add("Julia");
        
        Random random = new Random();
        String nomeSorteado = nomes.get(random.nextInt(nomes.size()));
        
        System.out.println("Nome sorteado: " + nomeSorteado);
    }
}