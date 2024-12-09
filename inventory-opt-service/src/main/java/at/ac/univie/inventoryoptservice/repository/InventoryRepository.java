package at.ac.univie.inventoryoptservice.repository;

import at.ac.univie.inventoryoptservice.model.Inventory;
import at.ac.univie.inventoryoptservice.dto.InventoryAllocationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT new at.ac.univie.inventoryoptservice.dto.InventoryAllocationDTO(i.id, i.currentStock, " +
            "i.targetStock, i.productId, l.id, l.name, l.city, l.latitude, l.longitude) " +
            "FROM Inventory i " +
            "JOIN i.location l")
    List<InventoryAllocationDTO> fetchInventoryWithLocation();

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i " +
            "SET i.currentStock = :newCurrentStock, i.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE i.productId = :productId AND i.location.id = :locationId ")
    int updateCurrentStock(@Param("productId") Long productId,
                           @Param("locationId") Long locationId,
                           @Param("newCurrentStock") int newCurrentStock);
}
