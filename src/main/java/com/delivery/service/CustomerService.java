package com.delivery.service;

import com.delivery.entity.Customer;
import com.delivery.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@Transactional
public class CustomerService {

    private static final Logger logger = Logger.getLogger(CustomerService.class.getName());

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        logger.info("Fetching all customers");
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        logger.info("Fetching customer with id: " + id);
        return customerRepository.findById(id);
    }

    public Customer createCustomer(Customer customer) {
        logger.info("Creating new customer: " + customer.getName());

        customer.validate();
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer customerDetails) {
        logger.info("Updating customer with id: " + id);

        customerDetails.validate();

        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setName(customerDetails.getName());
            customer.setAddress(customerDetails.getAddress());
            customer.setLatitude(customerDetails.getLatitude());
            customer.setLongitude(customerDetails.getLongitude());
            customer.setPreferredTimeSlot(customerDetails.getPreferredTimeSlot());
            return customerRepository.save(customer);
        }
        throw new RuntimeException("Customer not found with id: " + id);
    }

    public void deleteCustomer(Long id) {
        logger.info("Deleting customer with id: " + id);
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
        } else {
            throw new RuntimeException("Customer not found with id: " + id);
        }
    }

    public List<Customer> searchCustomersByName(String name) {
        logger.info("Searching customers by name: " + name);
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Customer> searchCustomersByAddress(String address) {
        logger.info("Searching customers by address: " + address);
        return customerRepository.findByAddressContaining(address);
    }
}