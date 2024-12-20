package at.ac.univie.inventorymgmtservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "configuration")
public class ConfigEntry {

    @Id
    private String name;

    private String value;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
