import java.util.ArrayList;
import java.util.Scanner;

public class GerenciadorTarefas {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> tarefas = new ArrayList<>();
        int opcao;
        
        System.out.println("=== GERENCIADOR DE TAREFAS ===");
        
        do {
            System.out.println("\nMenu:");
            System.out.println("1 - Adicionar tarefa");
            System.out.println("2 - Listar tarefas");
            System.out.println("3 - Remover tarefa");
            System.out.println("4 - Sair");
            System.out.print("Escolha uma opção: ");
            
            opcao = scanner.nextInt();
            scanner.nextLine(); // Limpar buffer
            
            switch (opcao) {
                case 1:
                    System.out.print("Digite a tarefa: ");
                    String tarefa = scanner.nextLine();
                    tarefas.add(tarefa);
                    System.out.println("Tarefa adicionada!");
                    break;
                    
                case 2:
                    if (tarefas.isEmpty()) {
                        System.out.println("Nenhuma tarefa cadastrada.");
                    } else {
                        System.out.println("\n=== LISTA DE TAREFAS ===");
                        for (int i = 0; i < tarefas.size(); i++) {
                            System.out.println((i + 1) + ". " + tarefas.get(i));
                        }
                    }
                    break;
                    
                case 3:
                    if (tarefas.isEmpty()) {
                        System.out.println("Nenhuma tarefa para remover.");
                    } else {
                        System.out.print("Digite o número da tarefa a remover: ");
                        int numero = scanner.nextInt();
                        if (numero >= 1 && numero <= tarefas.size()) {
                            tarefas.remove(numero - 1);
                            System.out.println("Tarefa removida!");
                        } else {
                            System.out.println("Número inválido!");
                        }
                    }
                    break;
                    
                case 4:
                    System.out.println("Saindo... Até logo!");
                    break;
                    
                default:
                    System.out.println("Opção inválida!");
            }
            
        } while (opcao != 4);
        
        scanner.close();
    }
}