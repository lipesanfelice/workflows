import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataPipelineProcessor<T, R> {
    private final List<ProcessingStage<T, ?>> stages;
    private final ExecutorService executor;

    public DataPipelineProcessor() {
        this.stages = new ArrayList<>();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public <U> DataPipelineProcessor<T, U> addStage(Function<T, U> transformer) {
        return addStage(transformer, false);
    }

    public <U> DataPipelineProcessor<T, U> addStage(Function<T, U> transformer, boolean parallel) {
        stages.add(new ProcessingStage<>(transformer, parallel));
        return (DataPipelineProcessor<T, U>) this;
    }

    public CompletableFuture<List<R>> process(List<T> input) {
        if (stages.isEmpty()) {
            return CompletableFuture.completedFuture((List<R>) input);
        }

        CompletableFuture<List<?>> current = CompletableFuture.completedFuture(input);

        for (ProcessingStage<T, ?> stage : stages) {
            current = current.thenCompose(data -> processStage(stage, (List<Object>) data));
        }

        return current.thenApply(result -> (List<R>) result);
    }

    private <I, O> CompletableFuture<List<O>> processStage(ProcessingStage<I, O> stage, List<I> input) {
        if (stage.isParallel()) {
            List<CompletableFuture<O>> futures = input.stream()
                    .map(item -> CompletableFuture.supplyAsync(() -> stage.getTransformer().apply(item), executor))
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList()));
        } else {
            return CompletableFuture.supplyAsync(() -> input.stream()
                    .map(stage.getTransformer())
                    .collect(Collectors.toList()), executor);
        }
    }

    private static class ProcessingStage<I, O> {
        private final Function<I, O> transformer;
        private final boolean parallel;

        public ProcessingStage(Function<I, O> transformer, boolean parallel) {
            this.transformer = transformer;
            this.parallel = parallel;
        }

        public Function<I, O> getTransformer() { return transformer; }
        public boolean isParallel() { return parallel; }
    }

    public void shutdown() {
        executor.shutdown();
    }
}