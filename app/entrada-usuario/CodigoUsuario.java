import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GraphSearch<T> {
    private final Map<T, List<Edge<T>>> graph;
    
    public GraphSearch() {
        this.graph = new HashMap<>();
    }
    
    public void addVertex(T vertex) {
        graph.putIfAbsent(vertex, new ArrayList<>());
    }
    
    public void addEdge(T from, T to, double weight) {
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge<>(to, weight));
        graph.computeIfAbsent(to, k -> new ArrayList<>());
    }
    
    public static class Edge<T> {
        final T target;
        final double weight;
        
        public Edge(T target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }
    
    public static class PathResult<T> {
        public final List<T> path;
        public final double totalCost;
        public final int nodesExplored;
        
        public PathResult(List<T> path, double totalCost, int nodesExplored) {
            this.path = path;
            this.totalCost = totalCost;
            this.nodesExplored = nodesExplored;
        }
    }
    
    // DFS com limite de profundidade
    public PathResult<T> depthLimitedSearch(T start, T goal, int depthLimit) {
        Set<T> visited = new HashSet<>();
        Map<T, T> parent = new HashMap<>();
        int nodesExplored = 0;
        
        boolean found = dls(start, goal, visited, parent, 0, depthLimit, nodesExplored);
        
        if (found) {
            List<T> path = reconstructPath(parent, goal);
            return new PathResult<>(path, calculatePathCost(path), nodesExplored);
        }
        
        return new PathResult<>(Collections.emptyList(), Double.POSITIVE_INFINITY, nodesExplored);
    }
    
    private boolean dls(T current, T goal, Set<T> visited, Map<T, T> parent, 
                       int depth, int limit, int nodesExplored) {
        nodesExplored++;
        visited.add(current);
        
        if (current.equals(goal)) {
            return true;
        }
        
        if (depth >= limit) {
            return false;
        }
        
        for (Edge<T> edge : graph.getOrDefault(current, Collections.emptyList())) {
            if (!visited.contains(edge.target)) {
                parent.put(edge.target, current);
                if (dls(edge.target, goal, visited, parent, depth + 1, limit, nodesExplored)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Busca A*
    public PathResult<T> aStarSearch(T start, T goal, Function<T, Double> heuristic) {
        PriorityQueue<Node<T>> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<T, Double> gScore = new HashMap<>();
        Map<T, T> cameFrom = new HashMap<>();
        Set<T> closedSet = new HashSet<>();
        int nodesExplored = 0;
        
        gScore.put(start, 0.0);
        openSet.add(new Node<>(start, 0, heuristic.apply(start)));
        
        while (!openSet.isEmpty()) {
            Node<T> current = openSet.poll();
            nodesExplored++;
            
            if (current.vertex.equals(goal)) {
                List<T> path = reconstructPath(cameFrom, goal);
                return new PathResult<>(path, gScore.get(goal), nodesExplored);
            }
            
            closedSet.add(current.vertex);
            
            for (Edge<T> edge : graph.getOrDefault(current.vertex, Collections.emptyList())) {
                if (closedSet.contains(edge.target)) {
                    continue;
                }
                
                double tentativeGScore = gScore.get(current.vertex) + edge.weight;
                
                if (tentativeGScore < gScore.getOrDefault(edge.target, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(edge.target, current.vertex);
                    gScore.put(edge.target, tentativeGScore);
                    double fScore = tentativeGScore + heuristic.apply(edge.target);
                    
                    // Remove old version if exists
                    openSet.removeIf(n -> n.vertex.equals(edge.target));
                    openSet.add(new Node<>(edge.target, tentativeGScore, fScore));
                }
            }
        }
        
        return new PathResult<>(Collections.emptyList(), Double.POSITIVE_INFINITY, nodesExplored);
    }
    
    // Algoritmo genético para encontrar caminho aproximado
    public PathResult<T> geneticAlgorithmSearch(T start, T goal, int populationSize, 
                                              int generations, double mutationRate) {
        List<Chromosome<T>> population = initializePopulation(start, goal, populationSize);
        Chromosome<T> bestSolution = null;
        
        for (int generation = 0; generation < generations; generation++) {
            // Avaliar fitness
            for (Chromosome<T> chromosome : population) {
                chromosome.calculateFitness(this);
            }
            
            // Ordenar por fitness (menor custo é melhor)
            population.sort(Comparator.comparingDouble(c -> c.fitness));
            
            // Manter o melhor
            if (bestSolution == null || population.get(0).fitness < bestSolution.fitness) {
                bestSolution = new Chromosome<>(population.get(0));
            }
            
            // Nova geração
            List<Chromosome<T>> newPopulation = new ArrayList<>();
            
            // Elitismo - manter os melhores
            int eliteCount = populationSize / 10;
            for (int i = 0; i < eliteCount; i++) {
                newPopulation.add(new Chromosome<>(population.get(i)));
            }
            
            // Cruzamento e mutação
            while (newPopulation.size() < populationSize) {
                Chromosome<T> parent1 = selectParent(population);
                Chromosome<T> parent2 = selectParent(population);
                Chromosome<T> child = crossover(parent1, parent2);
                mutate(child, mutationRate);
                newPopulation.add(child);
            }
            
            population = newPopulation;
        }
        
        if (bestSolution != null && bestSolution.isValidPath() && 
            bestSolution.path.get(bestSolution.path.size() - 1).equals(goal)) {
            return new PathResult<>(bestSolution.path, bestSolution.fitness, generations * populationSize);
        }
        
        return new PathResult<>(Collections.emptyList(), Double.POSITIVE_INFINITY, generations * populationSize);
    }
    
    private static class Node<T> {
        final T vertex;
        final double gCost;
        final double fCost;
        
        Node(T vertex, double gCost, double fCost) {
            this.vertex = vertex;
            this.gCost = gCost;
            this.fCost = fCost;
        }
    }
    
    private static class Chromosome<T> {
        List<T> path;
        double fitness;
        
        Chromosome(List<T> path) {
            this.path = new ArrayList<>(path);
            this.fitness = Double.POSITIVE_INFINITY;
        }
        
        Chromosome(Chromosome<T> other) {
            this.path = new ArrayList<>(other.path);
            this.fitness = other.fitness;
        }
        
        void calculateFitness(GraphSearch<T> graph) {
            if (!isValidPath()) {
                this.fitness = Double.POSITIVE_INFINITY;
                return;
            }
            
            double cost = 0;
            for (int i = 0; i < path.size() - 1; i++) {
                T current = path.get(i);
                T next = path.get(i + 1);
                boolean found = false;
                
                for (Edge<T> edge : graph.graph.get(current)) {
                    if (edge.target.equals(next)) {
                        cost += edge.weight;
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    this.fitness = Double.POSITIVE_INFINITY;
                    return;
                }
            }
            
            this.fitness = cost;
        }
        
        boolean isValidPath() {
            return path != null && !path.isEmpty() && new HashSet<>(path).size() == path.size();
        }
    }
    
    private List<Chromosome<T>> initializePopulation(T start, T goal, int populationSize) {
        List<Chromosome<T>> population = new ArrayList<>();
        List<T> vertices = new ArrayList<>(graph.keySet());
        
        for (int i = 0; i < populationSize; i++) {
            List<T> path = new ArrayList<>();
            path.add(start);
            
            // Gerar caminho aleatório
            Set<T> visited = new HashSet<>();
            visited.add(start);
            
            T current = start;
            Random random = new Random();
            
            while (!current.equals(goal) && path.size() < vertices.size()) {
                List<Edge<T>> edges = graph.get(current);
                if (edges.isEmpty()) break;
                
                List<T> possibleNext = edges.stream()
                    .map(e -> e.target)
                    .filter(v -> !visited.contains(v))
                    .collect(Collectors.toList());
                
                if (possibleNext.isEmpty()) break;
                
                T next = possibleNext.get(random.nextInt(possibleNext.size()));
                path.add(next);
                visited.add(next);
                current = next;
            }
            
            population.add(new Chromosome<>(path));
        }
        
        return population;
    }
    
    private Chromosome<T> selectParent(List<Chromosome<T>> population) {
        // Seleção por torneio
        Random random = new Random();
        Chromosome<T> best = population.get(random.nextInt(population.size()));
        
        for (int i = 0; i < 3; i++) {
            Chromosome<T> contender = population.get(random.nextInt(population.size()));
            if (contender.fitness < best.fitness) {
                best = contender;
            }
        }
        
        return best;
    }
    
    private Chromosome<T> crossover(Chromosome<T> parent1, Chromosome<T> parent2) {
        List<T> childPath = new ArrayList<>();
        Random random = new Random();
        int crossoverPoint = random.nextInt(Math.min(parent1.path.size(), parent2.path.size()));
        
        // Primeira parte do parent1
        for (int i = 0; i < crossoverPoint; i++) {
            childPath.add(parent1.path.get(i));
        }
        
        // Segunda parte do parent2, evitando duplicatas
        for (int i = crossoverPoint; i < parent2.path.size(); i++) {
            if (!childPath.contains(parent2.path.get(i))) {
                childPath.add(parent2.path.get(i));
            }
        }
        
        return new Chromosome<>(childPath);
    }
    
    private void mutate(Chromosome<T> chromosome, double mutationRate) {
        Random random = new Random();
        
        if (random.nextDouble() < mutationRate && chromosome.path.size() >= 3) {
            int pos1 = random.nextInt(chromosome.path.size() - 2) + 1;
            int pos2 = random.nextInt(chromosome.path.size() - 2) + 1;
            
            Collections.swap(chromosome.path, pos1, pos2);
        }
    }
    
    private List<T> reconstructPath(Map<T, T> parent, T goal) {
        List<T> path = new ArrayList<>();
        T current = goal;
        
        while (current != null) {
            path.add(0, current);
            current = parent.get(current);
        }
        
        return path;
    }
    
    private double calculatePathCost(List<T> path) {
        double cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            T from = path.get(i);
            T to = path.get(i + 1);
            
            for (Edge<T> edge : graph.get(from)) {
                if (edge.target.equals(to)) {
                    cost += edge.weight;
                    break;
                }
            }
        }
        return cost;
    }
}