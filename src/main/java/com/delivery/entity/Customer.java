package com.delivery.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "preferred_time_slot", length = 20)
    private String preferredTimeSlot; // Format: "09:00-11:00"

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Delivery> deliveries = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DeliveryHistory> deliveryHistories = new ArrayList<>();

    // Validation
    private static final Pattern TIME_SLOT_PATTERN =
            Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");

    public void validate() {
        validateCoordinates();
        validateTimeSlot();
    }

    private void validateCoordinates() {
        if (this.latitude == null || this.latitude < -90 || this.latitude > 90) {
            throw new IllegalArgumentException("Latitude invalide (-90 à 90)");
        }
        if (this.longitude == null || this.longitude < -180 || this.longitude > 180) {
            throw new IllegalArgumentException("Longitude invalide (-180 à 180)");
        }
    }

    private void validateTimeSlot() {
        if (this.preferredTimeSlot != null && !this.preferredTimeSlot.isEmpty()) {
            if (!TIME_SLOT_PATTERN.matcher(this.preferredTimeSlot).matches()) {
                throw new IllegalArgumentException("Format de créneau horaire invalide. Utilisez: HH:MM-HH:MM");
            }
        }
    }
}