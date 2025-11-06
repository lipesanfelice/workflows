import java.util.Scanner;

public class ConversorHoras {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Digite as horas (0-23): ");
        int horas = scanner.nextInt();
        
        if (horas >= 0 && horas <= 23) {
            String periodo;
            if (horas < 12) {
                periodo = "AM";
            } else {
                periodo = "PM";
            }
            
            int horas12 = horas > 12 ? horas - 12 : horas;
            if (horas12 == 0) horas12 = 12;
            
            System.out.println(horas + " horas = " + horas12 + " " + periodo);
        } else {
            System.out.println("Hora inválida!");
        }
        
        scanner.close();
    }
}