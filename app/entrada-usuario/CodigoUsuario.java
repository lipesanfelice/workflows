public class ValidadorCPF {
    
    public static boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        int[] digitos = cpf.chars().map(Character::getNumericValue).toArray();
        
        int digito1 = calcularDigito(digitos, 9);
        int digito2 = calcularDigito(digitos, 10);
        
        return digitos[9] == digito1 && digitos[10] == digito2;
    }
    
    private static int calcularDigito(int[] digitos, int posicao) {
        int soma = 0;
        for (int i = 0; i < posicao; i++) {
            soma += digitos[i] * (posicao + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
    
    public static void main(String[] args) {
        System.out.println("CPF válido: " + validarCPF("123.456.789-09"));
    }
}