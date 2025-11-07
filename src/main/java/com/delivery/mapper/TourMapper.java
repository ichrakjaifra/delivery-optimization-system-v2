package com.delivery.mapper;

import com.delivery.dto.TourDTO;
import com.delivery.entity.Tour;
import com.delivery.service.VehicleService;
import com.delivery.service.WarehouseService;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class TourMapper {

    private final VehicleService vehicleService;
    private final WarehouseService warehouseService;

    public TourMapper(VehicleService vehicleService, WarehouseService warehouseService) {
        this.vehicleService = vehicleService;
        this.warehouseService = warehouseService;
    }

    public TourDTO toDTO(Tour tour) {
        if (tour == null) {
            return null;
        }

        TourDTO dto = new TourDTO();
        dto.setId(tour.getId());
        dto.setDate(tour.getDate());
        dto.setVehicleId(tour.getVehicle() != null ? tour.getVehicle().getId() : null);
        dto.setWarehouseId(tour.getWarehouse() != null ? tour.getWarehouse().getId() : null);
        dto.setAlgorithmUsed(tour.getAlgorithmUsed());
        dto.setTotalDistance(tour.getTotalDistance());
        dto.setDeliveryIds(tour.getDeliveries().stream()
                .map(delivery -> delivery.getId())
                .collect(Collectors.toList()));

        return dto;
    }

    public Tour toEntity(TourDTO dto) {
        if (dto == null) {
            return null;
        }

        Tour tour = new Tour();
        tour.setId(dto.getId());
        tour.setDate(dto.getDate());
        tour.setAlgorithmUsed(dto.getAlgorithmUsed());
        tour.setTotalDistance(dto.getTotalDistance());

        if (dto.getVehicleId() != null) {
            tour.setVehicle(vehicleService.getVehicleById(dto.getVehicleId())
                    .orElseThrow(() -> new NoSuchElementException("Vehicle not found with ID: " + dto.getVehicleId())));
        } else {
            tour.setVehicle(null);
        }


        if (dto.getWarehouseId() != null) {
            tour.setWarehouse(warehouseService.getWarehouseById(dto.getWarehouseId())
                    .orElseThrow(() -> new NoSuchElementException("Warehouse not found with ID: " + dto.getWarehouseId())));
        } else {
            tour.setWarehouse(null);
        }

        return tour;
    }
}