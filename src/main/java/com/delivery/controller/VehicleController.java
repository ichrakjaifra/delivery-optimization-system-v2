package com.delivery.controller;

import com.delivery.dto.VehicleDTO;
import com.delivery.entity.Vehicle;
import com.delivery.mapper.VehicleMapper;
import com.delivery.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    public VehicleController(VehicleService vehicleService, VehicleMapper vehicleMapper) {
        this.vehicleService = vehicleService;
        this.vehicleMapper = vehicleMapper;
    }

    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAllVehicles() {
        try {
            List<VehicleDTO> vehicles = vehicleService.getAllVehicles().stream()
                    .map(vehicleMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getVehicleById(@PathVariable Long id) {
        try {
            return vehicleService.getVehicleById(id)
                    .map(vehicleMapper::toDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<VehicleDTO> createVehicle(@RequestBody VehicleDTO vehicleDTO) {
        try {
            Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO);
            Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
            VehicleDTO createdDTO = vehicleMapper.toDTO(createdVehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDTO> updateVehicle(@PathVariable Long id, @RequestBody VehicleDTO vehicleDTO) {
        try {
            Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO);
            Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicle);
            VehicleDTO updatedDTO = vehicleMapper.toDTO(updatedVehicle);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByType(@PathVariable Vehicle.VehicleType type) {
        try {
            List<VehicleDTO> vehicles = vehicleService.getVehiclesByType(type).stream()
                    .map(vehicleMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<VehicleDTO>> getAvailableVehicles() {
        try {
            List<VehicleDTO> vehicles = vehicleService.getAvailableVehicles().stream()
                    .map(vehicleMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/suitable")
    public ResponseEntity<List<VehicleDTO>> getSuitableVehicles(
            @RequestParam Double requiredWeight,
            @RequestParam Double requiredVolume) {
        try {
            List<VehicleDTO> vehicles = vehicleService.getSuitableVehicles(requiredWeight, requiredVolume).stream()
                    .map(vehicleMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple vehicles in batch")
    public ResponseEntity<List<VehicleDTO>> createVehiclesBatch(@RequestBody List<VehicleDTO> vehicleDTOs) {
        try {
            // Convertir les DTOs en entités
            List<Vehicle> vehicles = vehicleDTOs.stream()
                    .map(vehicleMapper::toEntity)
                    .collect(Collectors.toList());

            // Créer les véhicules en batch
            List<Vehicle> createdVehicles = vehicleService.createVehiclesBatch(vehicles);

            // Convertir en DTOs pour la réponse
            List<VehicleDTO> createdDTOs = createdVehicles.stream()
                    .map(vehicleMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}