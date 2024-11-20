package at.ac.univie.inventoryoptservice.optimization;

import at.ac.univie.inventoryoptservice.model.StockOptimizationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chromosome {
    private Long productId;
    private Long locationId;
    private int currentStock;
    private int targetStock;
    private double latitude;
    private double longitude;

    public StockOptimizationDTO toStockOptimizationDTO() {
        StockOptimizationDTO stockOptimizationDTO = new StockOptimizationDTO();
        stockOptimizationDTO.setProductId(productId);
        stockOptimizationDTO.setLocationId(locationId);
        stockOptimizationDTO.setNewCurrentStock(currentStock);
        return stockOptimizationDTO;
    }
}
