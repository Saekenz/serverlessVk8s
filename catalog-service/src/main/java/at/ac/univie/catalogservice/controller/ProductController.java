package at.ac.univie.catalogservice.controller;

import at.ac.univie.catalogservice.model.ProductDTO;
import at.ac.univie.catalogservice.service.IProductService;
import at.ac.univie.catalogservice.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @GetMapping
    public ResponseEntity<?> findAllProducts() {
        return productService.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        return productService.save(product);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findProductById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO product) {
        return productService.update(id, product);
    }
}
