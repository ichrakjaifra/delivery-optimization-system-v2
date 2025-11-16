package com.delivery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryHistoryDTO {
    private Long id;

    private Long customerId;
    private Long deliveryId;
    private Long tourId;

    private String customerName;
    private String deliveryAddress;

    private LocalDate deliveryDate;
    private LocalDateTime plannedTime;
    private LocalDateTime actualTime;
    private Integer delayMinutes;
    private DayOfWeek dayOfWeek;
    private String notes;
}