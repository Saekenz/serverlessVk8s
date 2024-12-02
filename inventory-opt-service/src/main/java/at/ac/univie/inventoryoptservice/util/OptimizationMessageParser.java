package at.ac.univie.inventoryoptservice.util;

import at.ac.univie.inventoryoptservice.config.OptimizationConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Slf4j
@Component
@RequiredArgsConstructor
public class OptimizationMessageParser {
    private final ObjectMapper objectMapper;

    /**
     * Parses a message that adheres to the schema of {@link OptimizationConfig}.
     *
     * @param message The message that is to be parsed.
     * @return A {@link OptimizationConfig} object containing the information found in the message. Returns a
     * {@link OptimizationConfig} object with default values if an error occurs during parsing.
     */
    public OptimizationConfig parseOptimizationMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            if (hasSchema(jsonNode)) {
                return objectMapper.treeToValue(jsonNode, OptimizationConfig.class);
            }
            else {
                log.warn("Incoming optimization message is missing parameters!");
                return null;
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private boolean hasSchema(JsonNode jsonNode) {
        return jsonNode.has("mutationRate") && jsonNode.has("populationSize")
                && jsonNode.has("generations");
    }
}
