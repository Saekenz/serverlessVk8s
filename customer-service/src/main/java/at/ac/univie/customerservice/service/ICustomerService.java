package at.ac.univie.customerservice.service;

import at.ac.univie.customerservice.model.Customer;

import java.util.List;

public interface ICustomerService {

    List<Customer> findAll();

    Customer findById(Long id);

    Customer save(Customer customer);

    Customer getReferenceById(Long id);
}
