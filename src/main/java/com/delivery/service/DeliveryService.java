package com.delivery.service;

import com.delivery.entity.Customer;
import com.delivery.entity.Delivery;
import com.delivery.repository.CustomerRepository;
import com.delivery.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@Transactional
public class DeliveryService {

    private static final Logger logger = Logger.getLogger(DeliveryService.class.getName());

    private final DeliveryRepository deliveryRepository;
    private final CustomerRepository customerRepository;

    public DeliveryService(DeliveryRepository deliveryRepository, CustomerRepository customerRepository) {
        this.deliveryRepository = deliveryRepository;
        this.customerRepository = customerRepository;
    }

    public List<Delivery> getAllDeliveries() {
        logger.info("Fetching all deliveries");
        return deliveryRepository.findAll();
    }

    public Optional<Delivery> getDeliveryById(Long id) {
        logger.info("Fetching delivery with id: " + id);
        return deliveryRepository.findById(id);
    }

    @Transactional
    public Delivery createDelivery(Delivery delivery, Long customerId) {
        logger.info("Creating new delivery: " + delivery.getAddress());

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        delivery.setCustomer(customer);

        try {
            delivery.validate();
            logger.info("Contraintes validées pour la livraison: " + delivery.getAddress());
        } catch (IllegalArgumentException e) {
            logger.severe("Erreur validation livraison: " + e.getMessage());
            throw new RuntimeException("Erreur de validation: " + e.getMessage());
        }

        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery updateDelivery(Long id, Delivery deliveryDetails, Long customerId) {
        logger.info("Updating delivery with id: " + id);

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
            deliveryDetails.setCustomer(customer);
        }

        try {
            deliveryDetails.validate();
            logger.info("Contraintes validées pour la mise à jour livraison: " + deliveryDetails.getAddress());
        } catch (IllegalArgumentException e) {
            logger.severe("Erreur validation livraison: " + e.getMessage());
            throw new RuntimeException("Erreur de validation: " + e.getMessage());
        }

        Optional<Delivery> deliveryOpt = deliveryRepository.findById(id);
        if (deliveryOpt.isPresent()) {
            Delivery delivery = deliveryOpt.get();
            delivery.setWeight(deliveryDetails.getWeight());
            delivery.setVolume(deliveryDetails.getVolume());
            delivery.setPreferredTimeSlot(deliveryDetails.getPreferredTimeSlot());
            delivery.setStatus(deliveryDetails.getStatus());
            delivery.setOrder(deliveryDetails.getOrder());

            if (customerId != null) {
                delivery.setCustomer(deliveryDetails.getCustomer());
            }

            return deliveryRepository.save(delivery);
        }
        throw new RuntimeException("Delivery not found with id: " + id);
    }

    @Transactional
    public void deleteDelivery(Long id) {
        logger.info("Deleting delivery with id: " + id);
        if (deliveryRepository.existsById(id)) {
            deliveryRepository.deleteById(id);
        } else {
            throw new RuntimeException("Delivery not found with id: " + id);
        }
    }

    public List<Delivery> getDeliveriesByStatus(Delivery.DeliveryStatus status) {
        logger.info("Fetching deliveries with status: " + status);
        return deliveryRepository.findByStatus(status);
    }

    public List<Delivery> getDeliveriesByTour(Long tourId) {
        logger.info("Fetching deliveries for tour id: " + tourId);
        return deliveryRepository.findByTourIdOrderByOrderAsc(tourId);
    }

    public List<Delivery> getUnassignedDeliveries() {
        logger.info("Fetching unassigned deliveries");
        return deliveryRepository.findUnassignedDeliveries();
    }

    public List<Delivery> getPendingUnassignedDeliveries() {
        logger.info("Fetching pending unassigned deliveries");
        return deliveryRepository.findPendingUnassignedDeliveries();
    }

    public List<Delivery> getDeliveriesByCustomer(Long customerId) {
        logger.info("Fetching deliveries for customer id: " + customerId);
        return deliveryRepository.findByCustomerId(customerId);
    }
}