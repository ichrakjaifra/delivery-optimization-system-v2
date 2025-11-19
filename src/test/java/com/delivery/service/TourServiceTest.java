package com.delivery.service;

import com.delivery.entity.*;
import com.delivery.optimizer.TourOptimizer;
import com.delivery.repository.TourRepository;
import com.delivery.repository.DeliveryRepository;
import com.delivery.repository.VehicleRepository;
import com.delivery.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private DeliveryHistoryService deliveryHistoryService;

    @Mock
    private TourOptimizer nearestNeighborOptimizer;

    @Mock
    private TourOptimizer clarkeWrightOptimizer;

    @Mock
    private TourOptimizer aiOptimizer;


    private TourService tourService;

    private Tour tour;
    private Vehicle vehicle;
    private Warehouse warehouse;
    private Customer customer;
    private Delivery delivery1;
    private Delivery delivery2;

    @BeforeEach
    void setUp() {

        this.tourService = new TourService(
                tourRepository,
                deliveryRepository,
                vehicleRepository,
                warehouseRepository,
                deliveryHistoryService,
                nearestNeighborOptimizer,
                clarkeWrightOptimizer

        );

        // Setup Customer
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Client Test");
        customer.setAddress("123 Rue Test, Casablanca");
        customer.setLatitude(33.5731);
        customer.setLongitude(-7.5898);
        customer.setPreferredTimeSlot("09:00-12:00");

        // Setup Vehicle
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setLicensePlate("ABC123");
        vehicle.setType(Vehicle.VehicleType.VAN);
        vehicle.setMaxWeight(1000.0);
        vehicle.setMaxVolume(8.0);
        vehicle.setMaxDeliveries(50);
        vehicle.setRange(500.0);

        // Setup Warehouse
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Entrepôt Principal");
        warehouse.setAddress("123 Rue Entrepôt, Casablanca");
        warehouse.setLatitude(33.5731);
        warehouse.setLongitude(-7.5898);
        warehouse.setOpeningHours("06:00-22:00");

        // Setup Deliveries
        delivery1 = new Delivery();
        delivery1.setId(1L);
        delivery1.setWeight(5.0);
        delivery1.setVolume(0.5);
        delivery1.setPreferredTimeSlot("09:00-11:00");
        delivery1.setCustomer(customer);
        delivery1.setOrder(1);
        // delivery1.setTour(tour);

        delivery2 = new Delivery();
        delivery2.setId(2L);
        delivery2.setWeight(10.0);
        delivery2.setVolume(1.0);
        delivery2.setPreferredTimeSlot("10:00-12:00");
        delivery2.setCustomer(customer);
        delivery2.setOrder(2);

        // Setup Tour
        tour = new Tour();
        tour.setId(1L);
        tour.setDate(LocalDate.now());
        tour.setVehicle(vehicle);
        tour.setWarehouse(warehouse);
        tour.setAlgorithmUsed(Tour.AlgorithmType.NEAREST_NEIGHBOR);
        tour.setTotalDistance(50.0);
        tour.setStatus(Tour.TourStatus.PLANNED);
        tour.setDeliveries(new ArrayList<>(Arrays.asList(delivery1, delivery2)));
    }

    @Test
    void removeDeliveryFromTour_ShouldMaintainCustomerData() {
        // Arrange
        Long tourId = 1L;
        Long deliveryId = 1L;

        delivery1.setTour(tour);

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery1));

        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery1);

        // Act
        tourService.removeDeliveryFromTour(tourId, deliveryId);

        // Assert
        verify(deliveryRepository, times(1)).save(delivery1);

        assertNull(delivery1.getTour());
        assertNull(delivery1.getOrder());

        assertEquals(customer, delivery1.getCustomer());
    }

    @Test
    void getOptimizedTour_ShouldReturnDeliveriesWithCustomers() {
        // Arrange
        List<Delivery> optimizedDeliveries = Arrays.asList(delivery1, delivery2);
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(nearestNeighborOptimizer.calculateOptimalTour(
                eq(warehouse),
                any(List.class),
                eq(vehicle)
        )).thenReturn(optimizedDeliveries);

        // Act
        List<Delivery> result = tourService.getOptimizedTour(1L, Tour.AlgorithmType.NEAREST_NEIGHBOR);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(delivery -> {
            assertNotNull(delivery.getCustomer());
            assertNotNull(delivery.getAddress());
        });
    }

    @Test
    void getTotalDistance_ShouldCalculateUsingCustomerCoordinates() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(nearestNeighborOptimizer.calculateTotalDistance(eq(warehouse), any(List.class))).thenReturn(42.5);

        // Act
        Double result = tourService.getTotalDistance(1L, Tour.AlgorithmType.NEAREST_NEIGHBOR);

        // Assert
        assertNotNull(result);
        assertEquals(42.5, result);
    }
}