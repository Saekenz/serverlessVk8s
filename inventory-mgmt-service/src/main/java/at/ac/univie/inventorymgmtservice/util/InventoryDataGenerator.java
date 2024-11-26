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
    private static final int MIN_TARGET_STOCK = 100;
    private static final int MAX_TARGET_STOCK = 500;
    private static final double MIN_CURRENT_STOCK_PERCENT = 0.65;

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

                // set target & current stock for the inventory (current stock is always at least 65% of target stock)
                int targetStock = random.nextInt(MIN_TARGET_STOCK, MAX_TARGET_STOCK + 1);
                int currentStock = random.nextInt((int) (targetStock * MIN_CURRENT_STOCK_PERCENT), targetStock + 1);

                inventory.setCurrentStock(currentStock);
                inventory.setTargetStock(targetStock);

                inventories.add(inventory);
            }
        }

        return inventories;
    }
}
