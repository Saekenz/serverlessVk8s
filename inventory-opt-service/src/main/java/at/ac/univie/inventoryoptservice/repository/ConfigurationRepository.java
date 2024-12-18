package at.ac.univie.inventoryoptservice.repository;

import at.ac.univie.inventoryoptservice.model.ConfigEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ConfigurationRepository extends JpaRepository<ConfigEntry, String> {

    /**
     * Fetches all configuration entries that belong to a specific service.
     * @param name The name of the service for which config entries are to be fetched
     * @return  A {@link List} object containing the fitting config entries.
     */
    List<ConfigEntry> findByNameStartingWith(String name);

    /**
     * Updates the value field of a configuration table entry using the name parameter.
     * @param name The name of the configuration entry that is to be updated.
     * @param value The new value of the configuration entry.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ConfigEntry c " +
            "SET c.value = :value, c.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE c.name = :name")
    void updateConfigEntryValueByName(@Param("name") String name,
                                      @Param("value") String value);
}
