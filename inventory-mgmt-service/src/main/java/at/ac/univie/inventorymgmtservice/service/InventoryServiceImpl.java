package at.ac.univie.inventorymgmtservice.service;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import at.ac.univie.inventorymgmtservice.model.Inventory;
import at.ac.univie.inventorymgmtservice.model.InventoryUpdateDTO;
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
            InventoryUpdateDTO inv = objectMapper.readValue(message, InventoryUpdateDTO.class);

            Optional<Inventory> foundInv = inventoryRepository.findByProductIdAndLocationId(
                    inv.getProductId(), inv.getLocationId());

            if (foundInv.isPresent()) {
                if (inv.getOrderedQuantity() > LARGE_ORDER_THRESHOLD) {
                    createAndSendStockAlert(inv, "Large Order");
                }

                double newTargetStock = foundInv.get().getTargetStock() + inv.getOrderedQuantity();
                if (foundInv.get().getCurrentStock()/newTargetStock < STOCK_LOW_THRESHOLD)
                    createAndSendStockAlert(inv, "Stock Low");

                updateTargetStock(inv);
            }
        } catch (Exception e) {
            log.error("Error while processing order item: {}", e.getMessage());
        }
    }

    private void updateTargetStock(InventoryUpdateDTO inv) {
        int rowsUpdated = inventoryRepository.updateTargetStock(inv.getProductId(), inv.getLocationId(),
                inv.getOrderedQuantity());

        log.info("Rows updated: {}", rowsUpdated);
        if (rowsUpdated < 1) {
            log.error("Error updating current stock");
        }
    }

    private void createAndSendStockAlert(InventoryUpdateDTO inv, String alertCategory) {
        try {
            StockAlertDTO stockAlert = new StockAlertDTO(inv.getProductId(), inv.getLocationId(), alertCategory);
            String stockAlertJson = objectMapper.writeValueAsString(stockAlert);
            messagingGateway.sendToPubSub(stockAlertJson, pubSubConfiguration.getAlertTopic());
        } catch (Exception e) {
            log.error("Error while creating stock alert: {}", e.getMessage());
        }
    }
}
