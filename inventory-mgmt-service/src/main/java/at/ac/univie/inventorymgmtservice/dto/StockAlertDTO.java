package at.ac.univie.inventorymgmtservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockAlertDTO {
    private Long productId;
    private Long locationId;
    private String category;
}
