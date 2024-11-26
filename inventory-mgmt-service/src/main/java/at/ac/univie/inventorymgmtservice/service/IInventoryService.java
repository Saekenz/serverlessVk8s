package at.ac.univie.inventorymgmtservice.service;

import org.springframework.http.ResponseEntity;

public interface IInventoryService {

    void handleIncomingTargetStockUpdateMessage(String message);

    void handleIncomingCurrentStockUpdateMessage(String message);

    ResponseEntity<?> generateInventoryData();

    void handleOutgoingOptimizationMessage();
}
