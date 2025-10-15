public class VerificadorIdade {
    public static void main(String[] args) {
        int idade = 20;
        
        System.out.println("Idade: " + idade + " anos");
        
        if (idade < 0) {
            System.out.println("Idade inválida!");
        } else if (idade < 12) {
            System.out.println("Criança");
        } else if (idade < 18) {
            System.out.println("Adolescente");
        } else if (idade < 60) {
            System.out.println("Adulto");
        } else {
            System.out.println("Idoso");
        }
        
        System.out.println("Pode votar: " + (idade >= 16 ? "Sim" : "Não"));
        System.out.println("Maior de idade: " + (idade >= 18 ? "Sim" : "Não"));
    }
}