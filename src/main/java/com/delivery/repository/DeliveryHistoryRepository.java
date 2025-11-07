package com.delivery.repository;

import com.delivery.entity.DeliveryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, Long> {

    List<DeliveryHistory> findByCustomerId(Long customerId);

    List<DeliveryHistory> findByTourId(Long tourId);

    List<DeliveryHistory> findByDeliveryDate(LocalDate deliveryDate);

    @Query("SELECT dh FROM DeliveryHistory dh WHERE dh.customer.id IN :customerIds")
    List<DeliveryHistory> findByCustomerIdIn(@Param("customerIds") List<Long> customerIds);

    @Query("SELECT dh FROM DeliveryHistory dh WHERE dh.delayMinutes > :minDelay")
    List<DeliveryHistory> findDelaysGreaterThan(@Param("minDelay") Integer minDelay);
}