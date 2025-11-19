package com.delivery.integration;

import com.delivery.entity.Customer;
import com.delivery.entity.Delivery;
import com.delivery.service.CustomerService;
import com.delivery.service.DeliveryService;
import com.delivery.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class SimpleDataIntegrationTest {


    @Autowired
    private CustomerService customerService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void test_CreateCustomerAndDelivery_Flow() {
        Customer newCustomer = new Customer(
                null,
                "Client Simple",
                "123 Rue Test",
                33.0,
                -7.0,
                "09:00-11:00",
                null, // Deliveries list
                null  // DeliveryHistory list
        );

        Customer savedCustomer = customerService.createCustomer(newCustomer);

        assertNotNull(savedCustomer.getId(), "Customer ID should be generated.");
        assertEquals("Client Simple", savedCustomer.getName());

        Delivery newDelivery = new Delivery(
                null,
                5.0,
                0.5,
                null,
                Delivery.DeliveryStatus.PENDING,
                null,
                null,
                null
        );

        Delivery savedDelivery = deliveryService.createDelivery(newDelivery, savedCustomer.getId());

        assertNotNull(savedDelivery.getId(), "Delivery ID should be generated.");

        assertNotNull(savedDelivery.getCustomer(), "Delivery must be linked to the Customer Entity.");
        assertEquals(savedCustomer.getId(), savedDelivery.getCustomer().getId(), "The linked Customer ID is incorrect.");

        assertTrue(customerRepository.findById(savedCustomer.getId()).isPresent(), "Customer must exist in DB.");
    }
}