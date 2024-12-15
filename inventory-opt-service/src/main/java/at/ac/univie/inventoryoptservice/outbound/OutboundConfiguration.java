package at.ac.univie.inventoryoptservice.outbound;

import at.ac.univie.inventoryoptservice.config.PubSubConfiguration;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OutboundConfiguration {
    private final PubSubConfiguration pubSubConfiguration;

    @Bean
    @ServiceActivator(inputChannel = "pubsubOutputChannel")
    public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
        PubSubMessageHandler adapter = new PubSubMessageHandler(pubsubTemplate, pubSubConfiguration.getOptFinishedTopic());

        adapter.setSuccessCallback(
                ((ackId, message) ->
                        log.debug("Message was sent to {}!", pubSubConfiguration.getOptFinishedTopic())));

        adapter.setFailureCallback(
                (cause, message) -> log.warn("Error sending {} due to {}", message, cause));

        return adapter;
    }

    @MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
    public interface PubSubOutboundGateway {
        void sendToPubSub(String message, @Header(GcpPubSubHeaders.TOPIC) String topic);
    }
}
