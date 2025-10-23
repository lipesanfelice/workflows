import java.util.ArrayList;
import java.util.List;

public class GerenciadorTarefas {
    private List<String> tarefas = new ArrayList<>();
    
    public void adicionarTarefa(String tarefa) {
        tarefas.add(tarefa);
        System.out.println("Tarefa adicionada: " + tarefa);
    }
    
    public void removerTarefa(int indice) {
        if (indice >= 0 && indice < tarefas.size()) {
            String removida = tarefas.remove(indice);
            System.out.println("Tarefa removida: " + removida);
        }
    }
    
    public void listarTarefas() {
        System.out.println("\n--- LISTA DE TAREFAS ---");
        for (int i = 0; i < tarefas.size(); i++) {
            System.out.println((i + 1) + ". " + tarefas.get(i));
        }
    }
    
    public static void main(String[] args) {
        GerenciadorTarefas gerenciador = new GerenciadorTarefas();
        gerenciador.adicionarTarefa("Estudar Java");
        gerenciador.adicionarTarefa("Fazer exercícios");
        gerenciador.listarTarefas();
        gerenciador.removerTarefa(0);
        gerenciador.listarTarefas();
    }
}