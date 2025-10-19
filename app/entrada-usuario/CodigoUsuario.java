import java.util.HashMap;
import java.util.Scanner;

public class CadastroAlunos {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HashMap<String, Double> alunos = new HashMap<>();
        int opcao;
        
        System.out.println("=== CADASTRO DE ALUNOS ===");
        
        do {
            System.out.println("\nMenu:");
            System.out.println("1 - Cadastrar aluno");
            System.out.println("2 - Buscar aluno");
            System.out.println("3 - Listar todos os alunos");
            System.out.println("4 - Sair");
            System.out.print("Escolha uma opção: ");
            
            opcao = scanner.nextInt();
            scanner.nextLine(); // Limpar buffer
            
            switch (opcao) {
                case 1:
                    System.out.print("Digite o nome do aluno: ");
                    String nome = scanner.nextLine();
                    System.out.print("Digite a nota do aluno: ");
                    double nota = scanner.nextDouble();
                    
                    alunos.put(nome, nota);
                    System.out.println("Aluno cadastrado com sucesso!");
                    break;
                    
                case 2:
                    System.out.print("Digite o nome do aluno a buscar: ");
                    String nomeBusca = scanner.nextLine();
                    
                    if (alunos.containsKey(nomeBusca)) {
                        double notaAluno = alunos.get(nomeBusca);
                        System.out.println("Aluno: " + nomeBusca + " - Nota: " + notaAluno);
                    } else {
                        System.out.println("Aluno não encontrado!");
                    }
                    break;
                    
                case 3:
                    if (alunos.isEmpty()) {
                        System.out.println("Nenhum aluno cadastrado.");
                    } else {
                        System.out.println("\n=== LISTA DE ALUNOS ===");
                        for (String aluno : alunos.keySet()) {
                            System.out.println("Aluno: " + aluno + " - Nota: " + alunos.get(aluno));
                        }
                    }
                    break;
                    
                case 4:
                    System.out.println("Saindo...");
                    break;
                    
                default:
                    System.out.println("Opção inválida!");
            }
            
        } while (opcao != 4);
        
        scanner.close();
    }
}