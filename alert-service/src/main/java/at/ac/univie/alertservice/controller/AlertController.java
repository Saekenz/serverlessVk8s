package at.ac.univie.alertservice.controller;

import at.ac.univie.alertservice.model.Alert;
import at.ac.univie.alertservice.service.IAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private IAlertService alertService;

    @GetMapping
    public ResponseEntity<?> findAllAlerts() {
        return alertService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findAlertById(@PathVariable Long id) {
        return alertService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody Alert alert) {
        return alertService.save(alert);
    }

    //TODO: add endpoint to get all alerts from date to date

    //TODO: return product name with alert -> retrieve product data from catalog service
}
