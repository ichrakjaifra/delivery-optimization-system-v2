//package com.delivery.service;
//
//import com.delivery.entity.*;
//import com.delivery.optimizer.NearestNeighborOptimizer;
//import com.delivery.optimizer.ClarkeWrightOptimizer;
//import com.delivery.repository.TourRepository;
//import com.delivery.repository.DeliveryRepository;
//import com.delivery.repository.VehicleRepository;
//import com.delivery.repository.WarehouseRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TourServiceTest {
//
//    @Mock
//    private TourRepository tourRepository;
//
//    @Mock
//    private DeliveryRepository deliveryRepository;
//
//    @Mock
//    private VehicleRepository vehicleRepository;
//
//    @Mock
//    private WarehouseRepository warehouseRepository;
//
//    @Mock
//    private NearestNeighborOptimizer nearestNeighborOptimizer;
//
//    @Mock
//    private ClarkeWrightOptimizer clarkeWrightOptimizer;
//
//    private TourService tourService;
//
//    private Tour tour;
//    private Vehicle vehicle;
//    private Warehouse warehouse;
//    private Customer customer;
//    private Delivery delivery1;
//    private Delivery delivery2;
//
//    @BeforeEach
//    void setUp() {
//        this.tourService = new TourService(
//                tourRepository,
//                deliveryRepository,
//                vehicleRepository,
//                warehouseRepository,
//                nearestNeighborOptimizer,
//                clarkeWrightOptimizer
//        );
//
//        // Setup Customer
//        customer = new Customer();
//        customer.setId(1L);
//        customer.setName("Client Test");
//        customer.setAddress("123 Rue Test");
//        customer.setLatitude(33.5731);
//        customer.setLongitude(-7.5898);
//
//        // Setup Vehicle
//        vehicle = new Vehicle();
//        vehicle.setId(1L);
//        vehicle.setLicensePlate("ABC123");
//        vehicle.setType(Vehicle.VehicleType.VAN);
//        vehicle.setMaxWeight(1000.0);
//        vehicle.setMaxVolume(8.0);
//        vehicle.setMaxDeliveries(50);
//        vehicle.setRange(500.0);
//
//        // Setup Warehouse
//        warehouse = new Warehouse();
//        warehouse.setId(1L);
//        warehouse.setName("Entrepôt Principal");
//        warehouse.setAddress("123 Rue Entrepôt, Casablanca");
//        warehouse.setLatitude(33.5731);
//        warehouse.setLongitude(-7.5898);
//        warehouse.setOpeningHours("06:00-22:00");
//
//        // Setup Deliveries (AVEC Customer)
//        delivery1 = new Delivery();
//        delivery1.setId(1L);
//        delivery1.setWeight(5.0);
//        delivery1.setVolume(0.5);
//        delivery1.setCustomer(customer);
//
//        delivery2 = new Delivery();
//        delivery2.setId(2L);
//        delivery2.setWeight(10.0);
//        delivery2.setVolume(1.0);
//        delivery2.setCustomer(customer);
//
//        // Setup Tour
//        tour = new Tour();
//        tour.setId(1L);
//        tour.setDate(LocalDate.now());
//        tour.setVehicle(vehicle);
//        tour.setWarehouse(warehouse);
//        tour.setAlgorithmUsed(Tour.AlgorithmType.NEAREST_NEIGHBOR);
//        tour.setTotalDistance(50.0);
//        tour.setDeliveries(new ArrayList<>(Arrays.asList(delivery1, delivery2)));
//    }
//
//    // Les tests existants restent valides, mais voici les tests supplémentaires pour la nouvelle architecture
//
//    @Test
//    void createTour_WithDeliveriesHavingCustomers_ShouldSaveTour() {
//        // Arrange
//        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
//        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
//        when(tourRepository.save(any(Tour.class))).thenReturn(tour);
//
//        // Act
//        Tour result = tourService.createTour(tour);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(2, result.getDeliveries().size());
//        // Vérifier que les deliveries ont bien des customers
//        assertNotNull(result.getDeliveries().get(0).getCustomer());
//        assertNotNull(result.getDeliveries().get(1).getCustomer());
//        verify(tourRepository, times(1)).save(tour);
//    }
//
//    @Test
//    void optimizeTour_WithDeliveriesHavingCustomers_ShouldOptimizeCorrectly() {
//        // Arrange
//        List<Delivery> optimizedDeliveries = new ArrayList<>(Arrays.asList(delivery1, delivery2));
//        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
//        when(nearestNeighborOptimizer.calculateOptimalTour(
//                eq(warehouse),
//                any(List.class),
//                eq(vehicle)
//        )).thenReturn(optimizedDeliveries);
//        when(nearestNeighborOptimizer.calculateTotalDistance(eq(warehouse), any(List.class))).thenReturn(45.0);
//        when(tourRepository.save(any(Tour.class))).thenReturn(tour);
//
//        // Act
//        Tour result = tourService.optimizeTour(1L, Tour.AlgorithmType.NEAREST_NEIGHBOR);
//
//        // Assert
//        assertNotNull(result);
//        // Vérifier que les customers sont préservés après optimisation
//        assertEquals(customer, result.getDeliveries().get(0).getCustomer());
//        assertEquals(customer, result.getDeliveries().get(1).getCustomer());
//        verify(nearestNeighborOptimizer, times(1)).calculateOptimalTour(
//                eq(warehouse),
//                any(List.class),
//                eq(vehicle)
//        );
//    }
//
//    @Test
//    void getTotalDistance_ShouldUseCustomerCoordinates() {
//        // Arrange
//        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
//        when(nearestNeighborOptimizer.calculateTotalDistance(eq(warehouse), any(List.class))).thenReturn(42.5);
//
//        // Act
//        Double result = tourService.getTotalDistance(1L, Tour.AlgorithmType.NEAREST_NEIGHBOR);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(42.5, result);
//        // L'optimizer utilise les coordonnées du customer via delivery.getLatitude()/getLongitude()
//        verify(nearestNeighborOptimizer, times(1)).calculateTotalDistance(eq(warehouse), any(List.class));
//    }
//
//    @Test
//    void addDeliveryToTour_WithDeliveryHavingCustomer_ShouldMaintainCustomer() {
//        // Arrange
//        Delivery newDelivery = new Delivery();
//        newDelivery.setId(3L);
//        newDelivery.setWeight(7.0);
//        newDelivery.setVolume(0.7);
//        newDelivery.setCustomer(customer);
//
//        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
//        when(deliveryRepository.findById(3L)).thenReturn(Optional.of(newDelivery));
//
//        // Act
//        tourService.addDeliveryToTour(1L, 3L);
//
//        // Assert
//        verify(deliveryRepository, times(1)).save(newDelivery);
//        assertEquals(tour, newDelivery.getTour());
//        // Vérifier que le customer est conservé
//        assertEquals(customer, newDelivery.getCustomer());
//    }
//
//    @Test
//    void tourValidation_WithValidDeliveriesAndCustomers_ShouldNotThrowException() {
//        // Act & Assert
//        assertDoesNotThrow(() -> tour.validate());
//    }
//
//    @Test
//    void tourGetTotalWeight_ShouldCalculateFromDeliveries() {
//        // Act
//        Double totalWeight = tour.getTotalWeight();
//        Double totalVolume = tour.getTotalVolume();
//        Integer deliveryCount = tour.getDeliveryCount();
//
//        // Assert
//        assertEquals(15.0, totalWeight); // 5.0 + 10.0
//        assertEquals(1.5, totalVolume);  // 0.5 + 1.0
//        assertEquals(2, deliveryCount);
//    }
//
//    @Test
//    void isValidForVehicle_WithValidCapacity_ShouldReturnTrue() {
//        // Act
//        boolean result = tour.isValidForVehicle();
//
//        // Assert
//        assertTrue(result);
//    }
//
//    @Test
//    void deliveryGetCoordinates_ShouldReturnCustomerCoordinates() {
//        // Act
//        Double latitude1 = delivery1.getLatitude();
//        Double longitude1 = delivery1.getLongitude();
//        Double latitude2 = delivery2.getLatitude();
//        Double longitude2 = delivery2.getLongitude();
//
//        // Assert
//        assertEquals(33.5731, latitude1);
//        assertEquals(-7.5898, longitude1);
//        assertEquals(33.5731, latitude2);
//        assertEquals(-7.5898, longitude2);
//    }
//}