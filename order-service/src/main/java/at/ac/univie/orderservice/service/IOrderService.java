package at.ac.univie.orderservice.service;

import at.ac.univie.orderservice.model.Order;
import at.ac.univie.orderservice.model.OrderCreationDTO;
import org.springframework.http.ResponseEntity;

public interface IOrderService {

    ResponseEntity<?> findAll();

    ResponseEntity<?> findById(Long id);

    ResponseEntity<?> save(OrderCreationDTO orderDTO);

    Order getReferenceById(Long id);
}
