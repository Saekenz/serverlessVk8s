package at.ac.univie.customerservice.service;

import at.ac.univie.customerservice.model.Customer;
import at.ac.univie.customerservice.model.CustomerDTO;
import at.ac.univie.customerservice.repository.CustomerRepository;
import at.ac.univie.customerservice.util.CustomerDataGenerator;
import org.modelmapper.ModelMapper;
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
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private Environment env;

    @Override
    public ResponseEntity<?> findAll() {
        List<CustomerDTO> customers = customerRepository.findAll().stream()
                .map(customer -> modelMapper.map(customer, CustomerDTO.class))
                .toList();
        return ResponseEntity.ok(customers);
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);

        if (customer.isPresent()) {
            return ResponseEntity.ok(modelMapper.map(customer.get(), CustomerDTO.class));
        }
        else {
            return new ResponseEntity<>(String.format("Customer with id %s could not be found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> save(Customer customer) {
        try {
            CustomerDTO customerDTO = modelMapper.map(customerRepository.save(customer), CustomerDTO.class);
            String newCustomerLocation = env.getProperty("app.url") + customerDTO.getId();

            return ResponseEntity.created(new URI(newCustomerLocation)).body(customerDTO);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> update(Long id, CustomerDTO customerDTO) {
        Optional<Customer> customer = customerRepository.findById(id);

        if (customer.isPresent()) {
            customer.get().setName(customerDTO.getName());
            customer.get().setUsername(customerDTO.getUsername());
            customer.get().setEmail(customerDTO.getEmail());
            customerRepository.save(customer.get());

            String updatedCustomerLocation = env.getProperty("app.url") + id;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Location", updatedCustomerLocation);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).headers(headers).build();
        }
        else {
            return new ResponseEntity<>(String.format("Customer with id %s could not be found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Customer getReferenceById(Long id) {
        return customerRepository.getReferenceById(id);
    }

    @Override
    public ResponseEntity<?> generateCustomerData(int count) {
        if (count <= 0) return ResponseEntity.badRequest().body("Invalid value for count: " + count);

        CustomerDataGenerator dataGenerator = new CustomerDataGenerator();
        List<Customer> generatedCustomers = dataGenerator.generateCustomers(count);
        int numInsertedCustomers = customerRepository.saveAll(generatedCustomers).size();

        if (numInsertedCustomers == count) {
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
