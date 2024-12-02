package at.ac.univie.inventoryoptservice.service;

import at.ac.univie.inventoryoptservice.config.OptimizationConfig;
import org.springframework.http.ResponseEntity;

public interface IInventoryService {

    ResponseEntity<?> fetchInventoryAllocation();

    void handleIncomingOptimizationMessage(String message);

    ResponseEntity<?> updateOptimizationConfig(OptimizationConfig config);

    ResponseEntity<?> getOptimizationConfig();
}
