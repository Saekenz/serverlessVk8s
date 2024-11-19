package at.ac.univie.customerservice.service;

import at.ac.univie.customerservice.model.Customer;
import at.ac.univie.customerservice.model.CustomerDTO;
import org.springframework.http.ResponseEntity;

public interface ICustomerService {

    ResponseEntity<?> findAll();

    ResponseEntity<?> findById(Long id);

    ResponseEntity<?> save(Customer customer);

    ResponseEntity<?> update(Long id, CustomerDTO customerDTO);

    Customer getReferenceById(Long id);

    ResponseEntity<?> generateCustomerData(int count);
}
