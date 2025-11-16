package com.delivery.service;

import com.delivery.entity.Vehicle;
import com.delivery.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle van;
    private Vehicle bike;
    private Vehicle truck;

    private static final String VALIDATION_PREFIX = "Erreur de validation: ";

    @BeforeEach
    void setUp() {


        van = new Vehicle();
        van.setId(1L);
        van.setLicensePlate("VAN-123");
        van.setType(Vehicle.VehicleType.VAN);
        van.setMaxWeight(Vehicle.Constraints.VAN_MAX_WEIGHT);
        van.setMaxVolume(Vehicle.Constraints.VAN_MAX_VOLUME);
        van.setMaxDeliveries(Vehicle.Constraints.VAN_MAX_DELIVERIES);
        van.setRange(500.0);


        bike = new Vehicle();
        bike.setId(2L);
        bike.setLicensePlate("BIKE-456");
        bike.setType(Vehicle.VehicleType.BIKE);
        bike.setMaxWeight(Vehicle.Constraints.BIKE_MAX_WEIGHT);
        bike.setMaxVolume(Vehicle.Constraints.BIKE_MAX_VOLUME);
        bike.setMaxDeliveries(Vehicle.Constraints.BIKE_MAX_DELIVERIES);
        bike.setRange(100.0);


        truck = new Vehicle();
        truck.setId(3L);
        truck.setLicensePlate("TRUCK-789");
        truck.setType(Vehicle.VehicleType.TRUCK);
        truck.setMaxWeight(Vehicle.Constraints.TRUCK_MAX_WEIGHT);
        truck.setMaxVolume(Vehicle.Constraints.TRUCK_MAX_VOLUME);
        truck.setMaxDeliveries(Vehicle.Constraints.TRUCK_MAX_DELIVERIES);
        truck.setRange(1000.0);
    }

    @Test
    void getAllVehicles_ShouldReturnAllVehicles() {
        // Arrange
        List<Vehicle> expectedVehicles = Arrays.asList(van, bike, truck);
        when(vehicleRepository.findAll()).thenReturn(expectedVehicles);

        // Act
        List<Vehicle> result = vehicleService.getAllVehicles();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    void getVehicleById_WithValidId_ShouldReturnVehicle() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(van));

        // Act
        Optional<Vehicle> result = vehicleService.getVehicleById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(van.getId(), result.get().getId());
        assertEquals(van.getLicensePlate(), result.get().getLicensePlate());
        assertEquals(van.getType(), result.get().getType());
    }

    @Test
    void getVehicleById_WithInvalidId_ShouldReturnEmpty() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Vehicle> result = vehicleService.getVehicleById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void createVehicle_WithValidVan_ShouldSaveAndReturnVehicle() {
        // Arrange
        when(vehicleRepository.findByLicensePlate("VAN-123")).thenReturn(null);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(van);

        // Act
        Vehicle result = vehicleService.createVehicle(van);

        // Assert
        assertNotNull(result);
        assertEquals(van.getId(), result.getId());
        assertEquals(van.getLicensePlate(), result.getLicensePlate());
        verify(vehicleRepository, times(1)).save(van);
    }

    @Test
    void createVehicle_WithValidBike_ShouldSaveAndReturnVehicle() {
        // Arrange
        when(vehicleRepository.findByLicensePlate("BIKE-456")).thenReturn(null);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(bike);

        // Act
        Vehicle result = vehicleService.createVehicle(bike);

        // Assert
        assertNotNull(result);
        assertEquals(bike.getId(), result.getId());
        assertEquals(Vehicle.VehicleType.BIKE, result.getType());
        verify(vehicleRepository, times(1)).save(bike);
    }

    @Test
    void createVehicle_WithValidTruck_ShouldSaveAndReturnVehicle() {
        // Arrange
        when(vehicleRepository.findByLicensePlate("TRUCK-789")).thenReturn(null);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(truck);

        // Act
        Vehicle result = vehicleService.createVehicle(truck);

        // Assert
        assertNotNull(result);
        assertEquals(truck.getId(), result.getId());
        assertEquals(Vehicle.VehicleType.TRUCK, result.getType());
        verify(vehicleRepository, times(1)).save(truck);
    }

    @Test
    void createVehicle_WithDuplicateLicensePlate_ShouldThrowException() {
        // Arrange
        when(vehicleRepository.findByLicensePlate("VAN-123")).thenReturn(van);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehicleService.createVehicle(van);
        });

        assertEquals("Vehicle with license plate VAN-123 already exists", exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_WithBikeExceedingConstraints_ShouldThrowException() {
        // Arrange
        Vehicle invalidBike = new Vehicle();
        invalidBike.setType(Vehicle.VehicleType.BIKE);
        // NullPointerException
        invalidBike.setMaxWeight(60.0);
        invalidBike.setMaxVolume(Vehicle.Constraints.BIKE_MAX_VOLUME);
        invalidBike.setMaxDeliveries(Vehicle.Constraints.BIKE_MAX_DELIVERIES);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehicleService.createVehicle(invalidBike);
        });


        String expectedMessage = VALIDATION_PREFIX + "Poids max vélo cargo: " + Vehicle.Constraints.BIKE_MAX_WEIGHT + "kg";
        assertEquals(expectedMessage, exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_WithVanExceedingConstraints_ShouldThrowException() {
        // Arrange
        Vehicle invalidVan = new Vehicle();
        invalidVan.setType(Vehicle.VehicleType.VAN);
        // NullPointerException
        invalidVan.setMaxWeight(Vehicle.Constraints.VAN_MAX_WEIGHT);
        invalidVan.setMaxVolume(10.0);
        invalidVan.setMaxDeliveries(Vehicle.Constraints.VAN_MAX_DELIVERIES);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehicleService.createVehicle(invalidVan);
        });


        String expectedMessage = VALIDATION_PREFIX + "Volume max camionnette: " + Vehicle.Constraints.VAN_MAX_VOLUME + "m³";
        assertEquals(expectedMessage, exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_WithTruckExceedingConstraints_ShouldThrowException() {
        // Arrange
        Vehicle invalidTruck = new Vehicle();
        invalidTruck.setType(Vehicle.VehicleType.TRUCK);

        invalidTruck.setMaxWeight(Vehicle.Constraints.TRUCK_MAX_WEIGHT);
        invalidTruck.setMaxVolume(Vehicle.Constraints.TRUCK_MAX_VOLUME);
        invalidTruck.setMaxDeliveries(150);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehicleService.createVehicle(invalidTruck);
        });


        String expectedMessage = VALIDATION_PREFIX + "Livraisons max camion: " + Vehicle.Constraints.TRUCK_MAX_DELIVERIES;
        assertEquals(expectedMessage, exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void updateVehicle_WithValidId_ShouldUpdateAndReturnVehicle() {
        // Arrange
        Vehicle updateDetails = new Vehicle();
        updateDetails.setLicensePlate("VAN-UPDATED");
        updateDetails.setType(Vehicle.VehicleType.VAN);
        updateDetails.setMaxWeight(900.0);
        updateDetails.setMaxVolume(7.0);
        updateDetails.setMaxDeliveries(45);
        updateDetails.setRange(550.0);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(van));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(van);

        // Act
        Vehicle result = vehicleService.updateVehicle(1L, updateDetails);

        // Assert
        assertNotNull(result);
        assertEquals("VAN-UPDATED", van.getLicensePlate());
        assertEquals(900.0, van.getMaxWeight());
        verify(vehicleRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).save(van);
    }

    @Test
    void updateVehicle_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehicleService.updateVehicle(99L, van);
        });

        assertEquals("Vehicle not found with id: 99", exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    void updateVehicle_WithInvalidConstraints_ShouldThrowException() {
        // Arrange
        Vehicle updateDetails = new Vehicle();
        updateDetails.setLicensePlate("VAN-ERROR");
        updateDetails.setType(Vehicle.VehicleType.VAN);
        //  NullPointerException
        updateDetails.setMaxWeight(2000.0);
        updateDetails.setMaxVolume(Vehicle.Constraints.VAN_MAX_VOLUME);
        updateDetails.setMaxDeliveries(Vehicle.Constraints.VAN_MAX_DELIVERIES);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(van));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehicleService.updateVehicle(1L, updateDetails);
        });


        String expectedMessage = VALIDATION_PREFIX + "Poids max camionnette: " + Vehicle.Constraints.VAN_MAX_WEIGHT + "kg";
        assertEquals(expectedMessage, exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void deleteVehicle_WithValidId_ShouldDeleteVehicle() {
        // Arrange
        when(vehicleRepository.existsById(1L)).thenReturn(true);

        // Act
        vehicleService.deleteVehicle(1L);

        // Assert
        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteVehicle_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehicleService.deleteVehicle(99L);
        });

        assertEquals("Vehicle not found with id: 99", exception.getMessage());
        verify(vehicleRepository, never()).deleteById(anyLong());
    }

    @Test
    void getVehiclesByType_ShouldReturnFilteredVehicles() {
        // Arrange
        List<Vehicle> vans = Arrays.asList(van);
        when(vehicleRepository.findByType(Vehicle.VehicleType.VAN)).thenReturn(vans);

        // Act
        List<Vehicle> result = vehicleService.getVehiclesByType(Vehicle.VehicleType.VAN);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Vehicle.VehicleType.VAN, result.get(0).getType());
        verify(vehicleRepository, times(1)).findByType(Vehicle.VehicleType.VAN);
    }

    @Test
    void getAvailableVehicles_ShouldReturnAvailableVehicles() {
        // Arrange
        List<Vehicle> availableVehicles = Arrays.asList(bike, truck);
        when(vehicleRepository.findAvailableVehicles()).thenReturn(availableVehicles);

        // Act
        List<Vehicle> result = vehicleService.getAvailableVehicles();

        // Assert
        assertEquals(2, result.size());
        verify(vehicleRepository, times(1)).findAvailableVehicles();
    }

    @Test
    void getSuitableVehicles_WithWeightAndVolume_ShouldReturnSuitableVehicles() {
        // Arrange
        Double requiredWeight = 800.0;
        Double requiredVolume = 6.0;
        List<Vehicle> suitableVehicles = Arrays.asList(van, truck);
        when(vehicleRepository.findSuitableVehicles(requiredWeight, requiredVolume)).thenReturn(suitableVehicles);

        // Act
        List<Vehicle> result = vehicleService.getSuitableVehicles(requiredWeight, requiredVolume);

        // Assert
        assertEquals(2, result.size());
        verify(vehicleRepository, times(1)).findSuitableVehicles(requiredWeight, requiredVolume);
    }

    @Test
    void getSuitableVehicles_WithHighRequirements_ShouldReturnOnlyTruck() {
        // Arrange
        Double requiredWeight = 3000.0;
        Double requiredVolume = 30.0;
        List<Vehicle> suitableVehicles = Arrays.asList(truck);
        when(vehicleRepository.findSuitableVehicles(requiredWeight, requiredVolume)).thenReturn(suitableVehicles);

        // Act
        List<Vehicle> result = vehicleService.getSuitableVehicles(requiredWeight, requiredVolume);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Vehicle.VehicleType.TRUCK, result.get(0).getType());
    }

    @Test
    void getSuitableVehicles_WithExcessiveRequirements_ShouldReturnEmptyList() {
        // Arrange
        Double requiredWeight = 6000.0; // Exceeds all vehicles
        Double requiredVolume = 50.0;   // Exceeds all vehicles
        List<Vehicle> suitableVehicles = Arrays.asList();
        when(vehicleRepository.findSuitableVehicles(requiredWeight, requiredVolume)).thenReturn(suitableVehicles);

        // Act
        List<Vehicle> result = vehicleService.getSuitableVehicles(requiredWeight, requiredVolume);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getVehicleByLicensePlate_WithExistingPlate_ShouldReturnVehicle() {
        // Arrange
        when(vehicleRepository.findByLicensePlate("VAN-123")).thenReturn(van);

        // Act
        Vehicle result = vehicleService.getVehicleByLicensePlate("VAN-123");

        // Assert
        assertNotNull(result);
        assertEquals(van.getLicensePlate(), result.getLicensePlate());
        verify(vehicleRepository, times(1)).findByLicensePlate("VAN-123");
    }

    @Test
    void getVehicleByLicensePlate_WithNonExistingPlate_ShouldReturnNull() {
        // Arrange
        when(vehicleRepository.findByLicensePlate("UNKNOWN-123")).thenReturn(null);

        // Act
        Vehicle result = vehicleService.getVehicleByLicensePlate("UNKNOWN-123");

        // Assert
        assertNull(result);
    }

    @Test
    void isValidForDelivery_WithValidParameters_ShouldReturnTrue() {
        // Act
        boolean result = van.isValidForDelivery(800.0, 6.0, 40);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidForDelivery_WithExceedingWeight_ShouldReturnFalse() {
        // Act
        boolean result = van.isValidForDelivery(1200.0, 6.0, 40);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidForDelivery_WithExceedingVolume_ShouldReturnFalse() {
        // Act
        boolean result = van.isValidForDelivery(800.0, 9.0, 40);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidForDelivery_WithExceedingDeliveries_ShouldReturnFalse() {
        // Act
        boolean result = van.isValidForDelivery(800.0, 6.0, 60);

        // Assert
        assertFalse(result);
    }
}