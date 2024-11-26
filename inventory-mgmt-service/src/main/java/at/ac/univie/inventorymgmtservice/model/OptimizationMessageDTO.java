package at.ac.univie.inventorymgmtservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptimizationMessageDTO {
    private double mutationRate;
    private int populationSize;
    private int generations;

    /**
     * Default constructor sets {@code mutationRate} to 1%, {@code populationSize} to 50
     * and {@code generations} to 200.
     *
     */
    public OptimizationMessageDTO() {
        this.mutationRate = 0.01;
        this.populationSize = 50;
        this.generations = 200;
    }
}
