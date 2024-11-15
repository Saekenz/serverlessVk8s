package at.ac.univie.inventorymgmtservice.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inv.mgmt.pubsub")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PubSubConfiguration {
    private String alertTopic;
    private String optimizeTopic;
    private String subscription;
}
