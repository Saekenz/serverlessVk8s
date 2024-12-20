package at.ac.univie.inventorymgmtservice.repository;

import at.ac.univie.inventorymgmtservice.model.ConfigEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ConfigurationRepository extends JpaRepository<ConfigEntry, String> {

    ConfigEntry findByNameEquals(String name);

    @Modifying
    @Transactional
    @Query("UPDATE ConfigEntry c " +
            "SET c.value = :value, c.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE c.name = :name")
    void updateConfigEntryValueByName(@Param("name") String name,
                                      @Param("value") String value);
}
