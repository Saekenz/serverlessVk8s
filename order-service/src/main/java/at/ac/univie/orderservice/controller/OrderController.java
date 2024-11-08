package at.ac.univie.orderservice.controller;

import at.ac.univie.orderservice.model.*;
import at.ac.univie.orderservice.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

//    @PostMapping
//    public ResponseEntity<?> createOrder(@RequestBody Order order) {
//        return orderService.save(order);
//    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderCreationDTO orderCreationDTO) {
        Order order = new Order();
        order.setCustomerId(orderCreationDTO.getCustomerId());
        order.setLocationId(orderCreationDTO.getLocationId());
        order.setStatus(orderCreationDTO.getStatus());

        List<OrderDetail> orderDetails = orderCreationDTO.getOrderDetails().stream()
                .map(orderDetail-> {
                    OrderDetail productInOrder = new OrderDetail();
                    OrderDetailKey key = new OrderDetailKey();
                    key.setProductId(orderDetail.getProductId());
                    productInOrder.setId(key);
                    productInOrder.setOrder(order);
                    productInOrder.setQuantity(orderDetail.getQuantity());
                    productInOrder.setUnitPrice(orderDetail.getUnitPrice());
                    return productInOrder;
                }).toList();

        order.setOrderDetails(orderDetails);

        // Save the order and cascade orderDetails
        return orderService.save(order);
    }
}
