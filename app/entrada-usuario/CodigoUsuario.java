import java.util.Scanner;

public class AventuraTextual {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== A AVENTURA DO GUERREIRO ===");
        System.out.println("Você é um guerreiro em uma missão para resgatar a princesa.");
        System.out.println("Você está na entrada de uma caverna escura...");
        
        int vida = 100;
        boolean temEspada = false;
        boolean derrotouDragao = false;
        
        while (vida > 0 && !derrotouDragao) {
            System.out.println("\n=== SUA VIDA: " + vida + " ===");
            System.out.println("O que você faz?");
            System.out.println("1 - Entrar na caverna");
            System.out.println("2 - Procurar por armas");
            System.out.println("3 - Descansar");
            System.out.println("4 - Fugir");
            
            int escolha = scanner.nextInt();
            
            switch (escolha) {
                case 1:
                    if (temEspada) {
                        System.out.println("Você entra na caverna e encontra o dragão!");
                        System.out.println("Com sua espada, você luta bravamente...");
                        System.out.println("🎉 Você derrotou o dragão e resgatou a princesa!");
                        derrotouDragao = true;
                    } else {
                        System.out.println("Você entra sem armas e o dragão te ataca!");
                        vida -= 50;
                        System.out.println("Você perdeu 50 de vida! Fuja e encontre uma arma!");
                    }
                    break;
                    
                case 2:
                    if (!temEspada) {
                        System.out.println("Você encontra uma espada lendária!");
                        temEspada = true;
                    } else {
                        System.out.println("Você já tem uma espada!");
                    }
                    break;
                    
                case 3:
                    vida = Math.min(100, vida + 30);
                    System.out.println("Você descansa e recupera 30 de vida.");
                    break;
                    
                case 4:
                    System.out.println("💀 Você fugiu como um covarde! Fim do jogo.");
                    vida = 0;
                    break;
                    
                default:
                    System.out.println("Opção inválida!");
            }
        }
        
        if (derrotouDragao) {
            System.out.println("\n🎉 VOCÊ VENCEU! O reino está salvo!");
        } else if (vida <= 0) {
            System.out.println("\n💀 GAME OVER! Você morreu...");
        }
        
        scanner.close();
    }
}