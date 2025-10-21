public class CalculadoraJuros {
    
    public static double jurosSimples(double capital, double taxa, int tempo) {
        return capital * taxa * tempo;
    }
    
    public static double jurosCompostos(double capital, double taxa, int tempo) {
        return capital * Math.pow(1 + taxa, tempo);
    }
    
    public static double montanteJurosCompostos(double capital, double taxa, int tempo) {
        return capital + jurosCompostos(capital, taxa, tempo);
    }
    
    public static void main(String[] args) {
        double capital = 1000;
        double taxa = 0.05; // 5% ao mês
        int tempo = 12; // 12 meses
        
        System.out.println("Juros simples (R$1000, 5%, 12 meses): R$" + jurosSimples(capital, taxa, tempo));
        System.out.println("Juros compostos (R$1000, 5%, 12 meses): R$" + jurosCompostos(capital, taxa, tempo));
        System.out.println("Montante final: R$" + montanteJurosCompostos(capital, taxa, tempo));
    }
}