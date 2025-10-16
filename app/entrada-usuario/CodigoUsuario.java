public class CalculadoraIMC {
    
    public static double calcularIMC(double peso, double altura) {
        return peso / (altura * altura);
    }
    
    public static String classificarIMC(double imc) {
        if (imc < 18.5) return "Abaixo do peso";
        else if (imc < 25) return "Peso normal";
        else if (imc < 30) return "Sobrepeso";
        else if (imc < 35) return "Obesidade grau I";
        else if (imc < 40) return "Obesidade grau II";
        else return "Obesidade grau III";
    }
    
    public static void main(String[] args) {
        double imc1 = calcularIMC(70, 1.75);
        double imc2 = calcularIMC(90, 1.70);
        
        System.out.println("IMC 70kg/1.75m: " + imc1 + " - " + classificarIMC(imc1));
        System.out.println("IMC 90kg/1.70m: " + imc2 + " - " + classificarIMC(imc2));
    }
}