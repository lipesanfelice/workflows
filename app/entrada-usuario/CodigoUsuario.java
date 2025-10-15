public class CalculadoraGeometrica {
    
    public static double areaCirculo(double raio) {
        return Math.PI * raio * raio;
    }
    
    public static double areaRetangulo(double base, double altura) {
        return base * altura;
    }
    
    public static double areaTriangulo(double base, double altura) {
        return (base * altura) / 2;
    }
    
    public static void main(String[] args) {
        System.out.println("Área do círculo (raio 5): " + areaCirculo(5));
        System.out.println("Área do retângulo (4x6): " + areaRetangulo(4, 6));
        System.out.println("Área do triângulo (3x4): " + areaTriangulo(3, 4));
    }
}