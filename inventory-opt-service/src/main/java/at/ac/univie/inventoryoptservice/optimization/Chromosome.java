package at.ac.univie.inventoryoptservice.optimization;

import lombok.Data;

@Data
public class Chromosome {
    private Long productId;
    private Long locationId;
    private int currentStock;
    private int targetStock;
    private double latitude;
    private double longitude;
}
