package at.ac.univie.inventoryoptservice.service;

import at.ac.univie.inventoryoptservice.config.OptimizationConfig;
import at.ac.univie.inventoryoptservice.dto.InventoryAllocationDTO;
import at.ac.univie.inventoryoptservice.config.PubSubConfiguration;
import at.ac.univie.inventoryoptservice.optimization.*;
import at.ac.univie.inventoryoptservice.outbound.OutboundConfiguration;
import at.ac.univie.inventoryoptservice.repository.InventoryRepository;
import at.ac.univie.inventoryoptservice.util.OptimizationMessageBuilder;
import at.ac.univie.inventoryoptservice.util.OptimizationMessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {
    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;
    private final OptimizationMessageBuilder optimizationMessageBuilder;
    private final OptimizationMessageParser optimizationMessageParser;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PubSubConfiguration pubSubConfiguration;

    @Autowired
    private OptimizationConfig optimizationConfig;

    @Override
    public ResponseEntity<?> fetchInventoryAllocation() {
        List<InventoryAllocationDTO> invAllocations = inventoryRepository.fetchInventoryWithLocation();
        return ResponseEntity.ok(invAllocations);
    }

    /**
     * Fetches the configuration currently used by the optimization service to run the genetic algorithm.
     *
     * @return {@link ResponseEntity} containing the current optimization configuration in JSON format and
     * status code {@code 200}.
     */
    @Override
    public ResponseEntity<?> getOptimizationConfig() {
        return ResponseEntity.ok(optimizationConfig);
    }

    /**
     * Makes changes to the configuration that is currently being used by the optimization service to run the
     * genetic algorithm.
     *
     * @param config {@link OptimizationConfig} object containing the optimization parameters.
     * @return Empty {@link ResponseEntity} and status code {@code 204}.
     */
    @Override
    public ResponseEntity<?> updateOptimizationConfig(OptimizationConfig config) {
        if (config != null) {
            optimizationConfig.setConfig(config);
            log.info("Optimization config updated: {}", optimizationConfig.toString());
            return ResponseEntity.noContent().build();
        }
        else {
            return ResponseEntity.badRequest().body("Optimization config is missing parameters!");
        }
    }

    @Override
    public void handleIncomingOptimizationMessage(String message) {
        // optionally retrieve optimization configuration from incoming message
        OptimizationConfig config = Objects.requireNonNullElse(
                optimizationMessageParser.parseOptimizationMessage(message),
                optimizationConfig);

        // start timer to measure genetic algorithm runtime
        long startTime = System.nanoTime();

        // 1) fetch data needed for optimization
        List<Chromosome> initialChromosomes = fetchInventoryData();
        DNA initalDNA = new DNA(initialChromosomes);

        // run the genetic algorithm to get an optimized allocation
        log.info("Used config: {}", config);
        DNA bestDNA = runGeneticAlgorithm(initalDNA, config);

        // end timer and log the elapsed time
        long endTime = System.nanoTime();
        logElapsedTime(startTime, endTime);

        // 5) create stock optimization messages
        List<String> stockOptimizationMessages = optimizationMessageBuilder
                .createStockOptimizationMessagesFromDNA(bestDNA);

        // 6) send the messages to the optimization topic
        sendStockOptimizationMessages(stockOptimizationMessages);

        // 7) create and send message that signals completion of optimization
        createAndSendOptimizationFinishedMessage();
    }

    /**
     * Fetches data from database tables {@code inventory} and {@code location} from the database and converts it into
     * a {@link List} of {@link Chromosome} objects for use in the genetic algorithm.
     *
     * @return {@link List} of {@link Chromosome} objects based on data found in the database.
     */
    private List<Chromosome> fetchInventoryData() {
        return inventoryRepository.fetchInventoryWithLocation().stream()
                .map(InventoryAllocationDTO::toChromosome)
                .toList();
    }

    /**
     * Takes the current allocation of products to locations in form a {@link DNA} object and runs a genetic algorithm
     * that tries to create an optimized version of it and return it.
     *
     * @param initialDNA The initial allocation of products to locations as found in the database.
     * @param config The configuration used to run the genetic algorithm.
     * @return An optimized version of the supplied {@link DNA} object.
     */
    private DNA runGeneticAlgorithm(DNA initialDNA, OptimizationConfig config) {
        Population population = new Population(initialDNA, config.getMutationRate());
        population.initializePopulation(config.getPopulationSize());
        log.debug("Initial population size: {}", population.getPopulation().size());

        // 2) selection
        while(population.getGenerations() < config.getGenerations()) {
            // 2a) evaluate fitness of each element in the population
            population.calculateFitness();

            // 2b) create a pool for roulette wheel selection (adding fitter elements more often)
            population.generateCrossoverPool();

            // 3) create a new population and replace the old one with it
            population.generateNextGeneration();
            log.debug("New generation population size: {}", population.getPopulation().size());
        }

        // 4) pick the best element of the final generation
        population.calculateFitness();
        DNA bestDNA = population.findFittestDNA();
        log.info("Best DNA fitness of last generation: {}", bestDNA.getFitness());
        return bestDNA;
    }

    /**
     * Sends stock optimization messages to a PubSub topic via a messaging gateway.
     *
     * @param stockOptimizationMessages A {@link List} of {@link String} objects representing stock optimization
     *                                  messages.
     */
    private void sendStockOptimizationMessages(List<String> stockOptimizationMessages) {
        if (!stockOptimizationMessages.contains("")) {
            int counter = 0;
            for (String stockOptimizationMessage : stockOptimizationMessages) {
                counter++;
                messagingGateway.sendToPubSub(stockOptimizationMessage, pubSubConfiguration.getStockUpdateTopic());
                log.debug("Sent stock optimization message {}: {}", counter, stockOptimizationMessage);
            }
            log.debug("Stock optimization messages sent: {}", counter);
        }
    }

    /**
     * Creates a message with an empty body that is sent to the {@code inv-opt-finished} PubSub topic.
     *
     */
    private void createAndSendOptimizationFinishedMessage() {
        String messageContent = "";
        messagingGateway.sendToPubSub(messageContent, pubSubConfiguration.getOptFinishedTopic());
        log.info("Sent optimization finished message!");
    }

    /**
     * Calculates the elapsed time in seconds between two points in time.
     * This method takes two {@code long} numbers representing the start and end times in nanoseconds
     * and calculates the time in seconds between them.
     *
     * @param startTime The starting point in nanoseconds.
     * @param endTime The ending point in nanoseconds.
     */
    private void logElapsedTime(long startTime, long endTime) {
        double elapsedTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;
        log.info("Execution time: {} seconds", elapsedTimeInSeconds);
    }
}
