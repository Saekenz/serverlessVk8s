package at.ac.univie.inventoryoptservice.outbound;

import at.ac.univie.inventoryoptservice.config.PubSubConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundMessageHandler {
    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;
    private final PubSubConfiguration pubSubConfiguration;

    /**
     * Sends stock optimization messages to a PubSub topic via a messaging gateway.
     *
     * @param stockOptimizationMessages A {@link List} of {@link String} objects representing stock optimization
     *                                  messages.
     */
    public void sendStockOptimizationMessages(List<String> stockOptimizationMessages) {
        if (!stockOptimizationMessages.contains("")) {
            int counter = 0;
            for (String stockOptimizationMessage : stockOptimizationMessages) {
                counter++;
                messagingGateway.sendToPubSub(stockOptimizationMessage, pubSubConfiguration.getStockUpdateTopic());
            }
            log.info("Stock optimization messages sent: {}", counter);
        }
    }

    /**
     * Creates a message with an empty body that is sent to the {@code inv-opt-finished} PubSub topic.
     *
     */
    public void createAndSendOptimizationFinishedMessage() {
        String messageContent = "";
        messagingGateway.sendToPubSub(messageContent, pubSubConfiguration.getOptFinishedTopic());
        log.info("Sent optimization finished message!");
    }
}
