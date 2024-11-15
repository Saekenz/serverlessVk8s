package at.ac.univie.inventorymgmtservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetStockUpdateDTO {
    private Long productId;
    private Long locationId;
    @JsonAlias({"quantity"})
    private int orderedQuantity;
}
