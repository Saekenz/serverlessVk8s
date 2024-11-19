package at.ac.univie.customerservice.util;

import at.ac.univie.customerservice.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CustomerDataGenerator {
    private final Faker faker;

    public CustomerDataGenerator() {
        this.faker = new Faker();
    }

    public List<Customer> generateCustomers(int count) {
        List<Customer> generatedCustomers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Customer customer = new Customer();
            customer.setEmail(faker.internet().emailAddress());
            customer.setUsername(faker.internet().username());
            customer.setPassword(faker.internet().password());
            customer.setName(faker.name().fullName());
            generatedCustomers.add(customer);
        }

        return generatedCustomers;
    }
}
