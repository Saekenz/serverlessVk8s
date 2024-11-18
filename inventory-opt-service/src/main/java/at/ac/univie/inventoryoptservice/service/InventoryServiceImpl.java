package at.ac.univie.inventoryoptservice.service;

import at.ac.univie.inventoryoptservice.model.InventoryAllocationDTO;
import at.ac.univie.inventoryoptservice.config.PubSubConfiguration;
import at.ac.univie.inventoryoptservice.model.StockUpdateDTO;
import at.ac.univie.inventoryoptservice.optimization.*;
import at.ac.univie.inventoryoptservice.outbound.OutboundConfiguration;
import at.ac.univie.inventoryoptservice.repository.InventoryRepository;
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
        List<Chromosome> initialChromosomes = fetchInventoryData();
        DNA initalDNA = new DNA(initialChromosomes);
        System.out.println(initalDNA);

        // 1b) create a population (permutations of input data)
        Population population = new Population();
        population.initializePopulation(initalDNA, config.getPopulationSize());
        population.printPopulation();

        // 2a) evaluate fitness of each element in the population

        // 2b) create a mating pool (adding fitter elements more often

        // 3a) pick 2 elements from the mating pool

        // 3b) crossover the 2 parent elements to create a new element

        // 3c) mutate the new element based on the mutation probability

        // 3d) add the new element to a new population and repeat the process from 3a)

        // 4) replace the old population with the new one and repeat the process from step 2a)

    }

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

    private List<Chromosome> fetchInventoryData() {
        return inventoryRepository.fetchInventoryWithLocation().stream()
                .map(InventoryAllocationDTO::toChromosome)
                .toList();
    }

    private InventoryAllocationDTO optimizeAllocation(InventoryAllocationDTO invAllocation) {
        InventoryAllocationDTO newAllocation = new InventoryAllocationDTO();
        return newAllocation;
    }

    @Override
    public ResponseEntity<?> pubsubTest() {
        try {
            StockUpdateDTO stockUpdateDTO = new StockUpdateDTO(1L, 3L, 33);
            String stockUpdateJson = new ObjectMapper().writeValueAsString(stockUpdateDTO);

            messagingGateway.sendToPubSub(stockUpdateJson, pubSubConfiguration.getTopic());
            return ResponseEntity.ok().body(stockUpdateJson);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
