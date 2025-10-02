public class GeradorSequencia {
    public static void main(String[] args) {
        System.out.println("Números pares de 0 a 20:");
        for (int i = 0; i <= 20; i += 2) {
            System.out.print(i + " ");
        }
        
        System.out.println("\n\nNúmeros ímpares de 1 a 19:");
        for (int i = 1; i <= 19; i += 2) {
            System.out.print(i + " ");
        }
        
        System.out.println("\n\nSequência Fibonacci (10 primeiros):");
        int a = 0, b = 1;
        for (int i = 0; i < 10; i++) {
            System.out.print(a + " ");
            int proximo = a + b;
            a = b;
            b = proximo;
        }
    }
}