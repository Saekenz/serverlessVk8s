package at.ac.univie.inventoryoptservice.optimization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class Population {
    private List<DNA> population;
    private List<DNA> crossoverPool;

    private Map<Long, Integer> productsInStock;
    private Map<Long, Integer> locationsPerProduct;
    private Map<Long, Coordinates> uniqueLocationsWithCoords;
    private Set<Long> uniqueLocations;
    private Random rand;

    public Population() {
        this.population = new ArrayList<>();
        this.crossoverPool = new ArrayList<>();
        this.productsInStock = new HashMap<>();
        this.locationsPerProduct = new HashMap<>();
        this.uniqueLocationsWithCoords = new HashMap<>();
        this.uniqueLocations = new HashSet<>();
        this.rand = new Random();
    }

    public void initializePopulation(DNA initialDNA, int populationSize) {
        setProductsAndLocations(initialDNA);
        generatePermutations(initialDNA, populationSize);
    }

    private void setProductsAndLocations(DNA dna) {
        for (Chromosome c : dna.getChromosomes()) {
            int oldQuantityInStock = productsInStock.getOrDefault(c.getProductId(), 0);
            productsInStock.put(c.getProductId(), oldQuantityInStock + c.getCurrentStock());
            uniqueLocationsWithCoords.putIfAbsent(c.getLocationId(), new Coordinates(c.getLatitude(), c.getLongitude()));
            locationsPerProduct.put(c.getProductId(), locationsPerProduct.getOrDefault(c.getProductId(), 0) + 1);
        }
        uniqueLocations.addAll(uniqueLocationsWithCoords.keySet());
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
                int newCurrentStock = rand.nextInt(stockLeft.get(c.getProductId()) + 1);
                newChromosome.setCurrentStock(newCurrentStock);
                stockLeft.put(c.getProductId(), stockLeft.get(c.getProductId()) - newCurrentStock);
            }

            permutation.addChromosome(newChromosome);
        }
        return permutation;
    }

    public void calculateFitness(DNA originalDNA) {
        for (DNA dna : population) {
            dna.calculateFitness(originalDNA);
        }
    }

    public void printPopulation() {
        System.out.println("==================================== Population =========================================");
        for (DNA dna : population) {
            System.out.println("=================== DNA ==================");
            for (Chromosome c : dna.getChromosomes()) {
                System.out.println(c.toString());
            }
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

        // TODO: check if fitness is guaranteed to be > 0 -> if not use linear interpolation
        for (DNA dna : population) {
            double normalizedFitness = dna.getFitness() / maxFitness;
            int n = (int) Math.floor(normalizedFitness * 100);
            for (int i = 0; i < n; i++) {
                crossoverPool.add(dna);
            }
        }
    }

    public void crossover() {
        DNA parent1 = crossoverPool.get(rand.nextInt(crossoverPool.size()));
        DNA parent2 = crossoverPool.get(rand.nextInt(crossoverPool.size()));

        DNA child = new DNA();

        Set<Long> productsTakenFromParent1 = new HashSet<>();
        Set<Long> productsTakenFromParent2 = new HashSet<>();

        for (Long productId : productsInStock.keySet()) {
            if (rand.nextBoolean()) {
                productsTakenFromParent1.add(productId);
            }
            else {
                productsTakenFromParent2.add(productId);
            }
        }

        for (int i = 0; i < parent1.getChromosomes().size(); i++) {
            if (productsTakenFromParent1.contains(parent1.getChromosomes().get(i).getProductId())) {
                child.addChromosome(parent1.getChromosomes().get(i));
            }
            if (productsTakenFromParent2.contains(parent2.getChromosomes().get(i).getProductId())) {
                child.addChromosome(parent2.getChromosomes().get(i));
            }
        }
        System.out.println("Crossover created child with size " + child.getChromosomes().size());
        System.out.println("=================== Child DNA ==================");
        for (Chromosome c : child.getChromosomes()) {
            System.out.println(c.toString());
        }
    }
}
