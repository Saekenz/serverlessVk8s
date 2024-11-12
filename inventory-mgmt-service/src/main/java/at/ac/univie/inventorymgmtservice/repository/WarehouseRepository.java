package at.ac.univie.inventorymgmtservice.repository;

import at.ac.univie.inventorymgmtservice.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
