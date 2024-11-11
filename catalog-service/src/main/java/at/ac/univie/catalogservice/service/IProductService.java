package at.ac.univie.catalogservice.service;

import at.ac.univie.catalogservice.model.Product;
import at.ac.univie.catalogservice.model.ProductDTO;
import org.springframework.http.ResponseEntity;

public interface IProductService {

    ResponseEntity<?> findAll();

    ResponseEntity<?> findById(Long id);

    ResponseEntity<?> save(Product product);

    ResponseEntity<?> update(Long id, ProductDTO productDTO);

    Product getReferenceById(Long id);
}
