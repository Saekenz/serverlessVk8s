package at.ac.univie.catalogservice.service;

import at.ac.univie.catalogservice.model.Product;

import java.util.List;

public interface IProductService {

    List<Product> findAll();

    Product findById(Long id);

    Product save(Product product);

    Product getReferenceById(Long id);
}
