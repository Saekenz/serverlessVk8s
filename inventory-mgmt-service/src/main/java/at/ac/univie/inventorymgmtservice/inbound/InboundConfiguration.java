package at.ac.univie.inventorymgmtservice.inbound;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import at.ac.univie.inventorymgmtservice.service.IInventoryService;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class InboundConfiguration {
    private final PubSubConfiguration pubSubConfiguration;

    @Autowired
    private IInventoryService inventoryService;

    @Bean
    public MessageChannel pubsubInputChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter targetStockUpdateAdapter(
            @Qualifier("pubsubInputChannel") MessageChannel inboundChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter inboundAdapter = new PubSubInboundChannelAdapter(pubSubTemplate,
                pubSubConfiguration.getTargetStockSub());
        inboundAdapter.setOutputChannel(inboundChannel);
        inboundAdapter.setAckMode(AckMode.MANUAL);
        inboundAdapter.setPayloadType(String.class);
        return inboundAdapter;
    }

//    @Bean
//    public PubSubInboundChannelAdapter currentStockUpdateAdapter(
//            @Qualifier("pubsubInputChannel") MessageChannel inboundChannel,
//            PubSubTemplate pubSubTemplate) {
//        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate,
//                pubSubConfiguration.getCurrentStockSub());
//        adapter.setOutputChannel(inboundChannel);
//        adapter.setAckMode(AckMode.MANUAL);
//        adapter.setPayloadType(String.class);
//        return adapter;
//
//    }

    @ServiceActivator(inputChannel = "pubsubInputChannel")
    public void messageReceiver(
            String payload, @Header (GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
        log.info("Message arrived! Payload: {}", payload);
        inventoryService.handleIncomingOrderItem(payload);
        message.ack();
    }
}
