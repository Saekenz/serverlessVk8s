package at.ac.univie.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailKey implements Serializable {

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "productId")
    private Long productId;
}
