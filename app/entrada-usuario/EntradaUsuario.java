import java.util.Random;

public class GeradorCor {
    public static String gerarCorRGB() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("rgb(%d, %d, %d)", r, g, b);
    }
    
    public static void main(String[] args) {
        System.out.println("Cor gerada: " + gerarCorRGB());
        System.out.println("Cor gerada: " + gerarCorRGB());
        System.out.println("Cor gerada: " + gerarCorRGB());
    }
}