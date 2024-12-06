package at.ac.univie.inventoryoptservice.service;

import at.ac.univie.inventoryoptservice.config.OptimizationConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

public interface IInventoryService {

    ResponseEntity<?> fetchInventoryAllocation();

    @Async
    void handleIncomingOptimizationMessage(String message);

    ResponseEntity<?> updateOptimizationConfig(OptimizationConfig config);

    ResponseEntity<?> getOptimizationConfig();

    ResponseEntity<?> processOptimizationRequest(String payload);
}
