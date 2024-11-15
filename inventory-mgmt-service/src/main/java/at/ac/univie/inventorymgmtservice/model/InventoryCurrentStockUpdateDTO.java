package at.ac.univie.inventorymgmtservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryCurrentStockUpdateDTO {
    private Long productId;
    private Long locationId;
    private int newCurrentStock;
}
