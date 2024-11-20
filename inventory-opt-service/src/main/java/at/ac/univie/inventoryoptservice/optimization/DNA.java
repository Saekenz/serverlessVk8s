package at.ac.univie.inventoryoptservice.optimization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class DNA {
    private static final Random RANDOM = new Random();
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

    public DNA crossover(DNA otherParent) {
        DNA child = new DNA();

        Set<Long> uniqueProducts = chromosomes.stream()
                .map(Chromosome::getProductId)
                .collect(Collectors.toSet());

        Set<Long> productIdsTakenFromThisParent = new HashSet<>();
        Set<Long> productIdsTakenFromOtherParent = new HashSet<>();

        for (Long productId : uniqueProducts) {
            if (RANDOM.nextBoolean()) {
                productIdsTakenFromThisParent.add(productId);
            }
            else {
                productIdsTakenFromOtherParent.add(productId);
            }
        }

        for (int i = 0; i < chromosomes.size(); i++) {
            if (productIdsTakenFromThisParent.contains(chromosomes.get(i).getProductId())) {
                child.addChromosome(chromosomes.get(i));
            }
            if (productIdsTakenFromOtherParent.contains(otherParent.getChromosomes().get(i).getProductId())) {
                child.addChromosome(otherParent.getChromosomes().get(i));
            }
        }

        System.out.println("Crossover created child with size " + child.getChromosomes().size());
        System.out.println("=================== Child DNA ==================");
        for (Chromosome c : child.getChromosomes()) {
            System.out.println(c.toString());
        }

        return child;
    }

    public void mutate(double mutationRate) {
        for (Chromosome c1 : chromosomes) {
            if (RANDOM.nextDouble() < mutationRate) {
                Chromosome c2 = findChromosomeWithSameProductId(c1);

                // if a matching chromosome was found do the swap
                if (c2 != null) {
                    int helperStock = c1.getCurrentStock();
                    c1.setCurrentStock(c2.getCurrentStock());
                    c2.setCurrentStock(helperStock);
                    log.info("Mutated chromosome {} {} with {} {}", c1.getProductId(), c1.getLocationId(),
                            c2.getProductId(), c2.getLocationId());
                }
            }
        }
    }

    private Chromosome findChromosomeWithSameProductId(Chromosome chromosome) {
        // filter the chromosomes to only include ones with the same productId
        List<Chromosome> foundChromosomes = chromosomes.stream()
                .filter(c -> Objects.equals(c.getProductId(), chromosome.getProductId()) && !c.equals(chromosome))
                .toList();

        if (foundChromosomes.isEmpty()) {
            return null;
        }

        // pick a random chromosome from the list
        return foundChromosomes.get(RANDOM.nextInt(foundChromosomes.size()));
    }
}
