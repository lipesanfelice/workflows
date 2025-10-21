import java.util.Scanner;

public class VerificadorParImpar {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Digite um número: ");
        int numero = scanner.nextInt();
        
        if (numero % 2 == 0) {
            System.out.println(numero + " é PAR");
        } else {
            System.out.println(numero + " é ÍMPAR");
        }
        
        scanner.close();
    }
}