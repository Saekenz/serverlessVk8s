package at.ac.univie.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private Long customerId;
    private Long locationId;
    private String status;
    private String createdAt;
    private String updatedAt;
    private List<OrderDetail> orderDetails;
}
