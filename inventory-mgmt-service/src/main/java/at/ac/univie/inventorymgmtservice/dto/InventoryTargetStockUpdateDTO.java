package at.ac.univie.inventorymgmtservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTargetStockUpdateDTO {
    private Long productId;
    private Long locationId;
    @JsonAlias({"quantity"})
    private int orderedQuantity;
}
