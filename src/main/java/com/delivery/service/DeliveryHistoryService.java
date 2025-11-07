package com.delivery.service;

import com.delivery.entity.DeliveryHistory;
import com.delivery.entity.Tour;
import com.delivery.entity.Delivery;
import com.delivery.repository.DeliveryHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
@Transactional
public class DeliveryHistoryService {

    private static final Logger logger = Logger.getLogger(DeliveryHistoryService.class.getName());

    private final DeliveryHistoryRepository deliveryHistoryRepository;

    public DeliveryHistoryService(DeliveryHistoryRepository deliveryHistoryRepository) {
        this.deliveryHistoryRepository = deliveryHistoryRepository;
    }

    public void createDeliveryHistoryFromCompletedTour(Tour tour) {
        logger.info("Creating delivery history for completed tour: " + tour.getId());

        if (tour.getDeliveries() != null) {
            for (Delivery delivery : tour.getDeliveries()) {
                if (delivery.getCustomer() != null) {
                    DeliveryHistory history = new DeliveryHistory();
                    history.setCustomer(delivery.getCustomer());
                    history.setDelivery(delivery);
                    history.setTour(tour);
                    history.setDeliveryDate(tour.getDate());
                    history.setPlannedTime(calculatePlannedTime(tour, delivery));
                    history.setActualTime(LocalDateTime.now());
                    history.setDayOfWeekFromDate();
                    history.calculateDelay();

                    deliveryHistoryRepository.save(history);
                }
            }
        }
    }

    public List<DeliveryHistory> getCustomerDeliveryHistory(Long customerId) {
        logger.info("Fetching delivery history for customer: " + customerId);
        return deliveryHistoryRepository.findByCustomerId(customerId);
    }

    public List<DeliveryHistory> getTourDeliveryHistory(Long tourId) {
        logger.info("Fetching delivery history for tour: " + tourId);
        return deliveryHistoryRepository.findByTourId(tourId);
    }

    public List<DeliveryHistory> getDelayedDeliveries(Integer minDelay) {
        logger.info("Fetching deliveries with delay greater than: " + minDelay + " minutes");
        return deliveryHistoryRepository.findDelaysGreaterThan(minDelay);
    }

    private LocalDateTime calculatePlannedTime(Tour tour, Delivery delivery) {
        // Logique simplifiée pour calculer le temps planifié
        // En réalité, cela devrait être basé sur l'ordre dans la tournée et la distance
        return tour.getDate().atTime(9, 0).plusMinutes(delivery.getOrder() * 30L);
    }
}