package at.ac.univie.inventoryoptservice.optimization;

public class OptimizationConfigFactory {

    public static OptimizationConfig createDefaultOptConfig() {
        return new OptimizationConfig(0.01, 100, 100);
    }

    public static OptimizationConfig createCustomOptConfig(double mutationRate, int populationSize, int generations) {
        return new OptimizationConfig(mutationRate, populationSize, generations);
    }
}
