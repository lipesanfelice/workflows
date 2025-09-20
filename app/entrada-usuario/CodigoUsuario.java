import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class DataProcessor {
    
    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private List<DataProcessingTask> tasks = new ArrayList<>();
    private Map<String, ProcessingResult> results = new ConcurrentHashMap<>();
    
    public interface DataTransformer {
        String transform(String data);
    }
    
    public class DataProcessingTask {
        private String taskId;
        private List<String> data;
        private DataTransformer transformer;
        private CompletableFuture<ProcessingResult> future;
        
        public DataProcessingTask(String taskId, List<String> data, DataTransformer transformer) {
            this.taskId = taskId;
            this.data = data;
            this.transformer = transformer;
        }
        
        public ProcessingResult process() {
            ProcessingResult result = new ProcessingResult(taskId);
            for (String item : data) {
                try {
                    String transformed = transformer.transform(item);
                    result.addProcessedItem(transformed);
                } catch (Exception e) {
                    result.addError(item, e.getMessage());
                }
            }
            return result;
        }
    }
    
    public class ProcessingResult {
        private String taskId;
        private List<String> processedItems;
        private Map<String, String> errors;
        private LocalDateTime completionTime;
        
        public ProcessingResult(String taskId) {
            this.taskId = taskId;
            this.processedItems = new ArrayList<>();
            this.errors = new HashMap<>();
        }
        
        public void addProcessedItem(String item) {
            processedItems.add(item);
        }
        
        public void addError(String item, String error) {
            errors.put(item, error);
        }
    }
    
    public CompletableFuture<ProcessingResult> submitTask(String taskId, List<String> data, DataTransformer transformer) {
        DataProcessingTask task = new DataProcessingTask(taskId, data, transformer);
        CompletableFuture<ProcessingResult> future = CompletableFuture.supplyAsync(task::process, executor);
        tasks.add(task);
        future.thenAccept(result -> {
            results.put(taskId, result);
            result.completionTime = LocalDateTime.now();
        });
        return future;
    }
}