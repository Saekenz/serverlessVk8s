package at.ac.univie.orderservice.service;

import at.ac.univie.orderservice.model.Order;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IOrderService {

    ResponseEntity<?> findAll();

    ResponseEntity<?> findById(Long id);

    ResponseEntity<?> save(Order order);

    Order getReferenceById(Long id);
}
