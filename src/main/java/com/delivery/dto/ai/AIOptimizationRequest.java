package com.delivery.dto.ai;

import com.delivery.entity.Delivery;
import com.delivery.entity.Warehouse;
import com.delivery.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIOptimizationRequest {
    private WarehouseData warehouse;
    private List<DeliveryData> deliveries;
    private VehicleData vehicle;
    private HistoricalData historicalData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseData {
        private Long id;
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private String openingHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryData {
        private Long id;
        private Double weight;
        private Double volume;
        private String preferredTimeSlot;
        private String address;
        private Double latitude;
        private Double longitude;
        private String customerName;
        private String customerTimeSlot;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleData {
        private String licensePlate;
        private String type;
        private Double maxWeight;
        private Double maxVolume;
        private Integer maxDeliveries;
        private Double range;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalData {
        private List<DeliveryHistory> deliveryHistory;
        private PerformanceMetrics performanceMetrics;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DeliveryHistory {
            private String customerName;
            private String address;
            private String deliveryDate;
            private String dayOfWeek;
            private Integer delayMinutes;
            private String plannedTime;
            private String actualTime;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PerformanceMetrics {
            private Double averageDelay;
            private Double onTimeRate;
            private String bestDayOfWeek;
            private String worstDayOfWeek;
            private List<ZonePerformance> zonePerformance;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ZonePerformance {
            private String zone;
            private Double averageDelay;
            private Integer deliveryCount;
        }
    }
}