package com.delivery.service;

import com.delivery.entity.Customer;
import com.delivery.entity.Delivery;
import com.delivery.repository.DeliveryRepository;
import com.delivery.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery delivery;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // Setup Customer avec toutes les propriétés requises
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Client Test");
        customer.setAddress("123 Rue Test, Casablanca");
        customer.setLatitude(33.5731);
        customer.setLongitude(-7.5898);
        customer.setPreferredTimeSlot("09:00-12:00");

        // Setup Delivery avec customer
        delivery = new Delivery();
        delivery.setId(1L);
        delivery.setWeight(5.0);
        delivery.setVolume(0.5);
        delivery.setPreferredTimeSlot("09:00-11:00");
        delivery.setStatus(Delivery.DeliveryStatus.PENDING);
        delivery.setCustomer(customer);
        delivery.setOrder(1);
    }

    @Test
    void getAllDeliveries_ShouldReturnAllDeliveries() {
        // Arrange
        List<Delivery> expectedDeliveries = Arrays.asList(delivery);
        when(deliveryRepository.findAll()).thenReturn(expectedDeliveries);

        // Act
        List<Delivery> result = deliveryService.getAllDeliveries();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(customer, result.get(0).getCustomer());
        verify(deliveryRepository, times(1)).findAll();
    }

    @Test
    void getDeliveryById_WithValidId_ShouldReturnDeliveryWithCustomer() {
        // Arrange
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

        // Act
        Optional<Delivery> result = deliveryService.getDeliveryById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(delivery.getId(), result.get().getId());
        assertNotNull(result.get().getCustomer());
        assertEquals(customer.getName(), result.get().getCustomer().getName());
    }

    @Test
    void createDelivery_WithValidDataAndCustomer_ShouldSaveAndReturnDelivery() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

        // Act
        Delivery result = deliveryService.createDelivery(delivery, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(delivery.getId(), result.getId());
        assertEquals(customer, result.getCustomer());
        verify(deliveryRepository, times(1)).save(delivery);
    }

    @Test
    void createDelivery_WithCustomerInheritance_ShouldUseCustomerCoordinates() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

        // Act
        Delivery result = deliveryService.createDelivery(delivery, 1L);

        // Assert
        assertNotNull(result);
        // Vérifier que la livraison hérite des coordonnées du client
        assertEquals(customer.getLatitude(), result.getLatitude());
        assertEquals(customer.getLongitude(), result.getLongitude());
        assertEquals(customer.getAddress(), result.getAddress());
    }

    @Test
    void createDelivery_WithTimeSlotOverride_ShouldUseDeliveryTimeSlot() {
        // Arrange
        delivery.setPreferredTimeSlot("10:00-12:00"); // Différent du client
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

        // Act
        Delivery result = deliveryService.createDelivery(delivery, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("10:00-12:00", result.getPreferredTimeSlot());
        // Le créneau du client reste inchangé
        assertEquals("09:00-12:00", result.getCustomer().getPreferredTimeSlot());
    }

    @Test
    void createDelivery_WithNullTimeSlot_ShouldUseCustomerTimeSlot() {
        // Arrange
        delivery.setPreferredTimeSlot(null);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

        // Act
        Delivery result = deliveryService.createDelivery(delivery, 1L);

        // Assert
        assertNotNull(result);
        // La livraison devrait utiliser le créneau du client via getCustomerPreferredTimeSlot()
        assertEquals("09:00-12:00", result.getCustomerPreferredTimeSlot());
    }

    @Test
    void updateDelivery_WithCustomerChange_ShouldUpdateCustomer() {
        // Arrange
        Customer newCustomer = new Customer();
        newCustomer.setId(2L);
        newCustomer.setName("Nouveau Client");
        newCustomer.setAddress("456 Nouvelle Adresse");
        newCustomer.setLatitude(34.0209);
        newCustomer.setLongitude(-6.8416);

        Delivery updatedDelivery = new Delivery();
        updatedDelivery.setWeight(8.0);
        updatedDelivery.setVolume(0.8);

        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(newCustomer));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(updatedDelivery);

        // Act
        Delivery result = deliveryService.updateDelivery(1L, updatedDelivery, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(newCustomer, delivery.getCustomer());
        verify(deliveryRepository, times(1)).save(delivery);
    }

    @Test
    void getDeliveriesByCustomer_ShouldReturnCustomerDeliveries() {
        // Arrange
        List<Delivery> customerDeliveries = Arrays.asList(delivery);
        when(deliveryRepository.findByCustomerId(1L)).thenReturn(customerDeliveries);

        // Act
        List<Delivery> result = deliveryService.getDeliveriesByCustomer(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getCustomer().getId());
        verify(deliveryRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void deliveryCoordinateMethods_ShouldReturnCustomerCoordinates() {
        // Act
        String address = delivery.getAddress();
        Double latitude = delivery.getLatitude();
        Double longitude = delivery.getLongitude();
        String customerTimeSlot = delivery.getCustomerPreferredTimeSlot();

        // Assert
        assertEquals("123 Rue Test, Casablanca", address);
        assertEquals(33.5731, latitude);
        assertEquals(-7.5898, longitude);
        assertEquals("09:00-12:00", customerTimeSlot);
    }

    @Test
    void deliveryValidation_WithValidCustomer_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> delivery.validate());
    }

    @Test
    void deliveryValidation_WithNullCustomer_ShouldThrowException() {
        // Arrange
        delivery.setCustomer(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            delivery.validate();
        });

        assertTrue(exception.getMessage().contains("Le client est obligatoire"));
    }

    @Test
    void createBatchDeliveries_WithMultipleCustomers_ShouldCreateAll() {
        // Arrange
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Client 2");
        customer2.setAddress("789 Autre Adresse");
        customer2.setLatitude(33.5841);
        customer2.setLongitude(-7.6116);

        Delivery delivery2 = new Delivery();
        delivery2.setWeight(3.0);
        delivery2.setVolume(0.3);
        delivery2.setCustomer(customer2);

        List<Delivery> deliveries = Arrays.asList(delivery, delivery2);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer2));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery, delivery2);

        // Act & Assert - Note: Vous devrez peut-être adapter pour supporter le batch
        for (int i = 0; i < deliveries.size(); i++) {
            Delivery result = deliveryService.createDelivery(deliveries.get(i), deliveries.get(i).getCustomer().getId());
            assertNotNull(result);
        }

        verify(deliveryRepository, times(2)).save(any(Delivery.class));
    }
}