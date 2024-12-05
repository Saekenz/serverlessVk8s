package at.ac.univie.alertservice.service;

import at.ac.univie.alertservice.model.Alert;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public interface IAlertService {

    ResponseEntity<?> findAll();

    ResponseEntity<?> findByDateRange(LocalDateTime from, LocalDateTime to);

    ResponseEntity<?> findById(Long id);

    ResponseEntity<?> save(Alert alert);

    ResponseEntity<?> saveAlert(String payload, HttpServletRequest request);

    void save(String alertMsg);

    ResponseEntity<?> delete(Long id);

    Alert getReferenceById(Long id);
}
