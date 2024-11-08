package at.ac.univie.customerservice.controller;

import at.ac.univie.customerservice.model.Customer;
import at.ac.univie.customerservice.service.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @GetMapping
    public List<Customer> findAllCustomers() {
        return customerService.findAll();
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

    @GetMapping("/{id}")
    public Customer findCustomerById(@PathVariable Long id) {
        return customerService.findById(id);
    }
}
