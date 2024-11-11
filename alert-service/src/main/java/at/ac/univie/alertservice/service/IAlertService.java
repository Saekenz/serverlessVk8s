package at.ac.univie.alertservice.service;

import at.ac.univie.alertservice.model.Alert;
import org.springframework.http.ResponseEntity;

public interface IAlertService {

    ResponseEntity<?> findAll();

    ResponseEntity<?> findById(Long id);

    ResponseEntity<?> save(Alert alert);

    Alert getReferenceById(Long id);
}
