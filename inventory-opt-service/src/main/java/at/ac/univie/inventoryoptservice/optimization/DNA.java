package at.ac.univie.inventoryoptservice.optimization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DNA {
    private List<Chromosome> chromosomes;

    public DNA() {
        this.chromosomes = new ArrayList<>();
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
}
