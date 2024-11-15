package at.ac.univie.inventorymgmtservice.repository;

import at.ac.univie.inventorymgmtservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i " +
            "SET i.targetStock = i.targetStock + :orderedQuantity, i.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE i.productId = :productId AND i.warehouse.location.id = :locationId ")
    int increaseTargetStock(@Param("productId") Long productId,
                            @Param("locationId") Long locationId,
                            @Param("orderedQuantity") int orderedQuantity);

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i " +
            "SET i.currentStock = :newCurrentStock, i.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE i.productId = :productId AND i.warehouse.location.id = :locationId ")
    int updateCurrentStock(@Param("productId") Long productId,
                           @Param("locationId") Long locationId,
                           @Param("newCurrentStock") int newCurrentStock);

    @Query("SELECT i FROM Inventory  i " +
            "WHERE i.productId = :productId AND i.warehouse.location.id = :locationId")
    Optional<Inventory> findByProductIdAndLocationId(Long productId, Long locationId);
}
