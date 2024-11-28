package at.ac.univie.alertservice.service;

import at.ac.univie.alertservice.model.Alert;
import at.ac.univie.alertservice.model.AlertDTO;
import at.ac.univie.alertservice.repository.AlertRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AlertServiceImpl implements IAlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @Override
    public ResponseEntity<?> findAll() {
        List<AlertDTO> alerts = alertRepository.findAll().stream()
                .map(alert -> modelMapper.map(alert, AlertDTO.class))
                .toList();
        return ResponseEntity.ok(alerts);
    }

    @Override
    public ResponseEntity<?> findByDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            List<AlertDTO> alerts = alertRepository.findByCreatedAtBetween(from, to).stream()
                    .map(alert -> modelMapper.map(alert, AlertDTO.class))
                    .toList();
            return ResponseEntity.ok(alerts);
        }
        return findAll();
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        Optional<Alert> alert = alertRepository.findById(id);

        if (alert.isPresent()) {
            return ResponseEntity.ok(modelMapper.map(alert.get(), AlertDTO.class));
        }
        else {
            return new ResponseEntity<>(String.format("Alert with id %s could not be found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> save(Alert alert) {
        try {
            AlertDTO dto = modelMapper.map(alertRepository.save(alert), AlertDTO.class);
            String createdAlertLocation = env.getProperty("app.url") + dto.getId();

            return ResponseEntity.created(new URI(createdAlertLocation)).body(dto);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void save(String alertMsg) {
        try {
            Alert alert = objectMapper.readValue(alertMsg, Alert.class);
            alertRepository.save(alert);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> delete(Long id) {
        Optional<Alert> alert = alertRepository.findById(id);

        if (alert.isPresent()) {
            alertRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        else {
            return new ResponseEntity<>(String.format("Alert with id %s was not found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Alert getReferenceById(Long id) {
        return alertRepository.getReferenceById(id);
    }
}
