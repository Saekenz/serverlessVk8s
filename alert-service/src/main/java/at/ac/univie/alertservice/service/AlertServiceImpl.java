package at.ac.univie.alertservice.service;

import at.ac.univie.alertservice.model.Alert;
import at.ac.univie.alertservice.model.AlertDTO;
import at.ac.univie.alertservice.repository.AlertRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class AlertServiceImpl implements IAlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ModelMapper modelMapper;

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
            String newCustomerLocation = env.getProperty("app.url") + dto.getId();

            return ResponseEntity.created(new URI(newCustomerLocation)).body(dto);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Alert getReferenceById(Long id) {
        return alertRepository.getReferenceById(id);
    }
}
