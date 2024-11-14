package at.ac.univie.orderservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OrderDetail {

    @EmbeddedId
    private OrderDetailKey id;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Column(nullable = false)
    private int quantity;

    private double unitPrice;

    public OrderDetailDTO toDto() {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setProductId(this.id.getProductId());
        dto.setLocationId(this.order.getLocationId());
        dto.setQuantity(this.quantity);
        return dto;
    }
}
