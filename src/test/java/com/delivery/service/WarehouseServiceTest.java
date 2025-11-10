//package com.delivery.service;
//
//import com.delivery.entity.Warehouse;
//import com.delivery.repository.WarehouseRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
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
//class WarehouseServiceTest {
//
//    @Mock
//    private WarehouseRepository warehouseRepository;
//
//    @InjectMocks
//    private WarehouseService warehouseService;
//
//    private Warehouse mainWarehouse;
//    private Warehouse secondaryWarehouse;
//
//
//    private static final String VALIDATION_PREFIX = "Erreur de validation: ";
//
//    @BeforeEach
//    void setUp() {
//        // Setup main warehouse
//        mainWarehouse = new Warehouse();
//        mainWarehouse.setId(1L);
//        mainWarehouse.setName("Entrepôt Principal");
//        mainWarehouse.setAddress("123 Rue Principale, Casablanca");
//        mainWarehouse.setLatitude(33.5731);
//        mainWarehouse.setLongitude(-7.5898);
//        mainWarehouse.setOpeningHours("06:00-22:00");
//
//        // Setup secondary warehouse
//        secondaryWarehouse = new Warehouse();
//        secondaryWarehouse.setId(2L);
//        secondaryWarehouse.setName("Entrepôt Secondaire");
//        secondaryWarehouse.setAddress("456 Avenue Secondaire, Rabat");
//        secondaryWarehouse.setLatitude(34.0209);
//        secondaryWarehouse.setLongitude(-6.8416);
//        secondaryWarehouse.setOpeningHours("08:00-20:00");
//    }
//
//    @Test
//    void getAllWarehouses_ShouldReturnAllWarehouses() {
//        // Arrange
//        List<Warehouse> expectedWarehouses = Arrays.asList(mainWarehouse, secondaryWarehouse);
//        when(warehouseRepository.findAll()).thenReturn(expectedWarehouses);
//
//        // Act
//        List<Warehouse> result = warehouseService.getAllWarehouses();
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        verify(warehouseRepository, times(1)).findAll();
//    }
//
//    @Test
//    void getWarehouseById_WithValidId_ShouldReturnWarehouse() {
//        // Arrange
//        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mainWarehouse));
//
//        // Act
//        Optional<Warehouse> result = warehouseService.getWarehouseById(1L);
//
//        // Assert
//        assertTrue(result.isPresent());
//        assertEquals(mainWarehouse.getId(), result.get().getId());
//        assertEquals(mainWarehouse.getName(), result.get().getName());
//        assertEquals(mainWarehouse.getAddress(), result.get().getAddress());
//    }
//
//    @Test
//    void getWarehouseById_WithInvalidId_ShouldReturnEmpty() {
//        // Arrange
//        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());
//
//        // Act
//        Optional<Warehouse> result = warehouseService.getWarehouseById(99L);
//
//        // Assert
//        assertFalse(result.isPresent());
//    }
//
//    @Test
//    void createWarehouse_WithValidData_ShouldSaveAndReturnWarehouse() {
//        // Arrange
//        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(mainWarehouse);
//
//        // Act
//        Warehouse result = warehouseService.createWarehouse(mainWarehouse);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(mainWarehouse.getId(), result.getId());
//        assertEquals(mainWarehouse.getName(), result.getName());
//        verify(warehouseRepository, times(1)).save(mainWarehouse);
//    }
//
//    @Test
//    void createWarehouse_WithInvalidLatitude_ShouldThrowException() {
//        // Arrange
//        Warehouse invalidWarehouse = createValidWarehouseCopy(mainWarehouse);
//        invalidWarehouse.setLatitude(100.0); // Invalid latitude
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            warehouseService.createWarehouse(invalidWarehouse);
//        });
//
//
//        assertEquals(VALIDATION_PREFIX + "Latitude invalide (-90 à 90)", exception.getMessage());
//        verify(warehouseRepository, never()).save(any(Warehouse.class));
//    }
//
//    @Test
//    void createWarehouse_WithInvalidLongitude_ShouldThrowException() {
//        // Arrange
//        Warehouse invalidWarehouse = createValidWarehouseCopy(mainWarehouse);
//        invalidWarehouse.setLongitude(-200.0); // Invalid longitude
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            warehouseService.createWarehouse(invalidWarehouse);
//        });
//
//
//        assertEquals(VALIDATION_PREFIX + "Longitude invalide (-180 à 180)", exception.getMessage());
//        verify(warehouseRepository, never()).save(any(Warehouse.class));
//    }
//
//    @Test
//    void createWarehouse_WithInvalidOpeningHours_ShouldThrowException() {
//        // Arrange
//        Warehouse invalidWarehouse = createValidWarehouseCopy(mainWarehouse);
//        invalidWarehouse.setOpeningHours("25:00-26:00"); // Invalid format
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            warehouseService.createWarehouse(invalidWarehouse);
//        });
//
//
//        assertEquals(VALIDATION_PREFIX + "Format d'horaire invalide. Utilisez: HH:MM-HH:MM", exception.getMessage());
//        verify(warehouseRepository, never()).save(any(Warehouse.class));
//    }
//
//    @Test
//    void createWarehouse_WithNullOpeningHours_ShouldThrowException() {
//        // Arrange
//        Warehouse invalidWarehouse = createValidWarehouseCopy(mainWarehouse);
//        invalidWarehouse.setOpeningHours(null);
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            warehouseService.createWarehouse(invalidWarehouse);
//        });
//
//
//        assertEquals(VALIDATION_PREFIX + "Les horaires d'ouverture sont obligatoires", exception.getMessage());
//        verify(warehouseRepository, never()).save(any(Warehouse.class));
//    }
//
//    @Test
//    void createWarehouse_WithEmptyOpeningHours_ShouldThrowException() {
//        // Arrange
//        Warehouse invalidWarehouse = createValidWarehouseCopy(mainWarehouse);
//        invalidWarehouse.setOpeningHours("");
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            warehouseService.createWarehouse(invalidWarehouse);
//        });
//
//
//        assertEquals(VALIDATION_PREFIX + "Les horaires d'ouverture sont obligatoires", exception.getMessage());
//        verify(warehouseRepository, never()).save(any(Warehouse.class));
//    }
//
//    @Test
//    void updateWarehouse_WithValidId_ShouldUpdateAndReturnWarehouse() {
//        // Arrange
//        Warehouse updateDetails = createValidWarehouseCopy(mainWarehouse);
//        updateDetails.setName("Entrepôt Principal Modifié");
//        updateDetails.setAddress("789 Nouvelle Adresse, Casablanca");
//        updateDetails.setLatitude(33.5741);
//        updateDetails.setLongitude(-7.5908);
//        updateDetails.setOpeningHours("07:00-23:00");
//
//
//        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mainWarehouse));
//        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(mainWarehouse);
//
//        // Act
//        Warehouse result = warehouseService.updateWarehouse(1L, updateDetails);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("Entrepôt Principal Modifié", mainWarehouse.getName());
//        assertEquals("789 Nouvelle Adresse, Casablanca", mainWarehouse.getAddress());
//        assertEquals("07:00-23:00", mainWarehouse.getOpeningHours());
//        verify(warehouseRepository, times(1)).findById(1L);
//        verify(warehouseRepository, times(1)).save(mainWarehouse);
//    }
//
//    @Test
//    void updateWarehouse_WithInvalidId_ShouldThrowException() {
//        // Arrange
//        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            warehouseService.updateWarehouse(99L, mainWarehouse);
//        });
//
//        assertEquals("Warehouse not found with id: 99", exception.getMessage());
//        verify(warehouseRepository, never()).save(any(Warehouse.class));
//    }
//
//
//    @MockitoSettings(strictness = Strictness.LENIENT)
//    @Test
//    void updateWarehouse_WithInvalidData_ShouldThrowException() {
//        // Arrange
//        Warehouse invalidUpdateDetails = createValidWarehouseCopy(mainWarehouse);
//        invalidUpdateDetails.setLatitude(-100.0); // Invalid latitude
//
//
//        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mainWarehouse));
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            warehouseService.updateWarehouse(1L, invalidUpdateDetails);
//        });
//
//
//        assertEquals(VALIDATION_PREFIX + "Latitude invalide (-90 à 90)", exception.getMessage());
//        verify(warehouseRepository, never()).save(any(Warehouse.class));
//    }
//
//    @Test
//    void deleteWarehouse_WithValidId_ShouldDeleteWarehouse() {
//        // Arrange
//        when(warehouseRepository.existsById(1L)).thenReturn(true);
//
//        // Act
//        warehouseService.deleteWarehouse(1L);
//
//        // Assert
//        verify(warehouseRepository, times(1)).deleteById(1L);
//    }
//
//    @Test
//    void deleteWarehouse_WithInvalidId_ShouldThrowException() {
//        // Arrange
//        when(warehouseRepository.existsById(99L)).thenReturn(false);
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            warehouseService.deleteWarehouse(99L);
//        });
//
//        assertEquals("Warehouse not found with id: 99", exception.getMessage());
//        verify(warehouseRepository, never()).deleteById(anyLong());
//    }
//
//    @Test
//    void getActiveWarehouses_ShouldReturnWarehousesWithOpeningHours() {
//        // Arrange
//        List<Warehouse> activeWarehouses = Arrays.asList(mainWarehouse, secondaryWarehouse);
//        when(warehouseRepository.findActiveWarehouses()).thenReturn(activeWarehouses);
//
//        // Act
//        List<Warehouse> result = warehouseService.getActiveWarehouses();
//
//        // Assert
//        assertEquals(2, result.size());
//        verify(warehouseRepository, times(1)).findActiveWarehouses();
//    }
//
//    @Test
//    void getWarehouseByName_WithExistingName_ShouldReturnWarehouse() {
//        // Arrange
//        when(warehouseRepository.findByName("Entrepôt Principal")).thenReturn(mainWarehouse);
//
//        // Act
//        Warehouse result = warehouseService.getWarehouseByName("Entrepôt Principal");
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(mainWarehouse.getName(), result.getName());
//        verify(warehouseRepository, times(1)).findByName("Entrepôt Principal");
//    }
//
//    @Test
//    void getWarehouseByName_WithNonExistingName_ShouldReturnNull() {
//        // Arrange
//        when(warehouseRepository.findByName("Entrepôt Inexistant")).thenReturn(null);
//
//        // Act
//        Warehouse result = warehouseService.getWarehouseByName("Entrepôt Inexistant");
//
//        // Assert
//        assertNull(result);
//    }
//
//    // --- Tests de Validation (Validation Tests) ---
//
//    @Test
//    void validate_WithValidWarehouse_ShouldNotThrowException() {
//        // Act & Assert
//        assertDoesNotThrow(() -> mainWarehouse.validate());
//    }
//
//    @Test
//    void validate_WithValidSecondaryWarehouse_ShouldNotThrowException() {
//        // Act & Assert
//        assertDoesNotThrow(() -> secondaryWarehouse.validate());
//    }
//
//    @Test
//    void validateCoordinates_WithValidCoordinates_ShouldNotThrowException() {
//        // Arrange
//        Warehouse warehouse = createValidWarehouseCopy(mainWarehouse);
//        warehouse.setLatitude(0.0);
//        warehouse.setLongitude(0.0);
//
//        // Act & Assert
//        assertDoesNotThrow(() -> warehouse.validate());
//    }
//
//    @Test
//    void validateCoordinates_WithBoundaryCoordinates_ShouldNotThrowException() {
//        // Arrange
//        Warehouse warehouse = createValidWarehouseCopy(mainWarehouse);
//        warehouse.setLatitude(90.0);  // Maximum latitude
//        warehouse.setLongitude(180.0); // Maximum longitude
//
//        // Act & Assert
//        assertDoesNotThrow(() -> warehouse.validate());
//    }
//
//    @Test
//    void validateCoordinates_WithNegativeBoundaryCoordinates_ShouldNotThrowException() {
//        // Arrange
//        Warehouse warehouse = createValidWarehouseCopy(mainWarehouse);
//        warehouse.setLatitude(-90.0);  // Minimum latitude
//        warehouse.setLongitude(-180.0); // Minimum longitude
//
//        // Act & Assert
//        assertDoesNotThrow(() -> warehouse.validate());
//    }
//
//    @Test
//    void validateOpeningHours_WithVariousValidFormats_ShouldNotThrowException() {
//        // Test various valid opening hours formats
//        String[] validOpeningHours = {
//                "00:00-23:59",
//                "09:00-18:00",
//                "06:30-22:45"
//        };
//
//        for (String hours : validOpeningHours) {
//            // Arrange
//            Warehouse warehouse = createValidWarehouseCopy(mainWarehouse);
//            warehouse.setOpeningHours(hours);
//
//            // Act & Assert
//            assertDoesNotThrow(() -> warehouse.validate(),
//                    "Should not throw exception for valid opening hours: " + hours);
//        }
//    }
//
//
//    private Warehouse createValidWarehouseCopy(Warehouse source) {
//        Warehouse copy = new Warehouse();
//        copy.setId(source.getId());
//        copy.setName(source.getName());
//        copy.setAddress(source.getAddress());
//        copy.setLatitude(source.getLatitude());
//        copy.setLongitude(source.getLongitude());
//        copy.setOpeningHours(source.getOpeningHours());
//        return copy;
//    }
//}