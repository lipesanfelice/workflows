public class ConversorUnidades {
    
    public static double metroParaCentimetro(double metro) {
        return metro * 100;
    }
    
    public static double centimetroParaMetro(double centimetro) {
        return centimetro / 100;
    }
    
    public static double kilometroParaMetro(double kilometro) {
        return kilometro * 1000;
    }
    
    public static double litroParaMililitro(double litro) {
        return litro * 1000;
    }
    
    public static void main(String[] args) {
        System.out.println("2 metros em centímetros: " + metroParaCentimetro(2));
        System.out.println("150 cm em metros: " + centimetroParaMetro(150));
        System.out.println("3.5 km em metros: " + kilometroParaMetro(3.5));
        System.out.println("2 litros em ml: " + litroParaMililitro(2));
    }
}