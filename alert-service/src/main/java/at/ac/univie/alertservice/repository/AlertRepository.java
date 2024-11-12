package at.ac.univie.alertservice.repository;

import at.ac.univie.alertservice.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
