package com.delivery.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIOptimizationResponse {
    private boolean success;
    private String message;
    private OptimizationResult result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationResult {
        private List<OptimizedDelivery> optimizedRoute;
        private RouteSummary summary;
        private List<Recommendation> recommendations;
        private List<Prediction> predictions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizedDelivery {
        private Long deliveryId;
        private Integer order;
        private String customerName;
        private String address;
        private String estimatedArrivalTime;
        private Double distanceFromPrevious;
        private String timeSlot;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteSummary {
        private Double totalDistance;
        private Integer totalDeliveries;
        private Double estimatedTotalTime;
        private Double averageSpeed;
        private String recommendedStartTime;
        private String estimatedCompletionTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String type; // TIME_OPTIMIZATION, ROUTE_OPTIMIZATION, CAPACITY_OPTIMIZATION
        private String description;
        private String impact; // HIGH, MEDIUM, LOW
        private String suggestion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prediction {
        private String type; // DELAY_RISK, TRAFFIC_PREDICTION, WEATHER_IMPACT
        private String description;
        private String probability; // HIGH, MEDIUM, LOW
        private String mitigation;
    }
}