package com.delivery.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnoreProperties({"tours"})
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({"tours"})
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlgorithmType algorithmUsed;

    @Column(nullable = false)
    private Double totalDistance; // en km

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TourStatus status = TourStatus.PLANNED;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("order ASC")
    @JsonIgnore
    private List<Delivery> deliveries = new ArrayList<>();

    public enum AlgorithmType {
        NEAREST_NEIGHBOR, CLARKE_WRIGHT, AI_OPTIMIZER
    }

    public enum TourStatus {
        PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public boolean isValidForVehicle() {
        if (this.vehicle == null || this.deliveries == null) {
            return false;
        }

        double totalWeight = this.deliveries.stream()
                .mapToDouble(Delivery::getWeight)
                .sum();

        double totalVolume = this.deliveries.stream()
                .mapToDouble(Delivery::getVolume)
                .sum();

        int deliveryCount = this.deliveries.size();

        return this.vehicle.isValidForDelivery(totalWeight, totalVolume, deliveryCount);
    }


    public Double getTotalWeight() {
        return this.deliveries.stream()
                .mapToDouble(Delivery::getWeight)
                .sum();
    }

    public Double getTotalVolume() {
        return this.deliveries.stream()
                .mapToDouble(Delivery::getVolume)
                .sum();
    }

    public Integer getDeliveryCount() {
        return this.deliveries.size();
    }


    public void validate() {
        if (this.date == null) {
            throw new IllegalArgumentException("La date est obligatoire");
        }
        if (this.vehicle == null) {
            throw new IllegalArgumentException("Le véhicule est obligatoire");
        }
        if (this.warehouse == null) {
            throw new IllegalArgumentException("L'entrepôt est obligatoire");
        }
        if (!isValidForVehicle()) {
            throw new IllegalArgumentException("Les livraisons dépassent la capacité du véhicule");
        }
    }
}