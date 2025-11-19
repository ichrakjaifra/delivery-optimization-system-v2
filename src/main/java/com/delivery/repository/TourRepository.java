package com.delivery.repository;

import com.delivery.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    List<Tour> findByDate(LocalDate date);

    List<Tour> findByVehicleId(Long vehicleId);

    @Query("SELECT t FROM Tour t WHERE t.algorithmUsed = 'NEAREST_NEIGHBOR'")
    List<Tour> findToursWithNearestNeighbor();

    @Query("SELECT t FROM Tour t WHERE t.algorithmUsed = 'CLARKE_WRIGHT'")
    List<Tour> findToursWithClarkeWright();

    @Query("SELECT t FROM Tour t WHERE t.totalDistance > :minDistance")
    List<Tour> findToursWithDistanceGreaterThan(@Param("minDistance") Double minDistance);

    @Query("SELECT t FROM Tour t WHERE t.date = :date AND t.vehicle.id = :vehicleId")
    List<Tour> findByDateAndVehicleId(@Param("date") LocalDate date, @Param("vehicleId") Long vehicleId);
}