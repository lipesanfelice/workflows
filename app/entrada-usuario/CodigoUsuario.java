import java.util.Scanner;

public class Calculadora {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== CALCULADORA SIMPLES ===");
        System.out.print("Digite o primeiro número: ");
        double num1 = scanner.nextDouble();
        
        System.out.print("Digite o segundo número: ");
        double num2 = scanner.nextDouble();
        
        System.out.println("\nEscolha a operação:");
        System.out.println("1 - Soma (+)");
        System.out.println("2 - Subtração (-)");
        System.out.println("3 - Multiplicação (*)");
        System.out.println("4 - Divisão (/)");
        System.out.print("Opção: ");
        
        int opcao = scanner.nextInt();
        double resultado = 0;
        
        switch (opcao) {
            case 1:
                resultado = num1 + num2;
                System.out.println(num1 + " + " + num2 + " = " + resultado);
                break;
            case 2:
                resultado = num1 - num2;
                System.out.println(num1 + " - " + num2 + " = " + resultado);
                break;
            case 3:
                resultado = num1 * num2;
                System.out.println(num1 + " * " + num2 + " = " + resultado);
                break;
            case 4:
                if (num2 != 0) {
                    resultado = num1 / num2;
                    System.out.println(num1 + " / " + num2 + " = " + resultado);
                } else {
                    System.out.println("Erro: Divisão por zero!");
                }
                break;
            default:
                System.out.println("Opção inválida!");
        }
        
        scanner.close();
    }
}