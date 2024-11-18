package at.ac.univie.inventoryoptservice.optimization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptimizationConfig {
    private double mutationRate;
    private int populationSize;
    private int generations;
}