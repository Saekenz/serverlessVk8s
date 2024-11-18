package at.ac.univie.inventoryoptservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryAllocationDTO {
    private Long id;
    private int currentStock;
    private int targetStock;
    private Long productId;
    private Long locationId;
    private String locationName;
    private String locationCity;
    private Double latitude;
    private Double longitude;
}