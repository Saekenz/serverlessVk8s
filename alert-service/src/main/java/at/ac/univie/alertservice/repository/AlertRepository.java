package at.ac.univie.alertservice.repository;

import at.ac.univie.alertservice.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
