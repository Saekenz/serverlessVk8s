package at.ac.univie.catalogservice.service;

import at.ac.univie.catalogservice.model.Category;
import org.springframework.http.ResponseEntity;

public interface ICategoryService {

    ResponseEntity<?> findAll();

    ResponseEntity<?> findById(Long id);

    ResponseEntity<?> save(Category category);

    Category getReferenceById(Long id);
}
