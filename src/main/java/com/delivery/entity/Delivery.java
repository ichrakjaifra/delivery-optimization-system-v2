package com.delivery.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double weight; // en kg

    @Column(nullable = false)
    private Double volume; // en m³

    @Column(name = "preferred_time_slot", length = 20)
    private String preferredTimeSlot; // Hérité du customer mais peut être overridé

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    @JsonIgnoreProperties({"deliveries", "vehicle", "warehouse"})
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"deliveries"})
    private Customer customer;

    @Column(name = "delivery_order")
    private Integer order; // Ordre dans la tournée

    public enum DeliveryStatus {
        PENDING, IN_TRANSIT, DELIVERED, FAILED
    }

    // Méthodes pratiques pour accéder aux informations du customer
    public String getAddress() {
        return customer != null ? customer.getAddress() : null;
    }

    public Double getLatitude() {
        return customer != null ? customer.getLatitude() : null;
    }

    public Double getLongitude() {
        return customer != null ? customer.getLongitude() : null;
    }

    public String getCustomerPreferredTimeSlot() {
        return customer != null ? customer.getPreferredTimeSlot() : null;
    }

    // Validation modifiée
    public void validate() {
        validateWeight();
        validateVolume();
        validateTimeSlot();
        validateCustomer();
    }

    private void validateWeight() {
        if (this.weight == null || this.weight <= 0) {
            throw new IllegalArgumentException("Le poids doit être positif");
        }
        if (this.weight > 1000) {
            throw new IllegalArgumentException("Le poids ne peut pas dépasser 1000kg");
        }
    }

    private void validateVolume() {
        if (this.volume == null || this.volume <= 0) {
            throw new IllegalArgumentException("Le volume doit être positif");
        }
        if (this.volume > 10) {
            throw new IllegalArgumentException("Le volume ne peut pas dépasser 10m³");
        }
    }

    private void validateTimeSlot() {
        String timeSlotToValidate = this.preferredTimeSlot != null ?
                this.preferredTimeSlot : getCustomerPreferredTimeSlot();

        if (timeSlotToValidate != null && !timeSlotToValidate.isEmpty()) {
            Pattern TIME_SLOT_PATTERN = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
            if (!TIME_SLOT_PATTERN.matcher(timeSlotToValidate).matches()) {
                throw new IllegalArgumentException("Format de créneau horaire invalide. Utilisez: HH:MM-HH:MM");
            }
        }
    }

    private void validateCustomer() {
        if (this.customer == null) {
            throw new IllegalArgumentException("Le client est obligatoire");
        }
        this.customer.validate();
    }
}