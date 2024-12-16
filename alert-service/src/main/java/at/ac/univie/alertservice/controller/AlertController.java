package at.ac.univie.alertservice.controller;

import at.ac.univie.alertservice.model.Alert;
import at.ac.univie.alertservice.service.IAlertService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@CrossOrigin("*")
@RestController
public class AlertController {

    @Autowired
    private IAlertService alertService;

    @GetMapping({"alerts/{id}", "/{id}"})
    public ResponseEntity<?> findAlertById(@PathVariable Long id) {
        return alertService.findById(id);
    }

    @PostMapping()
    public ResponseEntity<?> createAlert(@RequestBody String payload, HttpServletRequest request) {
        return alertService.saveAlert(payload, request);
    }

    @DeleteMapping({"alerts/{id}", "/{id}"})
    public ResponseEntity<?> deleteAlert(@PathVariable Long id) {
        return alertService.delete(id);
    }

    @GetMapping
    public ResponseEntity<?> findAlertsByDateRange(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return alertService.findByDateRange(from, to);
    }
}
