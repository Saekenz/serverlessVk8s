package at.ac.univie.orderservice.controller;

import at.ac.univie.orderservice.model.*;
import at.ac.univie.orderservice.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @GetMapping
    public ResponseEntity<?> findAllOrders() {
       return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findOrderById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderCreationDTO orderCreationDTO) {
        // Save the order and cascade orderDetails
        return orderService.save(orderCreationDTO);
    }
}
