package at.ac.univie.inventoryoptservice.optimization;

import at.ac.univie.inventoryoptservice.util.DistanceCalculator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Data
@AllArgsConstructor
@Slf4j
public class Population {
    private DNA initialDNA;
    private double mutationRate;
    private int generations;
    private List<DNA> population;
    private List<DNA> crossoverPool;

    private Map<Long, Integer> productsInStock;
    private Map<Long, Integer> locationsPerProduct;
    private List<SimpleLocation> uniqueLocations;

    private DistanceCalculator distanceCalculator;
    private Map<LocationPair, Double> distanceMatrix;
    private double maxPossibleTransferDistance;

    private Random rand;

    public Population(DNA initialDNA, double mutationRate) {
        this.initialDNA = initialDNA;
        this.mutationRate = mutationRate;
        this.generations = 0;
        this.population = new ArrayList<>();
        this.crossoverPool = new ArrayList<>();
        this.productsInStock = new HashMap<>();
        this.locationsPerProduct = new HashMap<>();
        this.uniqueLocations = new ArrayList<>();
        this.distanceCalculator = new DistanceCalculator();
        this.distanceMatrix = new HashMap<>();
        this.maxPossibleTransferDistance = 0.0;
        this.rand = new Random();
    }

    public void initializePopulation(int populationSize) {
        setProductsAndLocations(initialDNA);
        calculateDistanceMatrix();
        setMaxPossibleTransferDistance();
        generatePermutations(initialDNA, populationSize);
    }

    private void calculateDistanceMatrix() {
        this.distanceMatrix = distanceCalculator.calculateDistanceMatrix(uniqueLocations);
    }

    private void setMaxPossibleTransferDistance() {
        this.maxPossibleTransferDistance = distanceCalculator.calculateMaxPossibleDistance(distanceMatrix);
    }

    private void setProductsAndLocations(DNA dna) {
        Set<SimpleLocation> uniqueLocationsTemp = new HashSet<>();
        for (Chromosome c : dna.getChromosomes()) {
            int oldQuantityInStock = productsInStock.getOrDefault(c.getProductId(), 0);
            productsInStock.put(c.getProductId(), oldQuantityInStock + c.getCurrentStock());
            locationsPerProduct.put(c.getProductId(), locationsPerProduct.getOrDefault(c.getProductId(), 0) + 1);
            uniqueLocationsTemp.add(new SimpleLocation(c.getLocationId(), c.getLatitude(), c.getLongitude()));
        }
        this.uniqueLocations = new ArrayList<>(uniqueLocationsTemp);
    }

    /**
     *
     * @param dna The {@link DNA} object for which the permutations are to be created.
     * @param num The number of permutations that are to be created.
     */
    private void generatePermutations(DNA dna, int num) {
        List<DNA> permutations = new ArrayList<>();
        permutations.add(dna);
        for (int i = 1; i < num; i++) {
            DNA generatedDNA = generatePermutation(dna);
            permutations.add(generatedDNA);
        }
        this.population.addAll(permutations);
    }

    /**
     * Generates a permutation of a given {@link DNA} object by changing the {@code currentStock} property of its
     * {@link Chromosome} objects while retaining the total stock numbers.
     *
     * @param dna The {@link DNA} object for which a permutation is to be created.
     * @return {@link DNA} object with adjusted current stock values for each {@link Chromosome}.
     */
    private DNA generatePermutation(DNA dna) {
        // to keep track of the overall stock left for each product
        Map<Long, Integer> stockLeft = new HashMap<>(this.productsInStock);

        // to keep track of the number of locations that store each product
        Map<Long, Integer> locationsLeft = new HashMap<>(this.locationsPerProduct);

        DNA permutation = new DNA();
        for (Chromosome c : dna.getChromosomes()) {
            // decrement the number of locations where the product still needs to be stored
            locationsLeft.put(c.getProductId(), locationsLeft.get(c.getProductId()) - 1);

            // keep all parameters except for currentStock
            Chromosome newChromosome = new Chromosome();
            newChromosome.setId(c.getId());
            newChromosome.setProductId(c.getProductId());
            newChromosome.setLocationId(c.getLocationId());
            newChromosome.setTargetStock(c.getTargetStock());
            newChromosome.setLatitude(c.getLatitude());
            newChromosome.setLongitude(c.getLongitude());

            // if the last location containing the product is reached use all remaining stock
            if (locationsLeft.get(c.getProductId()) == 0) {
                newChromosome.setCurrentStock(stockLeft.get(c.getProductId()));
                stockLeft.put(c.getProductId(), 0);
            } else {
                // pick a random number from the total stock that is left for this product
                int stockLeftForProduct = stockLeft.get(c.getProductId());
                int newCurrentStock = rand.nextInt((int) (stockLeftForProduct * 0.2), stockLeftForProduct + 1);
                newChromosome.setCurrentStock(newCurrentStock);
                stockLeft.put(c.getProductId(), stockLeft.get(c.getProductId()) - newCurrentStock);
            }

            // add the newly created Chromosome to the new DNA object
            permutation.addChromosome(newChromosome);
        }
        return permutation;
    }

    /**
     * Determines the fitness of each {@link DNA} object in the population.
     */
    public void calculateFitness() {
        for (DNA dna : population) {
            // calculate fitness of each DNA object based on the initial DNA object
            dna.calculateFitness(initialDNA, distanceMatrix, maxPossibleTransferDistance);
            log.debug("Calculated fitness: {}", dna.getFitness());
        }
    }

    /**
     * Picks and returns the best DNA contained in this population.
     *
     * @return The {@link DNA} object with the highest fitness found in this {@code population} or a default {@link DNA}
     * object if no best {@link DNA} object could be found.
     */
    public DNA findFittestDNA() {

        return this.population.stream()
                .max(Comparator.comparingDouble(DNA::getFitness))
                .orElse(new DNA());
    }

    public void generateCrossoverPool() {
        // normalize fitness values to be between 0 - 1
        // determine n for each member of the population
        // add each member of the pop n times to the crossover pool
        crossoverPool.clear();
        double maxFitness = findFittestDNA().getFitness();

        for (DNA dna : population) {
            double normalizedFitness = dna.getFitness() / maxFitness;
            int n = (int) Math.floor(normalizedFitness * 100);
            for (int i = 0; i < n; i++) {
                crossoverPool.add(dna);
            }
        }
    }

    /**
     * Replaces the current population with a new one by creating new {@link DNA} objects through crossover
     * and mutation. Increments the {@code generations} count for each call.
     *
     */
    public void generateNextGeneration() {
        // apply elitism (carry over the best element of the last generation)
        population.set(0, findFittestDNA());

        for (int i = 1; i < population.size(); i++) {
            // pick 2 random objects from the crossover pool
            DNA parent1 = crossoverPool.get(rand.nextInt(crossoverPool.size()));
            DNA parent2 = crossoverPool.get(rand.nextInt(crossoverPool.size()));

            // perform crossover & mutation
            DNA child = parent1.crossover(parent2);
            child.mutate(mutationRate);

            // add the newly created DNA object the "new" population
            population.set(i, child);
        }
        // increase the counter that keeps track of the number of generations that have already been created.
        generations++;
    }
}
