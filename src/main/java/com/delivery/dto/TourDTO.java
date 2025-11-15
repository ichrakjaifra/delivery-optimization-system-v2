package com.delivery.dto;

import com.delivery.entity.Tour;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourDTO {
    private Long id;
    private LocalDate date;
    private Long vehicleId;
    private Long warehouseId;
    private Tour.AlgorithmType algorithmUsed;
    private Double totalDistance;
    private Tour.TourStatus status;
    private List<Long> deliveryIds = new ArrayList<>();
}