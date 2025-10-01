public class ConversorTemperatura {
    
    public static double celsiusParaFahrenheit(double celsius) {
        return (celsius * 9/5) + 32;
    }
    
    public static double fahrenheitParaCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5/9;
    }
    
    public static double celsiusParaKelvin(double celsius) {
        return celsius + 273.15;
    }
    
    public static void main(String[] args) {
        System.out.println("25°C para Fahrenheit: " + celsiusParaFahrenheit(25));
        System.out.println("77°F para Celsius: " + fahrenheitParaCelsius(77));
        System.out.println("0°C para Kelvin: " + celsiusParaKelvin(0));
    }
}