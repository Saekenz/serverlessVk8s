package at.ac.univie.inventoryoptservice.dto;

import at.ac.univie.inventoryoptservice.optimization.Chromosome;
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

    public Chromosome toChromosome() {
        Chromosome chromosome = new Chromosome();
        chromosome.setId(id);
        chromosome.setProductId(this.productId);
        chromosome.setLocationId(this.locationId);
        chromosome.setCurrentStock(this.currentStock);
        chromosome.setTargetStock(this.targetStock);
        chromosome.setLatitude(this.latitude);
        chromosome.setLongitude(this.longitude);
        return chromosome;
    }
}