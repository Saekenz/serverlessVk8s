package at.ac.univie.catalogservice.util;

import at.ac.univie.catalogservice.model.Category;
import at.ac.univie.catalogservice.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductDataGenerator {
    private final Faker faker;

    public ProductDataGenerator() {
        faker = new Faker();
    }

    public List<Product> generateProducts(int count, List<Long> categoryIds) {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Product product = new Product();
            Category category = new Category();
            category.setId(categoryIds.get(faker.number().numberBetween(0,categoryIds.size())));

            product.setCategory(category);
            product.setName(faker.commerce().productName());
            product.setPrice(Double.parseDouble(faker.commerce().price()));
            product.setDescription(faker.lorem().sentence(5));

            products.add(product);
        }

        return products;
    }
}
