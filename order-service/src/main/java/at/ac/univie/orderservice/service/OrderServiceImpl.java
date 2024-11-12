package at.ac.univie.orderservice.service;

import at.ac.univie.orderservice.model.*;
import at.ac.univie.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements IOrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Override
    public ResponseEntity<?> findAll() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {
            Customer customer = restTemplate.getForObject("http://localhost:8082/customers/" +
                    order.get().getCustomerId(), Customer.class);
            List<OrderDetail> orderDetails = new ArrayList<>();

            if (order.get().getOrderDetails() != null) {
                orderDetails = order.get().getOrderDetails();
            }

            OrderDTO orderDTO = new OrderDTO(
                    order.get().getId(),
                    customer,
                    order.get().getLocationId(),
                    order.get().getStatus(),
                    order.get().getCreatedAt(),
                    order.get().getUpdatedAt(),
                    orderDetails
            );

            return ResponseEntity.ok(orderDTO);
        }
        else {
            return new ResponseEntity<>(String.format("Order with id %s could not be found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> save(OrderCreationDTO orderDTO) {
        Order order = new Order();
        order.setCustomerId(orderDTO.getCustomerId());
        order.setLocationId(orderDTO.getLocationId());
        order.setStatus(orderDTO.getStatus());

        List<OrderDetail> orderDetails = orderDTO.getOrderDetails().stream()
                .map(orderDetail -> {
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

        try {
            Order savedOrder = orderRepository.save(order);
            String savedOrderLoc = env.getProperty("app.url") + savedOrder.getId();

            return ResponseEntity.created(new URI(savedOrderLoc)).body(savedOrder);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Order getReferenceById(Long id) {
        return orderRepository.getReferenceById(id);
    }
}
