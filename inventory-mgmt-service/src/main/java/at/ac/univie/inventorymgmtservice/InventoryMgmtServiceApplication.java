package at.ac.univie.inventorymgmtservice;

import at.ac.univie.inventorymgmtservice.config.PubSubConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(PubSubConfiguration.class)
public class InventoryMgmtServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryMgmtServiceApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
