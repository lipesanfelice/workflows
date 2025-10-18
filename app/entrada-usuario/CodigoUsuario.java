import java.util.Scanner;

public class ConversorTemperatura {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== CONVERSOR DE TEMPERATURA ===");
        System.out.println("1 - Celsius para Fahrenheit");
        System.out.println("2 - Fahrenheit para Celsius");
        System.out.println("3 - Celsius para Kelvin");
        System.out.println("4 - Kelvin para Celsius");
        System.out.print("Escolha a conversão: ");
        
        int opcao = scanner.nextInt();
        System.out.print("Digite a temperatura: ");
        double temperatura = scanner.nextDouble();
        double resultado = 0;
        
        switch (opcao) {
            case 1:
                resultado = (temperatura * 9/5) + 32;
                System.out.println(temperatura + "°C = " + resultado + "°F");
                break;
                
            case 2:
                resultado = (temperatura - 32) * 5/9;
                System.out.println(temperatura + "°F = " + resultado + "°C");
                break;
                
            case 3:
                resultado = temperatura + 273.15;
                System.out.println(temperatura + "°C = " + resultado + "K");
                break;
                
            case 4:
                resultado = temperatura - 273.15;
                System.out.println(temperatura + "K = " + resultado + "°C");
                break;
                
            default:
                System.out.println("Opção inválida!");
        }
        
        scanner.close();
    }
    
    // Métodos adicionais para reutilização
    public static double celsiusParaFahrenheit(double celsius) {
        return (celsius * 9/5) + 32;
    }
    
    public static double fahrenheitParaCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5/9;
    }
    
    public static double celsiusParaKelvin(double celsius) {
        return celsius + 273.15;
    }
    
    public static double kelvinParaCelsius(double kelvin) {
        return kelvin - 273.15;
    }
}