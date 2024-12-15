package at.ac.univie.inventorymgmtservice.controller;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import at.ac.univie.inventorymgmtservice.outbound.OutboundConfiguration;
import at.ac.univie.inventorymgmtservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    @Autowired
    private IInventoryService inventoryService;

    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;
    private final PubSubConfiguration pubSubConfiguration;

    // for testing only
    @PostMapping("/sendOpt")
    public void sendOptMessage(@RequestBody String message) {
        log.info("Sending optimization request message {}", message);
        messagingGateway.sendToPubSub(message, pubSubConfiguration.getOptimizeTopic());
    }

    // for testing only
    @PostMapping("/sendAlert")
    public void sendAlertMessage(@RequestBody String message) {
        log.info("Sending alert message {}", message);
        messagingGateway.sendToPubSub(message, pubSubConfiguration.getAlertTopic());
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateInventories() {
        return inventoryService.generateInventoryData();
    }

    @PostMapping("/optimization/finished")
    public ResponseEntity<?> processOptimizationFinishedNotification(@RequestBody String payload) {
        return inventoryService.processOptimizationFinishedNotification(payload);
    }

    @PostMapping("/update-stock")
    public ResponseEntity<?> processStockUpdateMessage(@RequestBody String payload) {
        return inventoryService.processStockUpdateMessage(payload);
    }
}
