package at.ac.univie.inventorymgmtservice.inbound;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class InboundConfiguration {
    private final PubSubConfiguration pubSubConfiguration;

    @Bean
    public PubSubInboundChannelAdapter pubSubInboundChannelAdapter(
            @Qualifier("pubsubInputChannel") MessageChannel inboundChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter inboundAdapter = new PubSubInboundChannelAdapter(pubSubTemplate,
                pubSubConfiguration.getSubscription());
        inboundAdapter.setOutputChannel(inboundChannel);
        inboundAdapter.setAckMode(AckMode.MANUAL);
        return inboundAdapter;
    }

    @Bean
    public MessageChannel pubsubInputChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "pubsubInputChannel")
    public MessageHandler messageReceiver() {
        return message -> {
            log.info("Received message: {}", new String((byte[]) message.getPayload()));
            BasicAcknowledgeablePubsubMessage originalMessage = message.getHeaders().get(
                    GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
            originalMessage.ack();
        };
    }
}
