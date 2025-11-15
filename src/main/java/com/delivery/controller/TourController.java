package com.delivery.controller;

import com.delivery.dto.TourDTO;
import com.delivery.entity.Delivery;
import com.delivery.entity.DeliveryHistory;
import com.delivery.entity.Tour;
import com.delivery.mapper.TourMapper;
import com.delivery.service.DeliveryHistoryService;
import com.delivery.service.TourService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;
    private final TourMapper tourMapper;
    private final DeliveryHistoryService deliveryHistoryService;

    public TourController(TourService tourService, TourMapper tourMapper, DeliveryHistoryService deliveryHistoryService) {
        this.tourService = tourService;
        this.tourMapper = tourMapper;
        this.deliveryHistoryService = deliveryHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<TourDTO>> getAllTours() {
        try {
            List<TourDTO> tours = tourService.getAllTours().stream()
                    .map(tourMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDTO> getTourById(@PathVariable Long id) {
        try {
            return tourService.getTourById(id)
                    .map(tourMapper::toDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<TourDTO> createTour(@RequestBody TourDTO tourDTO) {
        try {
            Tour tour = tourMapper.toEntity(tourDTO);
            Tour createdTour = tourService.createTour(tour);
            TourDTO createdDTO = tourMapper.toDTO(createdTour);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDTO> updateTour(@PathVariable Long id, @RequestBody TourDTO tourDTO) {
        try {
            Tour tour = tourMapper.toEntity(tourDTO);
            Tour updatedTour = tourService.updateTour(id, tour);
            TourDTO updatedDTO = tourMapper.toDTO(updatedTour);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        try {
            tourService.deleteTour(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/optimize")
    public ResponseEntity<TourDTO> optimizeTour(@PathVariable Long id, @RequestParam Tour.AlgorithmType algorithm) {
        try {
            Tour optimizedTour = tourService.optimizeTour(id, algorithm);
            TourDTO optimizedDTO = tourMapper.toDTO(optimizedTour);
            return ResponseEntity.ok(optimizedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}/optimized-route")
    public ResponseEntity<List<Delivery>> getOptimizedTour(@PathVariable Long id, @RequestParam Tour.AlgorithmType algorithm) {
        try {
            List<Delivery> optimizedRoute = tourService.getOptimizedTour(id, algorithm);
            return ResponseEntity.ok(optimizedRoute);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}/total-distance")
    public ResponseEntity<Double> getTotalDistance(@PathVariable Long id, @RequestParam Tour.AlgorithmType algorithm) {
        try {
            Double distance = tourService.getTotalDistance(id, algorithm);
            return ResponseEntity.ok(distance);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<TourDTO>> getToursByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<TourDTO> tours = tourService.getToursByDate(date).stream()
                    .map(tourMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<TourDTO>> getToursByVehicle(@PathVariable Long vehicleId) {
        try {
            List<TourDTO> tours = tourService.getToursByVehicle(vehicleId).stream()
                    .map(tourMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/algorithm/nearest-neighbor")
    public ResponseEntity<List<TourDTO>> getToursWithNearestNeighbor() {
        try {
            List<TourDTO> tours = tourService.getToursWithNearestNeighbor().stream()
                    .map(tourMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/algorithm/clarke-wright")
    public ResponseEntity<List<TourDTO>> getToursWithClarkeWright() {
        try {
            List<TourDTO> tours = tourService.getToursWithClarkeWright().stream()
                    .map(tourMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/{tourId}/deliveries/{deliveryId}")
    public ResponseEntity<Void> addDeliveryToTour(@PathVariable Long tourId, @PathVariable Long deliveryId) {
        try {
            tourService.addDeliveryToTour(tourId, deliveryId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{tourId}/deliveries/{deliveryId}")
    public ResponseEntity<Void> removeDeliveryFromTour(@PathVariable Long tourId, @PathVariable Long deliveryId) {
        try {
            tourService.removeDeliveryFromTour(tourId, deliveryId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple tours in batch")
    public ResponseEntity<List<TourDTO>> createToursBatch(@RequestBody List<TourDTO> tourDTOs) {
        try {
            // Convertir les DTOs en entités
            List<Tour> tours = tourDTOs.stream()
                    .map(tourMapper::toEntity)
                    .collect(Collectors.toList());

            // Créer les tournées en batch
            List<Tour> createdTours = tourService.createToursBatch(tours);

            // Convertir en DTOs pour la réponse
            List<TourDTO> createdDTOs = createdTours.stream()
                    .map(tourMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{tourId}/status")
    @Operation(summary = "Update tour status and automatically generate delivery history when completed")
    public ResponseEntity<TourDTO> updateTourStatus(
            @PathVariable Long tourId,
            @RequestParam Tour.TourStatus newStatus) {
        try {
            Tour updatedTour = tourService.updateTourStatus(tourId, newStatus);
            TourDTO updatedDTO = tourMapper.toDTO(updatedTour);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{tourId}/history/exists")
    @Operation(summary = "Check if delivery history exists for a tour")
    public ResponseEntity<Boolean> checkHistoryExists(@PathVariable Long tourId) {
        try {
            boolean exists = deliveryHistoryService.existsHistoryForTour(tourId);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{tourId}/history")
    @Operation(summary = "Get delivery history for a tour")
    public ResponseEntity<List<DeliveryHistory>> getTourHistory(@PathVariable Long tourId) {
        try {
            List<DeliveryHistory> history = deliveryHistoryService.getTourDeliveryHistory(tourId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}