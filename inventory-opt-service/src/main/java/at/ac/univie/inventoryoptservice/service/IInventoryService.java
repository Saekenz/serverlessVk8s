package at.ac.univie.inventoryoptservice.service;

import org.springframework.http.ResponseEntity;

public interface IInventoryService {

    ResponseEntity<?> fetchInventoryAllocation();

    void handleIncomingOptimizationMessage(String message);
}
