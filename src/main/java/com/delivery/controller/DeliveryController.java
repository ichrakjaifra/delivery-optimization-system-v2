package com.delivery.controller;

import com.delivery.dto.DeliveryDTO;
import com.delivery.entity.Delivery;
import com.delivery.mapper.DeliveryMapper;
import com.delivery.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryMapper deliveryMapper;

    public DeliveryController(DeliveryService deliveryService, DeliveryMapper deliveryMapper) {
        this.deliveryService = deliveryService;
        this.deliveryMapper = deliveryMapper;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveries() {
        try {
            List<DeliveryDTO> deliveries = deliveryService.getAllDeliveries().stream()
                    .map(deliveryMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Long id) {
        try {
            return deliveryService.getDeliveryById(id)
                    .map(deliveryMapper::toDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<DeliveryDTO> createDelivery(@RequestBody DeliveryDTO deliveryDTO, @RequestParam Long customerId) {
        try {
            Delivery delivery = deliveryMapper.toEntity(deliveryDTO);
            Delivery createdDelivery = deliveryService.createDelivery(delivery, customerId);
            DeliveryDTO createdDTO = deliveryMapper.toDTO(createdDelivery);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryDTO> updateDelivery(@PathVariable Long id, @RequestBody DeliveryDTO deliveryDTO, @RequestParam(required = false) Long customerId) {
        try {
            Delivery delivery = deliveryMapper.toEntity(deliveryDTO);
            Delivery updatedDelivery = deliveryService.updateDelivery(id, delivery, customerId);
            DeliveryDTO updatedDTO = deliveryMapper.toDTO(updatedDelivery);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        try {
            deliveryService.deleteDelivery(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryDTO>> getDeliveriesByStatus(@PathVariable Delivery.DeliveryStatus status) {
        try {
            List<DeliveryDTO> deliveries = deliveryService.getDeliveriesByStatus(status).stream()
                    .map(deliveryMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<DeliveryDTO>> getDeliveriesByTour(@PathVariable Long tourId) {
        try {
            List<DeliveryDTO> deliveries = deliveryService.getDeliveriesByTour(tourId).stream()
                    .map(deliveryMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<DeliveryDTO>> getUnassignedDeliveries() {
        try {
            List<DeliveryDTO> deliveries = deliveryService.getUnassignedDeliveries().stream()
                    .map(deliveryMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}