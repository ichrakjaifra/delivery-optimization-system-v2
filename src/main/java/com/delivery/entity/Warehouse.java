package com.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

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

    @Column(nullable = false, length = 20)
    private String openingHours; // Format: "06:00-22:00"

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tour> tours = new ArrayList<>();


    private static final Pattern OPENING_HOURS_PATTERN =
            Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");

    public void validate() {
        validateCoordinates();
        validateOpeningHours();
    }

    private void validateCoordinates() {
        if (this.latitude == null || this.latitude < -90 || this.latitude > 90) {
            throw new IllegalArgumentException("Latitude invalide (-90 à 90)");
        }
        if (this.longitude == null || this.longitude < -180 || this.longitude > 180) {
            throw new IllegalArgumentException("Longitude invalide (-180 à 180)");
        }
    }

    private void validateOpeningHours() {
        if (this.openingHours == null || this.openingHours.isEmpty()) {
            throw new IllegalArgumentException("Les horaires d'ouverture sont obligatoires");
        }
        if (!OPENING_HOURS_PATTERN.matcher(this.openingHours).matches()) {
            throw new IllegalArgumentException("Format d'horaire invalide. Utilisez: HH:MM-HH:MM");
        }
    }
}