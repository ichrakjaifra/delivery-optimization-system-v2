package com.delivery.service;

import com.delivery.entity.DeliveryHistory;
import com.delivery.entity.Tour;
import com.delivery.entity.Delivery;
import com.delivery.repository.DeliveryHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                    history.setNotes("Tour completed on " + LocalDateTime.now());

                    deliveryHistoryRepository.save(history);

                    logger.info("Created delivery history for delivery: " + delivery.getId());
                }
            }
        }
        logger.info("Completed creating delivery history for tour: " + tour.getId());
    }

    public boolean existsHistoryForTour(Long tourId) {
        return !deliveryHistoryRepository.findByTourId(tourId).isEmpty();
    }

    @Transactional
    public void deleteHistoryForTour(Long tourId) {
        List<DeliveryHistory> historyList = deliveryHistoryRepository.findByTourId(tourId);
        if (!historyList.isEmpty()) {
            deliveryHistoryRepository.deleteAll(historyList);
            logger.info("Deleted " + historyList.size() + " history records for tour: " + tourId);
        }
    }

    /*public List<DeliveryHistory> getCustomerDeliveryHistory(Long customerId) {
        logger.info("Fetching delivery history for customer: " + customerId);
        return deliveryHistoryRepository.findByCustomerId(customerId);
    }*/

    /*public List<DeliveryHistory> getTourDeliveryHistory(Long tourId) {
        logger.info("Fetching delivery history for tour: " + tourId);
        return deliveryHistoryRepository.findByTourId(tourId);
    }*/

    public List<DeliveryHistory> getDelayedDeliveries(Integer minDelay) {
        logger.info("Fetching deliveries with delay greater than: " + minDelay + " minutes");
        return deliveryHistoryRepository.findDelaysGreaterThan(minDelay);
    }

    public Page<DeliveryHistory> getDeliveryHistoryPaged(Pageable pageable) {
        logger.info("Fetching delivery history with pagination");
        return deliveryHistoryRepository.findAll(pageable);
    }


    /* public List<DeliveryHistory> getAllDeliveryHistory() {
        logger.info("Fetching all delivery history");
        return deliveryHistoryRepository.findAll();
    }*/

    private LocalDateTime calculatePlannedTime(Tour tour, Delivery delivery) {
        // Logique améliorée pour calculer le temps planifié
        if (delivery.getOrder() != null) {
            // Basé sur l'ordre dans la tournée (30 minutes par livraison)
            return tour.getDate().atTime(8, 0).plusMinutes(delivery.getOrder() * 30L);
        } else {
            // Ordre par défaut si non spécifié
            return tour.getDate().atTime(9, 0);
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


    public List<DeliveryHistory> getAllDeliveryHistory() {
        return deliveryHistoryRepository.findAll();
    }

}