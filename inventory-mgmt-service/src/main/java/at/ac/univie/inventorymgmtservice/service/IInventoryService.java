package at.ac.univie.inventorymgmtservice.service;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

public interface IInventoryService {

    boolean handleTargetStockUpdateFromPullSubscription(String message);

    ResponseEntity<?> generateInventoryData();

    void handleOutgoingOptimizationMessage();

    @Async
    void resumeMessageProcessing();

    ResponseEntity<?> processOptimizationFinishedNotification(String payload);

    ResponseEntity<?> handleTargetStockUpdateFromPushSubscription(String payload);

    void checkProcessingStatus();
}
