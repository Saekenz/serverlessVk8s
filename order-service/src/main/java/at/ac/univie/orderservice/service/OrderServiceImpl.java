package at.ac.univie.orderservice.service;

import at.ac.univie.orderservice.model.*;
import at.ac.univie.orderservice.outbound.OutboundConfiguration;
import at.ac.univie.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService{
    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @Override
    public ResponseEntity<?> findAll() {
        List<OrderDTO> orders = orderRepository.findAll().stream()
                .map(Order::toDto)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {
            OrderDTO orderDTO = order.get().toDto();
            return ResponseEntity.ok(orderDTO);
        }
        else {
            return new ResponseEntity<>(String.format("Order with id %s could not be found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> save(OrderCreationDTO orderDTO) {
        Order order = getOrderFromDTO(orderDTO);

        try {
            Order savedOrder = orderRepository.save(order);
            String savedOrderLoc = env.getProperty("app.url") + savedOrder.getId();

            for (OrderDetail orderDetail : savedOrder.getOrderDetails()) {
                createAndSendStockUpdate(orderDetail);
            }

            return ResponseEntity.created(new URI(savedOrderLoc)).body(savedOrder.toDto());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void createAndSendStockUpdate(OrderDetail orderDetail) {
        if (orderDetail != null) {
            try {
                OrderDetailDTO dto = orderDetail.toDto();
                String stockUpdateJson = objectMapper.writeValueAsString(dto);
                messagingGateway.sendToPubSub(stockUpdateJson);
            } catch (Exception e) {
                log.error("Error while creating stock update: {}", e.getMessage());
            }
        }
    }

    private Order getOrderFromDTO(OrderCreationDTO orderDTO) {
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

        return order;
    }

    @Override
    public Order getReferenceById(Long id) {
        return orderRepository.getReferenceById(id);
    }
}
