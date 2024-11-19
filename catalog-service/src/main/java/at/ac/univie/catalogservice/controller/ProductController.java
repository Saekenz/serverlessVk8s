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

    /**
     * Endpoint to retrieve all products.
     *
     * @return Response message containing all product data.
     */
    @GetMapping
    public ResponseEntity<?> findAllProducts() {
        return productService.findAll();
    }

    /**
     * Endpoint create a new product.
     *
     * @param product Data object containing the product's information.
     * @return Response message containing the status of the creation.
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        return productService.save(product);
    }

    /**
     * Endpoint to retrieve a product by its ID.
     *
     * @param id Unique identifier associated with the product.
     * @return Response message with the product data.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findProductById(@PathVariable Long id) {
        return productService.findById(id);
    }

    /**
     * Endpoint to update a product's information.
     *
     * @param id Unique identifier associated with the product.
     * @param product Data object containing the updated information.
     * @return Response message containing the status of the update.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO product) {
        return productService.update(id, product);
    }

    /**
     * Endpoint to generate test data.
     *
     * @param count Number of products to generate.
     * @return Response message with generation status.
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateProducts(@RequestParam int count) {
        return productService.generateProductData(count);
    }
}
