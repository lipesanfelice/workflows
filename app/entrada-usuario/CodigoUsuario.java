import java.util.Scanner;

public class ContadorVogais {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Digite uma palavra: ");
        String palavra = scanner.nextLine().toLowerCase();
        
        int contador = 0;
        for (int i = 0; i < palavra.length(); i++) {
            char letra = palavra.charAt(i);
            if (letra == 'a' || letra == 'e' || letra == 'i' || 
                letra == 'o' || letra == 'u') {
                contador++;
            }
        }
        
        System.out.println("A palavra '" + palavra + "' tem " + contador + " vogais");
        scanner.close();
    }
}