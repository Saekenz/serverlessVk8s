package at.ac.univie.alertservice.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertDTO {
    private Long id;
    private Long productId;
    private Long locationId;
    private String category;
    private LocalDateTime createdAt;
}
