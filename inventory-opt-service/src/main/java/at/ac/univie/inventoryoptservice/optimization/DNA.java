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

    /**
     * Mixes chromosomes of this {@link DNA} object with chromosomes of a given {@link DNA}
     * object to create a new {@link DNA} object.
     *
     * @param otherParent The {@link DNA} object which will be used to perform crossover.
     * @return A new {@link DNA} object containing chromosomes from both parent objects.
     */
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

    public void calculateFitness(DNA originalDNA, Map<LocationPair, Double> distanceMatrix) {
        // TODO: IMPLEMENT
        // calculate fitness of this allocation based on the initial distribution
        // factor in capacity utilization & distance that products need to travel
        fitness = calculateDemandCoverage();
        log.info("Fitness: {}", fitness);
        calculateDistanceMoved(originalDNA, distanceMatrix);
    }

    private double calculateDemandCoverage() {
        double demandCoverageSum = 0.0;

        for (Chromosome chromosome : chromosomes) {
            if (chromosome.getCurrentStock() < chromosome.getTargetStock()) {
                demandCoverageSum += (double) chromosome.getCurrentStock() / chromosome.getTargetStock();
            }
            else {
                demandCoverageSum += (double) chromosome.getTargetStock() / chromosome.getCurrentStock();
            }
        }

        return demandCoverageSum / chromosomes.size();
    }

    private double calculateDistanceMoved(DNA originalDNA, Map<LocationPair, Double> distanceMatrix) {
        // TODO: implement
        // 1  Calculate distances for each unique Location pair
        // 2  Compare product stock for each chromosome in initial DNA and current this DNA
        // 2a For each chromosome find if stock was added or removed
        // 3  Calculate distance stock for each product had to be moved from initial DNA to current DNA
        double totalDistance = 0.0;
        Map<Long, List<StockChange>> surplusMap = new HashMap<>();
        Map<Long, List<StockChange>> deficitMap = new HashMap<>();

        log.warn("Step 1");
        // get original and current chromosomes
        ArrayList<Chromosome> originalChromosomes = new ArrayList<>(originalDNA.getChromosomes());
        ArrayList<Chromosome> currentChromosomes = new ArrayList<>(this.chromosomes);

        // sort original and current DNA by their unique identifier
        originalChromosomes.sort(Comparator.comparing(Chromosome::getId));
        currentChromosomes.sort(Comparator.comparing(Chromosome::getId));

        log.warn("Step 2");
        // calculate stock increases and decreases for each product
        for (int i = 0; i < originalChromosomes.size(); i++) {
            Chromosome originalChromosome = originalChromosomes.get(i);
            Chromosome currentChromosome = currentChromosomes.get(i);

            Long productId = originalChromosome.getProductId();
            Long locationId = originalChromosome.getLocationId();
            int stockChange = currentChromosome.getCurrentStock() - originalChromosome.getCurrentStock();

            // create maps containing locations where products were added/removed
            if (stockChange > 0) {
                surplusMap.computeIfAbsent(productId, k -> new ArrayList<>())
                        .add(new StockChange(locationId, stockChange));
            } else if (stockChange < 0) {
                deficitMap.computeIfAbsent(productId, k -> new ArrayList<>())
                        .add(new StockChange(locationId, -stockChange));
            }
        }

        log.warn("Step 3");
        for (Long productId : surplusMap.keySet()) {
            List<StockChange> stockSurpluses = surplusMap.get(productId);
            List<StockChange> stockDeficits = deficitMap.get(productId);

            if (stockSurpluses == null || stockDeficits == null) {
                continue;
            }

            if (stockSurpluses.isEmpty()) continue;
            for (StockChange stockSurplus : stockSurpluses) {
                if (stockDeficits.isEmpty()) continue; // if a location had no transfers this list is empty
                for (StockChange stockDeficit : stockDeficits) {
                    if (stockSurplus.quantity == 0 || stockDeficit.quantity == 0) {
                        continue;
                    }

                    int transferQty = Math.min(stockSurplus.quantity, stockDeficit.quantity);
//                    double transferDistance = distanceMatrix.get(new LocationPair(stockSurplus.getLocationId(),
//                            stockDeficit.getLocationId())); TODO: FIX EXCEPTION
                    double transferDistance = 50.0;
                    log.info("{} of product with ID {} was transferred from Location {} to Location {} " +
                            "over a distance of {} km!", transferQty, productId, stockDeficit.getLocationId(),
                            stockSurplus.getLocationId(), transferDistance);

                    totalDistance += transferDistance;

                    stockSurplus.quantity -= transferQty;
                    stockDeficit.quantity -= transferQty;
                }
            }
        }
        log.info("Total distance travelled to move products: {}", totalDistance);
        return totalDistance;
    }
}
