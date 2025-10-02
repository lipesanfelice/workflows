public class ConversorMoedas {
    
    public static double realParaDolar(double real) {
        return real * 0.19; // Cotação aproximada
    }
    
    public static double dolarParaReal(double dolar) {
        return dolar * 5.25; // Cotação aproximada
    }
    
    public static double realParaEuro(double real) {
        return real * 0.18; // Cotação aproximada
    }
    
    public static void main(String[] args) {
        System.out.println("100 reais em dólar: $" + realParaDolar(100));
        System.out.println("50 dólares em reais: R$" + dolarParaReal(50));
        System.out.println("200 reais em euro: €" + realParaEuro(200));
    }
}