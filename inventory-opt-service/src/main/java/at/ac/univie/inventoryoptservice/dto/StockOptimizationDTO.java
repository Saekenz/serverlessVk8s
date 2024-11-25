package at.ac.univie.inventoryoptservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockOptimizationDTO {
    private Long productId;
    private Long locationId;
    private int newCurrentStock;
}