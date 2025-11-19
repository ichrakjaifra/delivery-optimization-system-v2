package com.delivery.controller;

import com.delivery.dto.WarehouseDTO;
import com.delivery.entity.Warehouse;
import com.delivery.mapper.WarehouseMapper;
import com.delivery.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;

    public WarehouseController(WarehouseService warehouseService, WarehouseMapper warehouseMapper) {
        this.warehouseService = warehouseService;
        this.warehouseMapper = warehouseMapper;
    }

    @GetMapping
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses() {
        try {
            List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses().stream()
                    .map(warehouseMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(warehouses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDTO> getWarehouseById(@PathVariable Long id) {
        try {
            return warehouseService.getWarehouseById(id)
                    .map(warehouseMapper::toDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<WarehouseDTO> createWarehouse(@RequestBody WarehouseDTO warehouseDTO) {
        try {
            Warehouse warehouse = warehouseMapper.toEntity(warehouseDTO);
            Warehouse createdWarehouse = warehouseService.createWarehouse(warehouse);
            WarehouseDTO createdDTO = warehouseMapper.toDTO(createdWarehouse);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseDTO> updateWarehouse(@PathVariable Long id, @RequestBody WarehouseDTO warehouseDTO) {
        try {
            Warehouse warehouse = warehouseMapper.toEntity(warehouseDTO);
            Warehouse updatedWarehouse = warehouseService.updateWarehouse(id, warehouse);
            WarehouseDTO updatedDTO = warehouseMapper.toDTO(updatedWarehouse);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        try {
            warehouseService.deleteWarehouse(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple warehouses in batch")
    public ResponseEntity<List<WarehouseDTO>> createWarehousesBatch(@RequestBody List<WarehouseDTO> warehouseDTOs) {
        try {
            // Convertir les DTOs en entités
            List<Warehouse> warehouses = warehouseDTOs.stream()
                    .map(warehouseMapper::toEntity)
                    .collect(Collectors.toList());

            // Créer les entrepôts en batch
            List<Warehouse> createdWarehouses = warehouseService.createWarehousesBatch(warehouses);

            // Convertir en DTOs pour la réponse
            List<WarehouseDTO> createdDTOs = createdWarehouses.stream()
                    .map(warehouseMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}