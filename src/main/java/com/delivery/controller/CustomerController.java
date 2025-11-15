package com.delivery.controller;

import com.delivery.dto.CustomerDTO;
import com.delivery.entity.Customer;
import com.delivery.mapper.CustomerMapper;
import com.delivery.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    public CustomerController(CustomerService customerService, CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        try {
            List<CustomerDTO> customers = customerService.getAllCustomers().stream()
                    .map(customerMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/paged")
    @Operation(summary = "Get all customers with pagination")
    public ResponseEntity<Page<CustomerDTO>> getAllCustomersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CustomerDTO> customers = customerService.getAllCustomersPaged(pageable)
                    .map(customerMapper::toDTO);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        try {
            return customerService.getCustomerById(id)
                    .map(customerMapper::toDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        try {
            Customer customer = customerMapper.toEntity(customerDTO);
            Customer createdCustomer = customerService.createCustomer(customer);
            CustomerDTO createdDTO = customerMapper.toDTO(createdCustomer);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        try {
            Customer customer = customerMapper.toEntity(customerDTO);
            Customer updatedCustomer = customerService.updateCustomer(id, customer);
            CustomerDTO updatedDTO = customerMapper.toDTO(updatedCustomer);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/name")
    @Operation(summary = "Search customers by name")
    public ResponseEntity<List<CustomerDTO>> searchCustomersByName(@RequestParam String name) {
        try {
            List<CustomerDTO> customers = customerService.searchCustomersByName(name).stream()
                    .map(customerMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/address")
    @Operation(summary = "Search customers by address")
    public ResponseEntity<List<CustomerDTO>> searchCustomersByAddress(@RequestParam String address) {
        try {
            List<CustomerDTO> customers = customerService.searchCustomersByAddress(address).stream()
                    .map(customerMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/advanced")
    @Operation(summary = "Advanced search for customers")
    public ResponseEntity<List<CustomerDTO>> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String timeSlot) {
        try {
            List<CustomerDTO> customers = customerService.advancedSearch(name, address, timeSlot).stream()
                    .map(customerMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple customers in batch")
    public ResponseEntity<List<CustomerDTO>> createCustomers(@RequestBody List<CustomerDTO> customerDTOs) {
        try {
            List<CustomerDTO> createdCustomers = new ArrayList<>();

            for (CustomerDTO customerDTO : customerDTOs) {
                Customer customer = customerMapper.toEntity(customerDTO);
                Customer createdCustomer = customerService.createCustomer(customer);
                CustomerDTO createdDTO = customerMapper.toDTO(createdCustomer);
                createdCustomers.add(createdDTO);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}