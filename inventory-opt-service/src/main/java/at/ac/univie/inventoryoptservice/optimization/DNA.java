package at.ac.univie.inventoryoptservice.optimization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
@AllArgsConstructor
public class DNA {
    private List<Chromosome> chromosomes;
    private double fitness;

    public DNA() {
        this.chromosomes = new ArrayList<>();
        this.fitness = 0.0;
    }

    public DNA(List<Chromosome> chromosomes) {
        this.chromosomes = chromosomes;
        this.fitness = 0.0;
    }

    public void addChromosome(Chromosome chromosome) {
        this.chromosomes.add(chromosome);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("DNA:\n");
        for (Chromosome chromosome : chromosomes) {
            str.append(chromosome);
            str.append("\n");
        }
        return str.toString();
    }

    public void calculateFitness(DNA originalDNA) {
        // TODO: IMPLEMENT
        // calculate fitness of this allocation based on the initial distribution
        // factor in capacity utilization & distance that products need to travel
        fitness = new Random().nextDouble(5000);
    }
}
