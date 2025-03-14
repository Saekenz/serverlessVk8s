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

        log.debug("Created child with crossover!");

        return child;
    }

    /**
     * Mutates this {@link DNA} object by randomly swapping the stock levels of contained {@link Chromosome}
     * based on the {@code mutationRate}.
     *
     * @param mutationRate The probability that is used on each {@link Chromosome} to determine if mutation is to be
     *                     applied.
     */
    public void mutate(double mutationRate) {
        for (Chromosome c1 : chromosomes) {
            if (RANDOM.nextDouble() < mutationRate) {
                Chromosome c2 = findChromosomeWithSameProductId(c1);

                // if a matching chromosome was found do the swap
                if (c2 != null) {
                    int helperStock = c1.getCurrentStock();
                    c1.setCurrentStock(c2.getCurrentStock());
                    c2.setCurrentStock(helperStock);
                    log.debug("Mutated chromosome {} {} with {} {}", c1.getProductId(), c1.getLocationId(),
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

    public void calculateFitness(DNA originalDNA, Map<LocationPair, Double> distanceMatrix,
                                 double maxPossibleTransferDistance) {

        // calculate how well demand is covered for each product/location pair
        double fitnessDemand = calculateDemandCoverage();

        // calculate how well distance for product transfers between locations is minimized
        double transferDistance = calculateDistanceMoved(originalDNA, distanceMatrix);
        double fitnessDistance = calculateDistanceFitness(transferDistance, maxPossibleTransferDistance);

        // use weighted average formula to combine both fitness values
        fitness = (fitnessDemand * 0.7) + (fitnessDistance * 0.3);

        log.debug("Fitness: {}", fitness);
    }

    public double calculateDemandCoverage() {
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

    /**
     * Calculates the total distance required to move products from the original allocation to the current allocation.
     *
     * @param originalDNA The original allocation of products as found in the database.
     * @param distanceMatrix The distances between each pair of locations between which products can be moved.
     * @return A double value that represents the sum of the distances that must be traveled to move the products
     * to reach this current allocation.
     */
    private double calculateDistanceMoved(DNA originalDNA, Map<LocationPair, Double> distanceMatrix) {
        double totalDistance = 0.0;
        int productTransferCount = 0;
        Set<LocationPair> visitedTransfers = new HashSet<>();

        // create collections to keep track of stock changes for each product at each location
        Map<Long, List<StockChange>> surplusMap = new HashMap<>();
        Map<Long, List<StockChange>> deficitMap = new HashMap<>();

        // get original and current chromosomes
        ArrayList<Chromosome> originalChromosomes = new ArrayList<>(originalDNA.getChromosomes());
        ArrayList<Chromosome> currentChromosomes = new ArrayList<>(this.chromosomes);

        // sort original and current chromosomes by their unique identifier
        originalChromosomes.sort(Comparator.comparing(Chromosome::getId));
        currentChromosomes.sort(Comparator.comparing(Chromosome::getId));

        // calculate stock increases and decreases for each product/location
        for (int i = 0; i < originalChromosomes.size(); i++) {
            Chromosome originalChromosome = originalChromosomes.get(i);
            Chromosome currentChromosome = currentChromosomes.get(i);

            Long productId = originalChromosome.getProductId();
            Long locationId = originalChromosome.getLocationId();
            int stockChange = currentChromosome.getCurrentStock() - originalChromosome.getCurrentStock();

            // create maps containing locations where products were added/removed for each product
            if (stockChange > 0) {
                surplusMap.computeIfAbsent(productId, k -> new ArrayList<>())
                        .add(new StockChange(locationId, stockChange));
            } else if (stockChange < 0) {
                deficitMap.computeIfAbsent(productId, k -> new ArrayList<>())
                        .add(new StockChange(locationId, -stockChange));
            }
        }

        // loop through stock changes for each product
        for (Long productId : surplusMap.keySet()) {
            List<StockChange> stockSurpluses = surplusMap.get(productId);
            List<StockChange> stockDeficits = deficitMap.get(productId);

            // if a product had no transfers skip this product
            if (stockSurpluses == null || stockDeficits == null) {
                continue;
            }

            // determine product movements form original allocation (DNA) to this allocation (DNA)
            for (StockChange stockSurplus : stockSurpluses) {
                for (StockChange stockDeficit : stockDeficits) {
                    // continue to the next location if all transfers for this location have been found
                    if (stockSurplus.quantity == 0 || stockDeficit.quantity == 0) {
                        continue;
                    }

                    int transferQty = Math.min(stockSurplus.quantity, stockDeficit.quantity);
                    LocationPair pair = new LocationPair(stockSurplus.getLocationId(), stockDeficit.getLocationId());

                    // if there has been no transfer between these two locations, add it to the total distance
                    if (!visitedTransfers.contains(pair)) {
                        totalDistance += distanceMatrix.get(pair);
                        visitedTransfers.add(pair);
                    }

                    productTransferCount++;

                    stockSurplus.quantity -= transferQty;
                    stockDeficit.quantity -= transferQty;
                }
            }
        }
        log.debug("Total distance travelled to move products: {} km with {} transfers.", totalDistance,
                productTransferCount);
        return totalDistance;
    }

    private double calculateDistanceFitness(double distance, double maxDistance) {
        return 1.0 - (distance / maxDistance);
    }
}
