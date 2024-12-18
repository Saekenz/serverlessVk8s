package at.ac.univie.inventorymgmtservice.service;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import at.ac.univie.inventorymgmtservice.dto.InventoryTargetStockUpdateDTO;
import at.ac.univie.inventorymgmtservice.dto.StockAlertDTO;
import at.ac.univie.inventorymgmtservice.model.*;
import at.ac.univie.inventorymgmtservice.outbound.OutboundConfiguration;
import at.ac.univie.inventorymgmtservice.repository.InventoryRepository;
import at.ac.univie.inventorymgmtservice.repository.LocationRepository;
import at.ac.univie.inventorymgmtservice.repository.ProductRepository;
import at.ac.univie.inventorymgmtservice.util.InventoryDataGenerator;
import at.ac.univie.inventorymgmtservice.util.MessageProcessingControl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {
    private static final int LARGE_ORDER_THRESHOLD = 30;
    private static final double STOCK_LOW_THRESHOLD = 0.3;

    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PubSubConfiguration pubSubConfiguration;

    @Autowired
    private MessageProcessingControl processingControl;


    /**
     * Process stock update message receive via PubSub pull subscription
     */
    @Override
    public void handleIncomingTargetStockUpdateMessage(String message) {
        if (processingControl.isProcessingEnabled()) {
            processTargetStockUpdateMessage(message);
        }
        else {
            // if processing is currently paused add message to queue
            boolean isOfferSuccess = messageQueue.offer(message);
            if (!isOfferSuccess) {
                log.warn("Failed adding incoming message to queue!");
            }

            log.info("Processing messages paused. Messages currently queued: {}", messageQueue.size());
        }
    }

    private void processQueuedMessages() {
        int messagesQueued = messageQueue.size();
        while (!messageQueue.isEmpty()) {
            String message = messageQueue.poll();
            processTargetStockUpdateMessage(message);
        }
        log.info("Queued messages processed: {}", messagesQueued);
    }

    private void processTargetStockUpdateMessage(String message) {
        try {
            InventoryTargetStockUpdateDTO recvInv = objectMapper.readValue(message,
                    InventoryTargetStockUpdateDTO.class);

            handleAlerting(recvInv);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse target stock update message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while processing target stock update: {}", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> generateInventoryData() {
        InventoryDataGenerator dataGenerator = new InventoryDataGenerator();
        List<Long> locationIds = locationRepository.getLocationIds();
        List<Long> productIds = productRepository.getProductIds();

        if (productIds.isEmpty() || locationIds.isEmpty()) {
            return ResponseEntity.internalServerError().body("Inventory cannot be created if there are no" +
                    " products/locations stored in the database yet!");
        }

        List<Inventory> generatedInventories = dataGenerator.generateInventories(locationIds, productIds);
        int numInsertedInventories = inventoryRepository.saveAll(generatedInventories).size();

        if (numInsertedInventories == locationIds.size() * productIds.size()) {
            return new ResponseEntity<>(String.format("Successfully generated %s inventory entries!",
                    numInsertedInventories),
                    HttpStatus.CREATED);
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public void handleOutgoingOptimizationMessage() {
        // Trigger an inventory optimization only if one isn't currently running
        if (processingControl.isProcessingEnabled()) {
            log.info("Sending optimization message");
            createAndSendOptMsg();
            processingControl.pauseProcessing();
            log.info("Processing of messages paused.");
        }
    }

    @Override
    public void resumeMessageProcessing() {
        processQueuedMessages();
        processingControl.resumeProcessing();
        log.info("Processing of messages resumed.");
    }

    /**
     * Processes optimization message received via PubSub push subscription
     *
     */
    @Override
    public ResponseEntity<?> processOptimizationFinishedNotification(String payload) {
        if (payload != null) {
            log.info("Received optimization finished notification: {}", payload);

            // Resume message processing asynchronously
            resumeMessageProcessing();

            // Acknowledge the message
            return ResponseEntity.accepted().build();
        }
        else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Processes stock update message received via PubSub push subscription
     *
     */
    @Override
    public ResponseEntity<?> processStockUpdateMessage(String payload) {
        if (payload != null) {
            log.info("Received stock update notification: {}", payload);

            // Parse the message
            InventoryTargetStockUpdateDTO updateDTO = parseStockUpdateMessage(payload);

            if (updateDTO != null) {
                // Check if any alerts have to be triggered
                handleAlerting(updateDTO);
                return ResponseEntity.accepted().build();
            }
            else {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().body("Stock update message body was missing!");
    }

    private InventoryTargetStockUpdateDTO parseStockUpdateMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            if (isStockUpdateMessageValid(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, InventoryTargetStockUpdateDTO.class);
            }
            else {
                log.warn("Incoming stock update message does not adhere to the required schema!");
                return null;
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private boolean isStockUpdateMessageValid(JsonNode jsonNode) {
        return jsonNode.has("productId") && jsonNode.has("locationId")
                && jsonNode.has("quantity");
    }

    private void handleAlerting(InventoryTargetStockUpdateDTO updateDTO) {
        inventoryRepository.findByProductIdAndLocationId(updateDTO.getProductId(), updateDTO.getLocationId())
                .ifPresentOrElse(oldInv -> {
                    if (updateDTO.getOrderedQuantity() > LARGE_ORDER_THRESHOLD) {
                        createAndSendStockAlert(updateDTO, "Large Order");
                    }

                    double newTargetStock = oldInv.getTargetStock() + updateDTO.getOrderedQuantity();
                    if (oldInv.getCurrentStock()/newTargetStock < STOCK_LOW_THRESHOLD)
                        createAndSendStockAlert(updateDTO, "Stock Low");

                    updateTargetStock(updateDTO);

                }, () -> log.warn("Inventory not found for productId: {} and locationId: {}",
                        updateDTO.getProductId(), updateDTO.getLocationId()));
    }

    private void logUpdatedRows(int rowsUpdated) {
        log.debug("Rows updated: {}", rowsUpdated);
    }

    private void updateTargetStock(InventoryTargetStockUpdateDTO inv) {
        int updatedRows = inventoryRepository.increaseTargetStock(inv.getProductId(), inv.getLocationId(),
                inv.getOrderedQuantity());

        logUpdatedRows(updatedRows);
        if (updatedRows < 1) {
            log.error("Error updating target stock for inventory with productId: {} and locationId: {}",
                    inv.getProductId(), inv.getLocationId());
        }
    }

    private void createAndSendStockAlert(InventoryTargetStockUpdateDTO inv, String alertCategory) {
        try {
            StockAlertDTO stockAlert = new StockAlertDTO(inv.getProductId(), inv.getLocationId(), alertCategory);
            String stockAlertJson = objectMapper.writeValueAsString(stockAlert);
            messagingGateway.sendToPubSub(stockAlertJson, pubSubConfiguration.getAlertTopic());
        } catch (Exception e) {
            log.error("Error while creating stock alert: {}", e.getMessage());
        }
    }

    public void createAndSendOptMsg() {
        String optMsgJson = "";
        messagingGateway.sendToPubSub(optMsgJson, pubSubConfiguration.getOptimizeTopic());
    }
}
