package at.ac.univie.catalogservice.repository;

import at.ac.univie.catalogservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
