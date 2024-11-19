package at.ac.univie.inventorymgmtservice.util;

import at.ac.univie.inventorymgmtservice.model.Inventory;
import at.ac.univie.inventorymgmtservice.model.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
@AllArgsConstructor
public class InventoryDataGenerator {
    private static final int MIN_CURRENT_STOCK = 1;
    private static final int MAX_CURRENT_STOCK = 300;
    private static final int MIN_TARGET_STOCK = 10;
    private static final int MAX_TARGET_STOCK = 500;

    private final Random random;

    public InventoryDataGenerator() {
        random = new Random();
    }

    public List<Inventory> generateInventories(List<Long> locationIds, List<Long> productIds) {
        List<Inventory> inventories = new ArrayList<>();

        for (Long productId : productIds) {
            for (Long locationId : locationIds) {
                Inventory inventory = new Inventory();
                Location location = new Location();

                location.setId(locationId);
                inventory.setLocation(location);

                inventory.setProductId(productId);
                inventory.setCurrentStock(random.nextInt(MIN_CURRENT_STOCK, MAX_CURRENT_STOCK + 1));
                inventory.setTargetStock(random.nextInt(MIN_TARGET_STOCK, MAX_TARGET_STOCK + 1));

                inventories.add(inventory);
            }
        }

        return inventories;
    }
}
