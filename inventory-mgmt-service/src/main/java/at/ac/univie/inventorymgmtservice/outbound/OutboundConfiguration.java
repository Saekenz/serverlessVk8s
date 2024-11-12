package at.ac.univie.inventorymgmtservice.outbound;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;

@Configuration
@RequiredArgsConstructor
public class OutboundConfiguration {
    private final PubSubConfiguration pubSubConfiguration;

    @Bean
    @ServiceActivator(inputChannel = "pubsubOutputChannel")
    public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
        return new PubSubMessageHandler(pubsubTemplate, pubSubConfiguration.getTopic());
    }

    @MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
    public interface PubSubOutboundGateway {
        void sendToPubSub(String message);
    }
}
