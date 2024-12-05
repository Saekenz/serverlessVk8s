package at.ac.univie.alertservice;

import at.ac.univie.alertservice.config.PubSubConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(PubSubConfiguration.class)
@Slf4j
public class AlertServiceApplication {

	public static void main(String[] args) {
		String port = System.getenv("PORT");
		log.info("Container listening to port {}", port);

		SpringApplication.run(AlertServiceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper().findAndRegisterModules();
	}

}
