package at.ac.univie.orderservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreationDTO {

    private Long customerId;
    private Long locationId;
    private String status;
    private List<OrderDetailCreationDTO> orderDetails;

    @Getter
    @Setter
    public static class OrderDetailCreationDTO {
        private Long productId;
        private int quantity;
        private double unitPrice;
    }
}
