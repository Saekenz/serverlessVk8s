package at.ac.univie.inventoryoptservice.optimization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class Population {
    private List<DNA> population;
    private Map<Long, Integer> productsInStock;
    private Map<Long, Integer> locationsPerProduct;
    private Map<Long, Coordinates> uniqueLocationsWithCoords;
    private Set<Long> uniqueLocations;
    private Random rand;

    public Population() {
        this.population = new ArrayList<>();
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
        this.population.add(dna);
    }

    private void generatePermutations(DNA dna, int num) {
        List<DNA> permutations = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            System.out.println("permutation " + i);
            DNA generatedDNA = generatePermutation(dna);
            permutations.add(generatedDNA);
        }
        this.population.addAll(permutations);
    }

    private DNA generatePermutation(DNA dna) {
        Map<Long, Integer> stockLeft = this.productsInStock;

        DNA permutation = new DNA();
        for (Chromosome c : dna.getChromosomes()) {
            locationsPerProduct.put(c.getProductId(), locationsPerProduct.get(c.getProductId()) -1);
            // TODO: clean up code!!!!
            Chromosome newChromosome = new Chromosome();
            newChromosome.setProductId(c.getProductId());
            newChromosome.setLocationId(c.getLocationId());
            newChromosome.setTargetStock(c.getTargetStock());
            newChromosome.setLatitude(c.getLatitude());
            newChromosome.setLongitude(c.getLongitude());

            if (locationsPerProduct.get(c.getProductId()) == 0) {
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

    public void printPopulation() {
        for (DNA dna : population) {
            System.out.println("=================== DNA ==================");
            for (Chromosome c : dna.getChromosomes()) {
                System.out.println(c.toString());
            }
        }
    }

}
