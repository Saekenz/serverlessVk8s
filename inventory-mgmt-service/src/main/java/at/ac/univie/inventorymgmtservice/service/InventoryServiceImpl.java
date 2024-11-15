package at.ac.univie.inventorymgmtservice.service;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import at.ac.univie.inventorymgmtservice.model.InventoryCurrentStockUpdateDTO;
import at.ac.univie.inventorymgmtservice.model.InventoryTargetStockUpdateDTO;
import at.ac.univie.inventorymgmtservice.model.StockAlertDTO;
import at.ac.univie.inventorymgmtservice.outbound.OutboundConfiguration;
import at.ac.univie.inventorymgmtservice.repository.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {
    private static final int LARGE_ORDER_THRESHOLD = 30;
    private static final double STOCK_LOW_THRESHOLD = 0.3;

    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PubSubConfiguration pubSubConfiguration;


    @Override
    public void handleIncomingTargetStockUpdateMessage(String message) {
        try {
            InventoryTargetStockUpdateDTO recvInv = objectMapper.readValue(message,
                    InventoryTargetStockUpdateDTO.class);

            inventoryRepository.findByProductIdAndLocationId(recvInv.getProductId(), recvInv.getLocationId())
                    .ifPresentOrElse(oldInv -> {
                        if (recvInv.getOrderedQuantity() > LARGE_ORDER_THRESHOLD) {
                            createAndSendStockAlert(recvInv, "Large Order");
                        }

                        double newTargetStock = oldInv.getTargetStock() + recvInv.getOrderedQuantity();
                        if (oldInv.getCurrentStock()/newTargetStock < STOCK_LOW_THRESHOLD)
                            createAndSendStockAlert(recvInv, "Stock Low");

                        updateTargetStock(recvInv);

                    }, () -> log.warn("Inventory not found for productId: {} and locationId: {}",
                            recvInv.getProductId(), recvInv.getLocationId()));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse target stock update message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while processing target stock update: {}", e.getMessage());
        }
    }

    @Override
    public void handleIncomingCurrentStockUpdateMessage(String message) {
        try {
            InventoryCurrentStockUpdateDTO recvInv = objectMapper.readValue(message,
                    InventoryCurrentStockUpdateDTO.class);

            int updatedRows = inventoryRepository.updateCurrentStock(recvInv.getProductId(), recvInv.getLocationId(),
                    recvInv.getNewCurrentStock());

            logUpdatedRows(updatedRows);
            if (updatedRows < 1) {
                log.error("Error updating current stock for inventory with productId: {} and locationId: {}",
                        recvInv.getProductId(), recvInv.getLocationId());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse inventory optimization message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while processing inventory optimization: {}", e.getMessage());
        }
    }

    private void logUpdatedRows(int rowsUpdated) {
        log.info("Rows updated: {}", rowsUpdated);
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
}
