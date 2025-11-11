package com.delivery.controller;

import com.delivery.entity.DeliveryHistory;
import com.delivery.service.DeliveryHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-history")
@Tag(name = "Delivery History", description = "APIs for accessing delivery history and analytics")
public class DeliveryHistoryController {

    private final DeliveryHistoryService deliveryHistoryService;

    public DeliveryHistoryController(DeliveryHistoryService deliveryHistoryService) {
        this.deliveryHistoryService = deliveryHistoryService;
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get delivery history for a customer")
    public ResponseEntity<List<DeliveryHistory>> getCustomerDeliveryHistory(@PathVariable Long customerId) {
        try {
            List<DeliveryHistory> history = deliveryHistoryService.getCustomerDeliveryHistory(customerId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tour/{tourId}")
    @Operation(summary = "Get delivery history for a tour")
    public ResponseEntity<List<DeliveryHistory>> getTourDeliveryHistory(@PathVariable Long tourId) {
        try {
            List<DeliveryHistory> history = deliveryHistoryService.getTourDeliveryHistory(tourId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/delayed")
    @Operation(summary = "Get delayed deliveries")
    public ResponseEntity<List<DeliveryHistory>> getDelayedDeliveries(@RequestParam(defaultValue = "15") Integer minDelay) {
        try {
            List<DeliveryHistory> delayed = deliveryHistoryService.getDelayedDeliveries(minDelay);
            return ResponseEntity.ok(delayed);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/analytics/customer/{customerId}")
    @Operation(summary = "Get delivery analytics for a customer")
    public ResponseEntity<Object> getCustomerAnalytics(@PathVariable Long customerId) {
        try {
            // Cette méthode pourrait retourner des statistiques agrégées
            // Pour l'instant, retournons l'historique avec des métriques
            List<DeliveryHistory> history = deliveryHistoryService.getCustomerDeliveryHistory(customerId);

            // Calcul des métriques simples
            long totalDeliveries = history.size();
            long onTimeDeliveries = history.stream()
                    .filter(h -> h.getDelayMinutes() != null && h.getDelayMinutes() <= 0)
                    .count();
            double onTimeRate = totalDeliveries > 0 ? (double) onTimeDeliveries / totalDeliveries * 100 : 0;

            var analytics = new Object() {
                public final long totalDeliveries = totalDeliveries;
                public final long onTimeDeliveries = onTimeDeliveries;
                public final double onTimeRate = onTimeRate;
                public final List<DeliveryHistory> history = history;
            };

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/paged")
    @Operation(summary = "Get delivery history with pagination")
    public ResponseEntity<Page<DeliveryHistory>> getDeliveryHistoryPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<DeliveryHistory> history = deliveryHistoryService.getDeliveryHistoryPaged(pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}