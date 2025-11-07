package com.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

@Entity
@Table(name = "delivery_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "planned_time")
    private LocalDateTime plannedTime;

    @Column(name = "actual_time")
    private LocalDateTime actualTime;

    @Column(name = "delay_minutes")
    private Integer delayMinutes; // actualTime - plannedTime (en minutes)

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(length = 500)
    private String notes;

    // Méthode pour calculer automatiquement le retard
    public void calculateDelay() {
        if (this.plannedTime != null && this.actualTime != null) {
            this.delayMinutes = (int) java.time.Duration.between(this.plannedTime, this.actualTime).toMinutes();
        }
    }

    // Méthode pour définir le jour de la semaine automatiquement
    public void setDayOfWeekFromDate() {
        if (this.deliveryDate != null) {
            this.dayOfWeek = this.deliveryDate.getDayOfWeek();
        }
    }
}