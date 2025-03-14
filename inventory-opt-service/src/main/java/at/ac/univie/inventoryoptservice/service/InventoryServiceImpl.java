package at.ac.univie.inventoryoptservice.service;

import at.ac.univie.inventoryoptservice.config.OptimizationConfig;
import at.ac.univie.inventoryoptservice.dto.InventoryAllocationDTO;
import at.ac.univie.inventoryoptservice.dto.StockOptimizationDTO;
import at.ac.univie.inventoryoptservice.optimization.*;
import at.ac.univie.inventoryoptservice.outbound.OutboundMessageHandler;
import at.ac.univie.inventoryoptservice.repository.ConfigurationRepository;
import at.ac.univie.inventoryoptservice.repository.InventoryRepository;
import at.ac.univie.inventoryoptservice.util.OptimizationMessageBuilder;
import at.ac.univie.inventoryoptservice.util.OptimizationMessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {
    private static final String MUTATION_RATE = "inv.opt.ga.mutationRate";
    private static final String POPULATION_SIZE = "inv.opt.ga.populationSize";
    private static final String GENERATIONS = "inv.opt.ga.generations";

    private final OptimizationMessageBuilder optimizationMessageBuilder;
    private final OptimizationMessageParser optimizationMessageParser;
    private final GeneticAlgorithm geneticAlgorithm;
    private final OutboundMessageHandler outboundMessageHandler;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ConfigurationRepository configRepository;

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
        loadOptimizationConfig();
        return ResponseEntity.ok(optimizationConfig);
    }

    private void loadOptimizationConfig() {
        configRepository.findByNameStartingWith("inv.opt.ga").forEach(configEntry -> {
            switch (configEntry.getName()) {
                case MUTATION_RATE -> optimizationConfig
                        .setMutationRate(Double.parseDouble(configEntry.getValue()));
                case POPULATION_SIZE -> optimizationConfig
                        .setPopulationSize(Integer.parseInt(configEntry.getValue()));
                case GENERATIONS -> optimizationConfig
                        .setGenerations(Integer.parseInt(configEntry.getValue()));
                default -> log.error("Unknown optimization configuration found while loading from database: {}",
                        configEntry.getName());
            }
        });
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

            // save new config to database
            storeOptimizationConfig(config);

            // update current config to new config
            optimizationConfig.setConfig(config);

            log.info("Optimization config updated: {}", optimizationConfig.toString());
            return ResponseEntity.noContent().build();
        }
        else {
            return ResponseEntity.badRequest().body("Optimization config is missing parameters!");
        }
    }

    private void storeOptimizationConfig(OptimizationConfig config) {
        configRepository.updateConfigEntryValueByName(MUTATION_RATE,
                String.valueOf(config.getMutationRate()));
        configRepository.updateConfigEntryValueByName(POPULATION_SIZE,
                String.valueOf(config.getPopulationSize()));
        configRepository.updateConfigEntryValueByName(GENERATIONS,
                String.valueOf(config.getGenerations()));
    }

    /**
     * Processes optimization request receives via PubSub push subscription
     */
    @Override
    public ResponseEntity<?> processOptimizationRequest(String payload) {
        if (payload != null) {
            log.info("Received optimization request: {}", payload);

            // Handle the optimization
            handleIncomingOptimizationMessage(payload);

            // Acknowledge the message
            return ResponseEntity.accepted().build();
        }
        else {
            return ResponseEntity.badRequest().body("Start optimization message request body was null!");
        }
    }

    @Override
    public void handleIncomingOptimizationMessage(String message) {
        // optionally retrieve optimization configuration from incoming message
        OptimizationConfig config = optimizationMessageParser.parseOptimizationMessage(message);

        // if no config is passed with the message fetch the latest config from the database
        if (config == null) {
            loadOptimizationConfig();
            config = optimizationConfig;
        }

        // start timer to measure genetic algorithm runtime
        long startTime = System.nanoTime();

        // fetch data needed for optimization
        List<Chromosome> initialChromosomes = fetchInventoryData();
        DNA initalDNA = new DNA(initialChromosomes);
        log.info("Initial DNA fitness: {}", initalDNA.calculateDemandCoverage());

        // run the genetic algorithm to get an optimized allocation
        DNA bestDNA = geneticAlgorithm.runGeneticAlgorithm(initalDNA, config);
        log.info("Best DNA fitness of last generation: {}", bestDNA.getFitness());

        // end timer and log the elapsed time
        long endTime = System.nanoTime();
        logElapsedTime(startTime, endTime);

        // apply stock optimization to the database
        applyStockOptimization(bestDNA);

        // create and send message that signals completion of optimization
        outboundMessageHandler.createAndSendOptimizationFinishedMessage();
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

    private void applyStockOptimization(DNA dna) {
        List<StockOptimizationDTO> optimizationDTOS = optimizationMessageBuilder
                .createStockOptimizationFromDNA(dna);

        if (optimizationDTOS.isEmpty()) {
            log.info("No stock optimizations to apply.");
            return;
        }

        int numRowsUpdated = applyUpdatesFromList(optimizationDTOS);

        if (numRowsUpdated != optimizationDTOS.size()) {
            log.warn("Mismatch in stock optimization updates! Expected {} updates, but {} rows were updated.",
                    optimizationDTOS.size(), numRowsUpdated);
        }
        else {
            log.info("Successfully applied stock optimization to {} entries.", numRowsUpdated);
        }
    }

    private int applyUpdatesFromList(List<StockOptimizationDTO> dtos) {
        int numRowsUpdated = 0;

        for (StockOptimizationDTO dto : dtos) {
            numRowsUpdated += inventoryRepository.updateCurrentStock(dto.getProductId(),
                    dto.getLocationId(), dto.getNewCurrentStock());
        }

        return numRowsUpdated;
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
