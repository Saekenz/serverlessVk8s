package at.ac.univie.inventorymgmtservice.repository;

import at.ac.univie.inventorymgmtservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
}
