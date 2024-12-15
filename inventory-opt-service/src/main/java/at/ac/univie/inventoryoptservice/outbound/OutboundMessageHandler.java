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
     * Creates a message with an empty body that is sent to the {@code inv-opt-finished} PubSub topic.
     *
     */
    public void createAndSendOptimizationFinishedMessage() {
        String messageContent = "optimization finished";
        messagingGateway.sendToPubSub(messageContent, pubSubConfiguration.getOptFinishedTopic());
        log.info("Sent optimization finished message!");
    }
}
