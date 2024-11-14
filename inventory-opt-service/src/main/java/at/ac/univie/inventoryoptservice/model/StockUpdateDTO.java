package at.ac.univie.inventoryoptservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockUpdateDTO {
    private Long productId;
    private Long locationId;
    private int newCurrentStock;
}