package at.ac.univie.inventoryoptservice.util;

import at.ac.univie.inventoryoptservice.dto.StockOptimizationDTO;
import at.ac.univie.inventoryoptservice.optimization.Chromosome;
import at.ac.univie.inventoryoptservice.optimization.DNA;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class OptimizationMessageBuilder {
    private ObjectMapper objectMapper;

    public OptimizationMessageBuilder() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Converts a {@link DNA} object into a {@link List} of {@link StockOptimizationDTO} objects.
     *
     * @param dna The {@link DNA} object that is to be converted.
     * @return A {@link List} of {@link StockOptimizationDTO} objects.
     */
    public List<StockOptimizationDTO> createStockOptimizationFromDNA(DNA dna) {
        return dna.getChromosomes().stream()
                .map(Chromosome::toStockOptimizationDTO)
                .toList();
    }

    /**
     * Takes a {@link StockOptimizationDTO} object and parses it into a {@link String} object.
     *
     * @param stockOptimizationDTO The {@link StockOptimizationDTO} object that is to be parsed.
     * @return {@link String} object containing stock optimization information.
     */
    private String buildStockOptimizationMessage (StockOptimizationDTO stockOptimizationDTO) {
        try {
            return objectMapper.writeValueAsString(stockOptimizationDTO);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing stock optimization: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Takes a {@link List} of {@link StockOptimizationDTO} objects and converts them into a {@link List} of
     * {@link String} objects.
     *
     * @param stockOptimizationDTOs The {@link List} of {@link StockOptimizationDTO} objects to be converted.
     * @return A {@link List} of {@link String} objects each representing a stock optimization message.
     */
    private List<String> buildStockOptimizationMessages (List<StockOptimizationDTO> stockOptimizationDTOs) {
        return stockOptimizationDTOs.stream()
                .map(this::buildStockOptimizationMessage)
                .toList();
    }

    /**
     * Takes a {@link DNA} object and converts it into messages that can be sent to a PubSub topic.
     *
     * @param bestDNA The {@link DNA} object that is to be converted.
     * @return A {@link List} of Strings that represent PubSub messages.
     */
    public List<String> createStockOptimizationMessagesFromDNA(DNA bestDNA) {
        // convert each Chromosome in DNA to StockOptimizationDTO
        List<StockOptimizationDTO> stockOptimizations = createStockOptimizationFromDNA(bestDNA);

        // build optimization messages based on list of StockOptimizationDTO
        return buildStockOptimizationMessages(stockOptimizations);
    }
}
