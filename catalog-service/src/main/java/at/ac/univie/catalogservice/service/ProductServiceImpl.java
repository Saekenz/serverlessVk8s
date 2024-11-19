package at.ac.univie.catalogservice.service;

import at.ac.univie.catalogservice.model.Product;
import at.ac.univie.catalogservice.model.ProductDTO;
import at.ac.univie.catalogservice.repository.CategoryRepository;
import at.ac.univie.catalogservice.repository.ProductRepository;
import at.ac.univie.catalogservice.util.ProductDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private Environment env;

    @Override
    public ResponseEntity<?> findAll() {
        List<ProductDTO> products = productRepository.findAll().stream()
                .map(Product::toDto)
                .toList();
        return ResponseEntity.ok(products);
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent()) {
            return ResponseEntity.ok(product.get().toDto());
        }
        else {
            return new ResponseEntity<>(String.format("Product with id %s could not be found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> save(Product product) {
        try {
            ProductDTO dto = productRepository.save(product).toDto();
            String newProductLocation = env.getProperty("prod.app.url") + dto.getId();

            return ResponseEntity.created(new URI(newProductLocation)).body(dto);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> update(Long id, ProductDTO productDTO) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent()) {
            product.get().setName(productDTO.getName());
            product.get().setDescription(productDTO.getDescription());
            product.get().setPrice(productDTO.getPrice());
            productRepository.save(product.get());

            String updatedProductLoc = env.getProperty("prod.app.url") + id;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Location", updatedProductLoc);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).headers(headers).build();
        }
        else {
            return new ResponseEntity<>(String.format("Product with id %s could not be found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Product getReferenceById(Long id) {
        return productRepository.getReferenceById(id);
    }

    @Override
    public ResponseEntity<?> generateProductData(int count) {
        if (count <= 0) return ResponseEntity.badRequest().body("Invalid value for count: " + count);

        ProductDataGenerator dataGenerator = new ProductDataGenerator();
        List<Long> categoryIds = categoryRepository.getCategories();

        if (categoryIds.isEmpty()) {
            return ResponseEntity.internalServerError().body("Products cannot be added if no categories have" +
                    " been created yet!");
        }

        List<Product> generatedProducts = dataGenerator.generateProducts(count, categoryIds);
        int numInsertedProducts = productRepository.saveAll(generatedProducts).size();

        if (numInsertedProducts == count) {
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
