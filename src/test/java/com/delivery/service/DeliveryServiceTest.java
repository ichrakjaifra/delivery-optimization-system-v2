//package com.delivery.service;
//
//import com.delivery.entity.Customer;
//import com.delivery.entity.Delivery;
//import com.delivery.repository.DeliveryRepository;
//import com.delivery.repository.CustomerRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class DeliveryServiceTest {
//
//    @Mock
//    private DeliveryRepository deliveryRepository;
//
//    @Mock
//    private CustomerRepository customerRepository;
//
//    @InjectMocks
//    private DeliveryService deliveryService;
//
//    private Delivery delivery;
//    private Customer customer;
//
//    @BeforeEach
//    void setUp() {
//        // Setup Customer
//        customer = new Customer();
//        customer.setId(1L);
//        customer.setName("Client Test");
//        customer.setAddress("123 Rue Test, Casablanca");
//        customer.setLatitude(33.5731);
//        customer.setLongitude(-7.5898);
//        customer.setPreferredTimeSlot("09:00-12:00");
//
//        // Setup Delivery (SANS address/latitude/longitude)
//        delivery = new Delivery();
//        delivery.setId(1L);
//        delivery.setWeight(5.0);
//        delivery.setVolume(0.5);
//        delivery.setPreferredTimeSlot("09:00-11:00");
//        delivery.setStatus(Delivery.DeliveryStatus.PENDING);
//        delivery.setCustomer(customer);
//        delivery.setOrder(1);
//    }
//
//    @Test
//    void getAllDeliveries_ShouldReturnAllDeliveries() {
//        // Arrange
//        List<Delivery> expectedDeliveries = Arrays.asList(delivery);
//        when(deliveryRepository.findAll()).thenReturn(expectedDeliveries);
//
//        // Act
//        List<Delivery> result = deliveryService.getAllDeliveries();
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(deliveryRepository, times(1)).findAll();
//    }
//
//    @Test
//    void getDeliveryById_WithValidId_ShouldReturnDelivery() {
//        // Arrange
//        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
//
//        // Act
//        Optional<Delivery> result = deliveryService.getDeliveryById(1L);
//
//        // Assert
//        assertTrue(result.isPresent());
//        assertEquals(delivery.getId(), result.get().getId());
//        assertEquals(delivery.getWeight(), result.get().getWeight());
//    }
//
//    @Test
//    void createDelivery_WithValidDataAndCustomer_ShouldSaveAndReturnDelivery() {
//        // Arrange
//        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
//        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
//
//        // Act
//        Delivery result = deliveryService.createDelivery(delivery, 1L);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(delivery.getId(), result.getId());
//        assertEquals(customer, result.getCustomer());
//        verify(deliveryRepository, times(1)).save(delivery);
//    }
//
//    @Test
//    void createDelivery_WithInvalidCustomer_ShouldThrowException() {
//        // Arrange
//        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            deliveryService.createDelivery(delivery, 99L);
//        });
//
//        assertEquals("Customer not found with id: 99", exception.getMessage());
//        verify(deliveryRepository, never()).save(any());
//    }
//
//    @Test
//    void createDelivery_WithInvalidWeight_ShouldThrowException() {
//        // Arrange
//        delivery.setWeight(-5.0);
//        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            deliveryService.createDelivery(delivery, 1L);
//        });
//
//        assertTrue(exception.getMessage().contains("Erreur de validation"));
//        verify(deliveryRepository, never()).save(any());
//    }
//
//    @Test
//    void updateDelivery_WithValidIdAndCustomer_ShouldUpdateAndReturnDelivery() {
//        // Arrange
//        Delivery updatedDelivery = new Delivery();
//        updatedDelivery.setWeight(8.0);
//        updatedDelivery.setVolume(0.8);
//        updatedDelivery.setPreferredTimeSlot("10:00-12:00");
//        updatedDelivery.setStatus(Delivery.DeliveryStatus.IN_TRANSIT);
//        updatedDelivery.setOrder(2);
//
//        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
//        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
//        when(deliveryRepository.save(any(Delivery.class))).thenReturn(updatedDelivery);
//
//        // Act
//        Delivery result = deliveryService.updateDelivery(1L, updatedDelivery, 1L);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(8.0, delivery.getWeight());
//        assertEquals(0.8, delivery.getVolume());
//        verify(deliveryRepository, times(1)).findById(1L);
//        verify(deliveryRepository, times(1)).save(delivery);
//    }
//
//    @Test
//    void updateDelivery_WithoutCustomerChange_ShouldUpdateAndReturnDelivery() {
//        // Arrange
//        Delivery updatedDelivery = new Delivery();
//        updatedDelivery.setWeight(8.0);
//        updatedDelivery.setVolume(0.8);
//
//        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
//        when(deliveryRepository.save(any(Delivery.class))).thenReturn(updatedDelivery);
//
//        // Act
//        Delivery result = deliveryService.updateDelivery(1L, updatedDelivery, null);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(8.0, delivery.getWeight());
//        verify(deliveryRepository, times(1)).save(delivery);
//    }
//
//    @Test
//    void updateDelivery_WithInvalidId_ShouldThrowException() {
//        // Arrange
//        when(deliveryRepository.findById(99L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            deliveryService.updateDelivery(99L, delivery, 1L);
//        });
//
//        assertEquals("Delivery not found with id: 99", exception.getMessage());
//    }
//
//    @Test
//    void deleteDelivery_WithValidId_ShouldDeleteDelivery() {
//        // Arrange
//        when(deliveryRepository.existsById(1L)).thenReturn(true);
//
//        // Act
//        deliveryService.deleteDelivery(1L);
//
//        // Assert
//        verify(deliveryRepository, times(1)).deleteById(1L);
//    }
//
//    @Test
//    void getDeliveriesByCustomer_ShouldReturnCustomerDeliveries() {
//        // Arrange
//        List<Delivery> customerDeliveries = Arrays.asList(delivery);
//        when(deliveryRepository.findByCustomerId(1L)).thenReturn(customerDeliveries);
//
//        // Act
//        List<Delivery> result = deliveryService.getDeliveriesByCustomer(1L);
//
//        // Assert
//        assertEquals(1, result.size());
//        verify(deliveryRepository, times(1)).findByCustomerId(1L);
//    }
//
//    @Test
//    void deliveryGetAddress_ShouldReturnCustomerAddress() {
//        // Act
//        String address = delivery.getAddress();
//        Double latitude = delivery.getLatitude();
//        Double longitude = delivery.getLongitude();
//
//        // Assert
//        assertEquals("123 Rue Test, Casablanca", address);
//        assertEquals(33.5731, latitude);
//        assertEquals(-7.5898, longitude);
//    }
//
//    @Test
//    void deliveryValidation_WithValidData_ShouldNotThrowException() {
//        // Act & Assert
//        assertDoesNotThrow(() -> delivery.validate());
//    }
//
//    @Test
//    void deliveryValidation_WithNullCustomer_ShouldThrowException() {
//        // Arrange
//        delivery.setCustomer(null);
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            delivery.validate();
//        });
//
//        assertTrue(exception.getMessage().contains("Le client est obligatoire"));
//    }
//}