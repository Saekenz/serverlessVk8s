package at.ac.univie.alertservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlertDTO {
    private Long id;
    private Long productId;
    private Long locationId;
    private String category;
    private LocalDateTime createdAt;
}
