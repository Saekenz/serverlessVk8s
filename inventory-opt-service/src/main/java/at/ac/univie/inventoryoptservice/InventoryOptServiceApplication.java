package at.ac.univie.inventoryoptservice;

import at.ac.univie.inventoryoptservice.config.PubSubConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(PubSubConfiguration.class)
@EnableAsync
public class InventoryOptServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryOptServiceApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
