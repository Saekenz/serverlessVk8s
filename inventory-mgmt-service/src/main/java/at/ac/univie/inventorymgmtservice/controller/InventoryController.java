package at.ac.univie.inventorymgmtservice.controller;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import at.ac.univie.inventorymgmtservice.outbound.OutboundConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;
    private final PubSubConfiguration pubSubConfiguration;

    @PostMapping("/sendOpt")
    public void sendOptMessage(@RequestBody String message) {
        log.info("Sending message to outbound channel {}", message);
        messagingGateway.sendToPubSub(message, pubSubConfiguration.getOptimizeTopic());
    }

    @PostMapping("/sendAlert")
    public void sendAlertMessage(@RequestBody String message) {
        log.info("Sending message to outbound channel {}", message);
        messagingGateway.sendToPubSub(message, pubSubConfiguration.getAlertTopic());
    }
}
