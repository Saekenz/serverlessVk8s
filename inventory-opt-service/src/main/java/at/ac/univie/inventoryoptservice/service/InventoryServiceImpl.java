package at.ac.univie.inventoryoptservice.service;

import at.ac.univie.inventoryoptservice.config.OptimizationConfig;
import at.ac.univie.inventoryoptservice.config.OptimizationConfigFactory;
import at.ac.univie.inventoryoptservice.dto.InventoryAllocationDTO;
import at.ac.univie.inventoryoptservice.config.PubSubConfiguration;
import at.ac.univie.inventoryoptservice.dto.StockOptimizationDTO;
import at.ac.univie.inventoryoptservice.optimization.*;
import at.ac.univie.inventoryoptservice.outbound.OutboundConfiguration;
import at.ac.univie.inventoryoptservice.repository.InventoryRepository;
import at.ac.univie.inventoryoptservice.util.OptimizationMessageBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;
    private final OptimizationMessageBuilder optimizationMessageBuilder;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PubSubConfiguration pubSubConfiguration;

    @Override
    public ResponseEntity<?> fetchInventoryAllocation() {
        List<InventoryAllocationDTO> invAllocations = inventoryRepository.fetchInventoryWithLocation();
        return ResponseEntity.ok(invAllocations);
    }

    @Override
    public void handleIncomingOptimizationMessage(String message) {
        // retrieve optimization configuration from incoming message
        OptimizationConfig config = parseOptimizationMessage(message);

        // 1a) fetch data needed for optimization
        long startTime = System.nanoTime();
        List<Chromosome> initialChromosomes = fetchInventoryData();
        DNA initalDNA = new DNA(initialChromosomes);

        // 1b) create a population (permutations of input data)
        Population population = new Population(initalDNA, config.getMutationRate());
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
        long endTime = System.nanoTime();
        logElapsedTime(startTime, endTime);

        // 6) create stock optimization messages
        List<String> stockOptimizationMessages = optimizationMessageBuilder
                .createStockOptimizationMessagesFromDNA(bestDNA);

        // 7) send the messages to the optimization topic
        sendStockOptimizationMessages(stockOptimizationMessages);

        // 8) create and state message that signals completion of optimization
        createAndSendOptimizationFinishedMessage();
    }

    /**
     * Parses a message that adheres to the schema of {@link OptimizationConfig}.
     *
     * @param message The message that is to be parsed.
     * @return A {@link OptimizationConfig} object containing the information found in the message. Returns a
     * {@link OptimizationConfig} object with default values if an error occurs during parsing.
     */
    private OptimizationConfig parseOptimizationMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(message, OptimizationConfig.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());

            // return default config if parsing fails
            return OptimizationConfigFactory.createDefaultOptConfig();
        }
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

    private void createAndSendOptimizationFinishedMessage() {
        String messageContent = "";
        messagingGateway.sendToPubSub(messageContent, pubSubConfiguration.getOptFinishedTopic());
        log.info("Sent optimization finished message!");
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

    @Override
    public ResponseEntity<?> pubsubTest() {
        try {
            StockOptimizationDTO stockOptimizationDTO = new StockOptimizationDTO(1L, 3L, 33);
            String stockUpdateJson = new ObjectMapper().writeValueAsString(stockOptimizationDTO);

            messagingGateway.sendToPubSub(stockUpdateJson, pubSubConfiguration.getStockUpdateTopic());
            return ResponseEntity.ok().body(stockUpdateJson);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private void logElapsedTime(long startTime, long endTime) {
        double elapsedTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;
        log.info("Execution time: {} seconds", elapsedTimeInSeconds);
    }

    private void sendOptMessagesTest() {
        String msg1 = "{"
                + "\"productId\":105,"
                + "\"locationId\":12,"
                + "\"newCurrentStock\":77"
                + "}";

        String msg2 = "{"
                + "\"productId\":103,"
                + "\"locationId\":12,"
                + "\"newCurrentStock\":14"
                + "}";

        String msg3 = "{"
                + "\"productId\":107,"
                + "\"locationId\":12,"
                + "\"newCurrentStock\":23"
                + "}";

        sendStockOptimizationMessages(List.of(msg1, msg2, msg3));
    }
}
