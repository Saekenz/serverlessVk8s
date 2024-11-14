package at.ac.univie.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long locationId;
    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public OrderDTO toDto() {
        OrderDTO dto = new OrderDTO();

        dto.setId(this.id);
        dto.setCustomerId(this.customerId);
        dto.setLocationId(this.locationId);
        dto.setStatus(this.status);
        dto.setCreatedAt(this.createdAt.toString());
        dto.setUpdatedAt(this.updatedAt.toString());
        dto.setOrderDetails(Objects.requireNonNullElseGet(orderDetails, ArrayList::new));

        return dto;
    }
}
