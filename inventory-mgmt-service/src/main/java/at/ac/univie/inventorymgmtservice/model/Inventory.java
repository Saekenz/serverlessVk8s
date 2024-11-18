package at.ac.univie.inventorymgmtservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private int currentStock;
    private int targetStock;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
