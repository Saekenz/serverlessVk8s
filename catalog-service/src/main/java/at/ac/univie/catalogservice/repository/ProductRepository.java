package at.ac.univie.catalogservice.repository;

import at.ac.univie.catalogservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
