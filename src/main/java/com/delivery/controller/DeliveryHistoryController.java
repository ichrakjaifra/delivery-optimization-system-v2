package com.delivery.controller;

import com.delivery.entity.DeliveryHistory;
import com.delivery.service.DeliveryHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.delivery.dto.DeliveryHistoryDTO;
import com.delivery.mapper.DeliveryHistoryMapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivery-history")
@Tag(name = "Delivery History", description = "APIs for accessing delivery history and analytics")
public class DeliveryHistoryController {

    private final DeliveryHistoryService deliveryHistoryService;
    private final DeliveryHistoryMapper historyMapper;

    public DeliveryHistoryController(DeliveryHistoryService deliveryHistoryService, DeliveryHistoryMapper historyMapper) {
        this.deliveryHistoryService = deliveryHistoryService;
        this.historyMapper = historyMapper;
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get delivery history for a customer")
    public ResponseEntity<List<DeliveryHistoryDTO>> getCustomerDeliveryHistory(@PathVariable Long customerId) {
        try {
            List<DeliveryHistoryDTO> history = deliveryHistoryService.getCustomerDeliveryHistory(customerId).stream()
                    .map(historyMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tour/{tourId}")
    @Operation(summary = "Get delivery history for a tour")
    public ResponseEntity<List<DeliveryHistoryDTO>> getTourDeliveryHistory(@PathVariable Long tourId) { // ⬅️ تم التعديل هنا
        try {
            List<DeliveryHistoryDTO> history = deliveryHistoryService.getTourDeliveryHistory(tourId).stream()
                    .map(historyMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/delayed")
    @Operation(summary = "Get delayed deliveries")
    public ResponseEntity<List<DeliveryHistoryDTO>> getDelayedDeliveries(@RequestParam(defaultValue = "15") Integer minDelay) { // ⬅️ تعديل النوع
        try {
            List<DeliveryHistoryDTO> delayed = deliveryHistoryService.getDelayedDeliveries(minDelay).stream()
                    .map(historyMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(delayed);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/analytics/customer/{customerId}")
    @Operation(summary = "Get delivery analytics for a customer")
    public ResponseEntity<Object> getCustomerAnalytics(@PathVariable Long customerId) {
        try {
            List<DeliveryHistory> historyData = deliveryHistoryService.getCustomerDeliveryHistory(customerId);

            List<DeliveryHistoryDTO> historyDTOs = historyData.stream()
                    .map(historyMapper::toDTO)
                    .collect(Collectors.toList());

            // Calcul des métriques simples
            long totalCount = historyData.size();
            long onTimeCount = historyData.stream()
                    .filter(h -> h.getDelayMinutes() != null && h.getDelayMinutes() <= 0)
                    .count();
            double calculatedRate = totalCount > 0 ? (double) onTimeCount / totalCount * 100 : 0;

            var analytics = new Object() {
                public final long totalDeliveries = totalCount;
                public final long onTimeDeliveries = onTimeCount;
                public final double onTimeRate = calculatedRate;
                public final List<DeliveryHistoryDTO> history = historyDTOs;
            };

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/paged")
    @Operation(summary = "Get delivery history with pagination")
    public ResponseEntity<Page<DeliveryHistoryDTO>> getDeliveryHistoryPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "deliveryDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

            Page<DeliveryHistory> historyPage = deliveryHistoryService.getDeliveryHistoryPaged(pageable);

            Page<DeliveryHistoryDTO> dtoPage = historyPage.map(historyMapper::toDTO);

            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/analytics/summary")
    @Operation(summary = "Get delivery analytics summary")
    public ResponseEntity<Object> getAnalyticsSummary() {
        try {
            // Récupérer toutes les données pour l'analyse
            List<DeliveryHistory> allHistory = deliveryHistoryService.getAllDeliveryHistory();

            // Calculer les métriques globales
            long totalCount = allHistory.size();
            long delayedCount = allHistory.stream()
                    .filter(h -> h.getDelayMinutes() != null && h.getDelayMinutes() > 0)
                    .count();
            double calculatedDelayRate = totalCount > 0 ?
                    (double) delayedCount / totalCount * 100 : 0;

            // Calculer le retard moyen
            double calculatedAvgDelay = allHistory.stream()
                    .filter(h -> h.getDelayMinutes() != null && h.getDelayMinutes() > 0)
                    .mapToInt(DeliveryHistory::getDelayMinutes)
                    .average()
                    .orElse(0.0);

            var summary = new Object() {
                public final long totalDeliveries = totalCount;
                public final long delayedDeliveries = delayedCount;
                public final double delayRate = calculatedDelayRate;
                public final double averageDelayMinutes = calculatedAvgDelay;
                public final long onTimeDeliveries = totalCount - delayedCount;
            };

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}