package at.ac.univie.customerservice.controller;

import at.ac.univie.customerservice.model.Customer;
import at.ac.univie.customerservice.model.CustomerDTO;
import at.ac.univie.customerservice.service.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    /**
     * Endpoint to retrieve all customers.
     *
     * @return Response message containing all customer data.
     */
    @GetMapping
    public ResponseEntity<?> findAllCustomers() {
        return customerService.findAll();
    }

    /**
     * Endpoint create a new customer.
     *
     * @param customer Data object containing the customer's information.
     * @return Response message containing the status of the creation.
     */
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

    /**
     * Endpoint to update a customer's profile data.
     *
     * @param id Unique identifier associated with the customer.
     * @param customerDTO Data object containing the updated information.
     * @return Response message containing the status of the update.
     */
    @PutMapping({"customers/{id}", "/{id}"})
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        return customerService.update(id, customerDTO);
    }

    /**
     * Endpoint to retrieve a customer by their ID.
     *
     * @param id Unique identifier associated with the customer.
     * @return Response message with the customer data.
     */
    @GetMapping({"customers/{id}", "/{id}"})
    public ResponseEntity<?> findCustomerById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    /**
     * Endpoint to generate test data.
     *
     * @param count Number of customers to generate.
     * @return Response message with generation status.
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateCustomers(@RequestParam int count) {
        return customerService.generateCustomerData(count);
    }
}
