package at.ac.univie.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private Customer customer;
    private Long locationId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
