package at.ac.univie.inventorymgmtservice.service;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import at.ac.univie.inventorymgmtservice.model.CurrentStockUpdateDTO;
import at.ac.univie.inventorymgmtservice.model.Inventory;
import at.ac.univie.inventorymgmtservice.model.TargetStockUpdateDTO;
import at.ac.univie.inventorymgmtservice.model.StockAlertDTO;
import at.ac.univie.inventorymgmtservice.outbound.OutboundConfiguration;
import at.ac.univie.inventorymgmtservice.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public void handleIncomingOrderItem(String message) {
        try {
            TargetStockUpdateDTO inv = objectMapper.readValue(message, TargetStockUpdateDTO.class);

            Optional<Inventory> foundInv = inventoryRepository.findByProductIdAndLocationId(
                    inv.getProductId(), inv.getLocationId());

            if (foundInv.isPresent()) {
                if (inv.getOrderedQuantity() > LARGE_ORDER_THRESHOLD) {
                    createAndSendStockAlert(inv, "Large Order");
                }

                double newTargetStock = foundInv.get().getTargetStock() + inv.getOrderedQuantity();
                if (foundInv.get().getCurrentStock()/newTargetStock < STOCK_LOW_THRESHOLD)
                    createAndSendStockAlert(inv, "Stock Low");

//                updateTargetStock(inv);

                foundInv.get().setTargetStock(foundInv.get().getTargetStock() + inv.getOrderedQuantity());
                Inventory updatedInv = inventoryRepository.save(foundInv.get());
                log.info("Target stock updated: {}", updatedInv);
            }
        } catch (Exception e) {
            log.error("Error while processing order item: {}", e.getMessage());
        }
    }

    private void updateTargetStock(TargetStockUpdateDTO inv) {
        int rowsUpdated = inventoryRepository.updateTargetStock(inv.getProductId(), inv.getLocationId(),
                inv.getOrderedQuantity());

        log.info("Rows updated: {}", rowsUpdated);
        if (rowsUpdated < 1) {
            log.error("Error updating current stock");
        }
    }

    private void createAndSendStockAlert(TargetStockUpdateDTO inv, String alertCategory) {
        try {
            StockAlertDTO stockAlert = new StockAlertDTO(inv.getProductId(), inv.getLocationId(), alertCategory);
            String stockAlertJson = objectMapper.writeValueAsString(stockAlert);
            messagingGateway.sendToPubSub(stockAlertJson, pubSubConfiguration.getAlertTopic());
        } catch (Exception e) {
            log.error("Error while creating stock alert: {}", e.getMessage());
        }
    }

    @Override
    public void handleIncomingStockOptimization(String message) {
        try {
            CurrentStockUpdateDTO inv = objectMapper.readValue(message, CurrentStockUpdateDTO.class);

            Optional<Inventory> foundInv = inventoryRepository.findByProductIdAndLocationId(inv.getProductId(),
                    inv.getLocationId());

            if (foundInv.isPresent()) {
                foundInv.get().setCurrentStock(inv.getNewCurrentStock());
                Inventory updatedInv = inventoryRepository.save(foundInv.get());
                log.info("Current Stock updated: {}", updatedInv);
            }

        } catch (Exception e) {
            log.error("Error while processing inventory optimization: {}", e.getMessage());
        }
    }
}
