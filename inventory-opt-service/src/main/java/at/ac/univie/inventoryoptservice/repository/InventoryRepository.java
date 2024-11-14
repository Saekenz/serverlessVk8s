package at.ac.univie.inventoryoptservice.repository;

import at.ac.univie.inventoryoptservice.model.Inventory;
import at.ac.univie.inventoryoptservice.model.InventoryAllocationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT new at.ac.univie.inventoryoptservice.model.InventoryAllocationDTO(i.id, i.currentStock, i.targetStock, i.productId, i.warehouse.id, w.name, l.id,l.latitude, l.longitude) " +
            "FROM Inventory i " +
            "JOIN i.warehouse w " +
            "JOIN w.location l")
    List<InventoryAllocationDTO> fetchInventoryWithWarehouseAndLocation();
}
