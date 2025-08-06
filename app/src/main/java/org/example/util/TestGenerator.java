// package org.example.util;

// import java.io.File;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Scanner;

// public class TestGenerator {

//     public static void main(String[] args) {
//         Scanner scanner = new Scanner(System.in);
        
//         System.out.println("Bem-vindo ao Gerador de Testes Automatizados com IA");
//         System.out.println("Escolha o tipo de entrada:");
//         System.out.println("1 - Trecho de código Java");
//         System.out.println("2 - Arquivo Java");
//         System.out.println("3 - Projeto completo");
//         System.out.print("Opção: ");
        
//         int option = scanner.nextInt();
//         scanner.nextLine(); // Consume newline
        
//         try {
//             switch (option) {
//                 case 1:
//                     processCodeSnippet(scanner);
//                     break;
//                 case 2:
//                     processJavaFile(scanner);
//                     break;
//                 case 3:
//                     processProject(scanner);
//                     break;
//                 default:
//                     System.out.println("Opção inválida.");
//             }
//         } catch (Exception e) {
//             System.err.println("Erro ao processar entrada: " + e.getMessage());
//         } finally {
//             scanner.close();
//         }
//     }
    
//     // Métodos de processamento serão implementados abaixo
//     private static void processCodeSnippet(Scanner scanner) {
//         System.out.println("Cole seu trecho de código Java abaixo (digite 'END' em uma nova linha para finalizar):");
        
//         StringBuilder codeBuilder = new StringBuilder();
//         String line;
//         while (!(line = scanner.nextLine()).equals("END")) {
//             codeBuilder.append(line).append("\n");
//         }
        
//         String code = codeBuilder.toString();
//         System.out.println("\nTrecho de código recebido:");
//         System.out.println(code);
        
//         // Aqui você chamaria sua IA para gerar os testes
//         //generateTestsForCode(code);
//     }

//     private static void processJavaFile(Scanner scanner) {
//         System.out.print("Digite o caminho completo para o arquivo Java: ");
//         String filePath = scanner.nextLine().trim();
        
//         File javaFile = new File(filePath);
//         if (!javaFile.exists() || !javaFile.isFile() || !filePath.endsWith(".java")) {
//             System.err.println("Arquivo Java inválido ou não encontrado.");
//             return;
//         }
        
//         try {
//             String code = Files.readString(Path.of(filePath));
//             System.out.println("\nConteúdo do arquivo lido:");
//             System.out.println(code);
            
//             // Gerar testes para o arquivo
//             //generateTestsForFile(javaFile, code);
//         } catch (Exception e) {
//             System.err.println("Erro ao ler o arquivo: " + e.getMessage());
//         }
//     }

//     private static void processProject(Scanner scanner) {
//         System.out.print("Digite o caminho para a raiz do projeto: ");
//         String projectPath = scanner.nextLine().trim();
        
//         File projectDir = new File(projectPath);
//         if (!projectDir.exists() || !projectDir.isDirectory()) {
//             System.err.println("Diretório do projeto não encontrado.");
//             return;
//         }
        
//         List<File> javaFiles = findJavaFiles(projectDir);
//         if (javaFiles.isEmpty()) {
//             System.err.println("Nenhum arquivo Java encontrado no projeto.");
//             return;
//         }
        
//         System.out.println("Arquivos Java encontrados:");
//         javaFiles.forEach(f -> System.out.println("- " + f.getPath()));
        
//         // Processar cada arquivo Java encontrado
//         for (File javaFile : javaFiles) {
//             try {
//                 String code = Files.readString(javaFile.toPath());
//                 //generateTestsForFile(javaFile, code);
//             } catch (Exception e) {
//                 System.err.println("Erro ao processar arquivo " + javaFile.getName() + ": " + e.getMessage());
//             }
//         }
        
//         // Após processar todos os arquivos, gerar relatórios consolidados
//         //generateConsolidatedReports(projectDir);
//     }

//     private static List<File> findJavaFiles(File directory) {
//         List<File> javaFiles = new ArrayList<>();
//         findJavaFilesRecursive(directory, javaFiles);
//         return javaFiles;
//     }

//     private static void findJavaFilesRecursive(File directory, List<File> javaFiles) {
//         File[] files = directory.listFiles();
//         if (files != null) {
//             for (File file : files) {
//                 if (file.isDirectory()) {
//                     findJavaFilesRecursive(file, javaFiles);
//                 } else if (file.getName().endsWith(".java")) {
//                     javaFiles.add(file);
//                 }
//             }
//         }
//     }
// }

package org.example.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestGenerator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  Gerador de Testes Automatizados com IA ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("\nEscolha o tipo de entrada:");
        System.out.println("1 - Trecho de código Java");
        System.out.println("2 - Arquivo Java");
        System.out.println("3 - Projeto completo (testar extratores)");
        System.out.print("➤ Opção: ");
        
        int option = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        try {
            switch (option) {
                case 1:
                    processCodeSnippet(scanner);
                    break;
                case 2:
                    processJavaFile(scanner);
                    break;
                case 3:
                    processProject(scanner);
                    break;
                default:
                    System.out.println("❌ Opção inválida.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar entrada: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    private static void processCodeSnippet(Scanner scanner) {
        System.out.println("\n═════════ INSIRA SEU CÓDIGO ═════════");
        System.out.println("Cole seu trecho de código Java abaixo (digite 'END' em uma nova linha para finalizar):");
        
        StringBuilder codeBuilder = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            codeBuilder.append(line).append("\n");
        }
        
        String code = codeBuilder.toString();
        System.out.println("\n✅ Trecho de código recebido:");
        System.out.println(code);
        
        // TODO: Chamar IA para gerar testes
        System.out.println("\n⚠ Funcionalidade de geração de testes ainda não implementada");
    }

    private static void processJavaFile(Scanner scanner) {
        System.out.println("\n═════════ ARQUIVO JAVA ═════════");
        System.out.print("Digite o caminho completo para o arquivo Java: ");
        String filePath = scanner.nextLine().trim();
        
        File javaFile = new File(filePath);
        if (!javaFile.exists() || !javaFile.isFile() || !filePath.endsWith(".java")) {
            System.err.println("❌ Arquivo Java inválido ou não encontrado.");
            return;
        }
        
        try {
            String code = Files.readString(Path.of(filePath));
            System.out.println("\n✅ Conteúdo do arquivo lido:");
            System.out.println(code);
            
            // TODO: Gerar testes para o arquivo
            System.out.println("\n⚠ Funcionalidade de geração de testes ainda não implementada");
        } catch (Exception e) {
            System.err.println("❌ Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    private static void processProject(Scanner scanner) {
        System.out.println("\n═════════ TESTAR EXTRATORES ═════════");
        System.out.print("Digite o caminho para a raiz do projeto: ");
        String projectPath = scanner.nextLine().trim();
        
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            System.err.println("❌ Diretório do projeto não encontrado.");
            return;
        }
        
        System.out.println("\nSelecione qual extrator deseja testar:");
        System.out.println("1 - JaCoCo (relatório XML local)");
        System.out.println("2 - SonarCloud (requer token)");
        System.out.println("3 - Codecov (requer token)");
        System.out.println("4 - Testar todos os extratores");
        System.out.print("➤ Opção: ");
        int extratorOption = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        try {
            switch (extratorOption) {
                case 1:
                    testarExtratorJacoco(scanner);
                    break;
                case 2:
                    testarExtratorSonar(scanner);
                    break;
                case 3:
                    testarExtratorCodecov(scanner);
                    break;
                case 4:
                    testarTodosExtratores(scanner);
                    break;
                default:
                    System.out.println("❌ Opção inválida.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao testar extrator: " + e.getMessage());
        }
    }

    private static void testarExtratorJacoco(Scanner scanner) {
        System.out.println("\n═════════ TESTE DO JACOCO ═════════");
        System.out.print("Digite o caminho para o relatório JaCoCo (jacoco.xml): ");
        String jacocoPath = scanner.nextLine().trim();
        
        System.out.println("\n🔍 Processando relatório JaCoCo...");
        List<DadoCobertura> dados = ExtratorJacoco.extrair(jacocoPath);
        
        System.out.println("\n📊 RESULTADOS JACOCO:");
        if (dados.isEmpty()) {
            System.out.println("⚠ Nenhum dado de cobertura encontrado.");
        } else {
            System.out.println("✅ " + dados.size() + " itens de cobertura encontrados:");
            dados.forEach(dado -> System.out.println("  • " + dado));
        }
    }

    private static void testarExtratorSonar(Scanner scanner) {
        System.out.println("\n═════════ TESTE DO SONARCLOUD ═════════");
        System.out.print("Digite o nome do projeto no SonarCloud: ");
        String sonarProject = scanner.nextLine().trim();
        System.out.print("Digite o token do SonarCloud: ");
        String sonarToken = scanner.nextLine().trim();
        
        System.out.println("\n🔍 Conectando ao SonarCloud...");
        List<DadoCobertura> dados = ExtratorSonar.extrair(sonarProject, sonarToken);
        
        System.out.println("\n📊 RESULTADOS SONARCLOUD:");
        if (dados.isEmpty()) {
            System.out.println("⚠ Nenhum dado de cobertura encontrado.");
        } else {
            dados.forEach(dado -> System.out.println("  • " + dado));
        }
    }

    private static void testarExtratorCodecov(Scanner scanner) {
        System.out.println("\n═════════ TESTE DO CODECOV ═════════");
        System.out.print("Digite o owner do repositório no Codecov: ");
        String codecovOwner = scanner.nextLine().trim();
        System.out.print("Digite o nome do repositório no Codecov: ");
        String codecovRepo = scanner.nextLine().trim();
        System.out.print("Digite o token do Codecov: ");
        String codecovToken = scanner.nextLine().trim();
        
        System.out.println("\n🔍 Conectando ao Codecov...");
        List<DadoCobertura> dados = ExtratorCodecov.extrair(codecovOwner, codecovRepo, codecovToken);
        
        System.out.println("\n📊 RESULTADOS CODECOV:");
        if (dados.isEmpty()) {
            System.out.println("⚠ Nenhum dado de cobertura encontrado.");
        } else {
            dados.forEach(dado -> System.out.println("  • " + dado));
        }
    }

    private static void testarTodosExtratores(Scanner scanner) {
        System.out.println("\n═════════ TESTE DE TODOS OS EXTRATORES ═════════");
        
        testarExtratorJacoco(scanner);
        testarExtratorSonar(scanner);
        testarExtratorCodecov(scanner);
        
        System.out.println("\n✅ Todos os extratores foram testados!");
    }

    private static List<File> findJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        findJavaFilesRecursive(directory, javaFiles);
        return javaFiles;
    }

    private static void findJavaFilesRecursive(File directory, List<File> javaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFilesRecursive(file, javaFiles);
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }
}