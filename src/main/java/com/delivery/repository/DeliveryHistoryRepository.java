package com.delivery.repository;

import com.delivery.entity.DeliveryHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, Long> {

    // Méthodes dérivées
    List<DeliveryHistory> findByCustomerId(Long customerId);
    List<DeliveryHistory> findByTourId(Long tourId);

    // Requêtes personnalisées
    @Query("SELECT dh FROM DeliveryHistory dh WHERE dh.delayMinutes > :minDelay")
    List<DeliveryHistory> findDelaysGreaterThan(@Param("minDelay") Integer minDelay);

    @Query("SELECT dh FROM DeliveryHistory dh WHERE dh.customer.id = :customerId ORDER BY dh.deliveryDate DESC")
    Page<DeliveryHistory> findByCustomerIdPaged(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT dh FROM DeliveryHistory dh WHERE " +
            "dh.deliveryDate BETWEEN :startDate AND :endDate")
    List<DeliveryHistory> findByDeliveryDateRange(@Param("startDate") java.time.LocalDate startDate,
                                                  @Param("endDate") java.time.LocalDate endDate);

    // Statistiques avancées
    @Query("SELECT AVG(dh.delayMinutes) FROM DeliveryHistory dh WHERE dh.delayMinutes > 0")
    Double findAverageDelay();

    @Query("SELECT dh.dayOfWeek, AVG(dh.delayMinutes) FROM DeliveryHistory dh " +
            "WHERE dh.delayMinutes IS NOT NULL " +
            "GROUP BY dh.dayOfWeek " +
            "ORDER BY AVG(dh.delayMinutes) DESC")
    List<Object[]> findAverageDelayByDayOfWeek();
}