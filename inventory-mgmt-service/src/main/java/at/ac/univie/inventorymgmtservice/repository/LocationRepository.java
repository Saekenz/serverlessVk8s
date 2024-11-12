package at.ac.univie.inventorymgmtservice.repository;

import at.ac.univie.inventorymgmtservice.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
