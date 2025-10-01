public class GeradorTabuada {
    
    public static void gerarTabuada(int numero) {
        System.out.println("Tabuada do " + numero + ":");
        for (int i = 1; i <= 10; i++) {
            System.out.printf("%d x %d = %d\n", numero, i, numero * i);
        }
    }
    
    public static void gerarTodasTabuadas() {
        for (int i = 1; i <= 10; i++) {
            gerarTabuada(i);
            System.out.println();
        }
    }
    
    public static void main(String[] args) {
        gerarTabuada(7);
        System.out.println("\n--- TODAS AS TABUADAS ---");
        gerarTodasTabuadas();
    }
}