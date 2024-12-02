package at.ac.univie.inventoryoptservice.optimization;

import at.ac.univie.inventoryoptservice.config.OptimizationConfig;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NoArgsConstructor
public class GeneticAlgorithm {

    /**
     * Takes the current allocation of products to locations in form a {@link DNA} object and runs a genetic algorithm
     * that tries to create an optimized version of it and return it.
     *
     * @param initialDNA The initial allocation of products to locations as found in the database.
     * @param config The configuration used to run the genetic algorithm.
     * @return An optimized version of the supplied {@link DNA} object.
     */
    public DNA runGeneticAlgorithm(DNA initialDNA, OptimizationConfig config) {
        log.info("Used config: {}", config);
        Population population = new Population(initialDNA, config.getMutationRate());
        population.initializePopulation(config.getPopulationSize());
        log.debug("Initial population size: {}", population.getPopulation().size());

        // selection
        while(population.getGenerations() < config.getGenerations()) {
            // evaluate fitness of each element in the population
            population.calculateFitness();

            // create a pool for roulette wheel selection (adding fitter elements more often)
            population.generateCrossoverPool();

            // create a new population and replace the old one with it
            population.generateNextGeneration();
            log.debug("New generation population size: {}", population.getPopulation().size());
        }

        // calculate the fitness of the final generation and pick the best element
        population.calculateFitness();
        return population.findFittestDNA();
    }
}
