package at.ac.univie.inventoryoptservice.optimization;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LocationPair {
    private Long fromLocationId;
    private Long toLocationId;


    /**
     * Equals implemented in a way that makes it so {@link LocationPair} objects featuring the same pair of locationIds
     * e.g. (1,4) & (4,1) are treated as the same object for distance matrix lookups later on.
     *
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationPair that)) return false;
        return (Objects.equals(fromLocationId, that.fromLocationId) && Objects.equals(toLocationId, that.toLocationId))
                || (Objects.equals(fromLocationId, that.toLocationId) && Objects.equals(toLocationId, that.fromLocationId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(Math.min(fromLocationId, toLocationId), Math.max(fromLocationId, toLocationId));
    }
}