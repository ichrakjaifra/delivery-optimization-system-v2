package com.delivery.service;

import com.delivery.entity.*;
import com.delivery.optimizer.TourOptimizer;
import com.delivery.repository.TourRepository;
import com.delivery.repository.DeliveryRepository;
import com.delivery.repository.VehicleRepository;
import com.delivery.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@Transactional
public class TourService {

    private static final Logger logger = Logger.getLogger(TourService.class.getName());

    private final TourRepository tourRepository;
    private final DeliveryRepository deliveryRepository;
    private final VehicleRepository vehicleRepository;
    private final WarehouseRepository warehouseRepository;
    private final TourOptimizer nearestNeighborOptimizer;
    private final TourOptimizer clarkeWrightOptimizer;

    public TourService(TourRepository tourRepository, DeliveryRepository deliveryRepository,
                       VehicleRepository vehicleRepository, WarehouseRepository warehouseRepository,
                       TourOptimizer nearestNeighborOptimizer, TourOptimizer clarkeWrightOptimizer) {
        this.tourRepository = tourRepository;
        this.deliveryRepository = deliveryRepository;
        this.vehicleRepository = vehicleRepository;
        this.warehouseRepository = warehouseRepository;
        this.nearestNeighborOptimizer = nearestNeighborOptimizer;
        this.clarkeWrightOptimizer = clarkeWrightOptimizer;
    }

    public List<Tour> getAllTours() {
        logger.info("Fetching all tours");
        return tourRepository.findAll();
    }

    public Optional<Tour> getTourById(Long id) {
        logger.info("Fetching tour with id: " + id);
        return tourRepository.findById(id);
    }

    @Transactional
    public Tour createTour(Tour tour) {
        logger.info("Creating new tour for date: " + tour.getDate());

        // Vérifier si le véhicule existe
        if (tour.getVehicle() != null && tour.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(tour.getVehicle().getId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + tour.getVehicle().getId()));
            tour.setVehicle(vehicle);
        }

        // Vérifier si l'entrepôt existe
        if (tour.getWarehouse() != null && tour.getWarehouse().getId() != null) {
            Warehouse warehouse = warehouseRepository.findById(tour.getWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + tour.getWarehouse().getId()));
            tour.setWarehouse(warehouse);
        }

        try {
            tour.validate();
            logger.info("Contraintes validées pour la tournée du: " + tour.getDate());
        } catch (IllegalArgumentException e) {
            logger.severe("Erreur validation tournée: " + e.getMessage());
            throw new RuntimeException("Erreur de validation: " + e.getMessage());
        }

        return tourRepository.save(tour);
    }

    @Transactional
    public Tour optimizeTour(Long tourId, Tour.AlgorithmType algorithmType) {
        logger.info("Optimizing tour " + tourId + " with algorithm: " + algorithmType);

        Optional<Tour> tourOpt = tourRepository.findById(tourId);
        if (tourOpt.isEmpty()) {
            throw new RuntimeException("Tour not found with id: " + tourId);
        }

        Tour tour = tourOpt.get();
        List<Delivery> deliveries = tour.getDeliveries();
        Warehouse warehouse = tour.getWarehouse();
        Vehicle vehicle = tour.getVehicle();

        if (deliveries.isEmpty()) {
            throw new RuntimeException("No deliveries found for tour id: " + tourId);
        }

        double totalWeight = deliveries.stream().mapToDouble(Delivery::getWeight).sum();
        double totalVolume = deliveries.stream().mapToDouble(Delivery::getVolume).sum();
        int deliveryCount = deliveries.size();

        if (!vehicle.isValidForDelivery(totalWeight, totalVolume, deliveryCount)) {
            String errorMsg = String.format(
                    "Le véhicule %s ne peut pas transporter %d livraisons (Poids: %.1fkg/%.1fkg, Volume: %.2fm³/%.2fm³)",
                    vehicle.getLicensePlate(), deliveryCount, totalWeight, vehicle.getMaxWeight(),
                    totalVolume, vehicle.getMaxVolume());
            logger.severe(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        TourOptimizer optimizer = algorithmType == Tour.AlgorithmType.NEAREST_NEIGHBOR ?
                nearestNeighborOptimizer : clarkeWrightOptimizer;

        List<Delivery> optimizedDeliveries = optimizer.calculateOptimalTour(warehouse, deliveries, vehicle);
        Double totalDistance = optimizer.calculateTotalDistance(warehouse, optimizedDeliveries);

        // Mettre à jour les livraisons avec le nouvel ordre
        for (Delivery delivery : optimizedDeliveries) {
            delivery.setTour(tour);
            deliveryRepository.save(delivery);
        }

        tour.setDeliveries(optimizedDeliveries);
        tour.setAlgorithmUsed(algorithmType);
        tour.setTotalDistance(totalDistance);

        logger.info("Optimization completed for tour " + tourId + " - Distance: " + totalDistance + "km");

        return tourRepository.save(tour);
    }

    public List<Delivery> getOptimizedTour(Long tourId, Tour.AlgorithmType algorithmType) {
        logger.info("Getting optimized tour for tour " + tourId + " with algorithm: " + algorithmType);

        Optional<Tour> tourOpt = tourRepository.findById(tourId);
        if (tourOpt.isEmpty()) {
            throw new RuntimeException("Tour not found with id: " + tourId);
        }

        Tour tour = tourOpt.get();
        List<Delivery> deliveries = tour.getDeliveries();
        Warehouse warehouse = tour.getWarehouse();
        Vehicle vehicle = tour.getVehicle();

        TourOptimizer optimizer = algorithmType == Tour.AlgorithmType.NEAREST_NEIGHBOR ?
                nearestNeighborOptimizer : clarkeWrightOptimizer;

        return optimizer.calculateOptimalTour(warehouse, deliveries, vehicle);
    }

    public Double getTotalDistance(Long tourId, Tour.AlgorithmType algorithmType) {
        logger.info("Calculating total distance for tour " + tourId + " with algorithm: " + algorithmType);

        Optional<Tour> tourOpt = tourRepository.findById(tourId);
        if (tourOpt.isEmpty()) {
            throw new RuntimeException("Tour not found with id: " + tourId);
        }

        Tour tour = tourOpt.get();
        List<Delivery> deliveries = tour.getDeliveries();
        Warehouse warehouse = tour.getWarehouse();

        TourOptimizer optimizer = algorithmType == Tour.AlgorithmType.NEAREST_NEIGHBOR ?
                nearestNeighborOptimizer : clarkeWrightOptimizer;

        return optimizer.calculateTotalDistance(warehouse, deliveries);
    }

    public List<Tour> getToursByDate(LocalDate date) {
        logger.info("Fetching tours for date: " + date);
        return tourRepository.findByDate(date);
    }

    public List<Tour> getToursByVehicle(Long vehicleId) {
        logger.info("Fetching tours for vehicle id: " + vehicleId);
        return tourRepository.findByVehicleId(vehicleId);
    }

    public List<Tour> getToursWithNearestNeighbor() {
        logger.info("Fetching tours optimized with Nearest Neighbor");
        return tourRepository.findToursWithNearestNeighbor();
    }

    public List<Tour> getToursWithClarkeWright() {
        logger.info("Fetching tours optimized with Clarke & Wright");
        return tourRepository.findToursWithClarkeWright();
    }

    @Transactional
    public void addDeliveryToTour(Long tourId, Long deliveryId) {
        logger.info("Adding delivery " + deliveryId + " to tour " + tourId);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + tourId));

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        delivery.setTour(tour);
        deliveryRepository.save(delivery);
    }

    @Transactional
    public void removeDeliveryFromTour(Long tourId, Long deliveryId) {
        logger.info("Removing delivery " + deliveryId + " from tour " + tourId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        if (delivery.getTour() == null || !delivery.getTour().getId().equals(tourId)) {
            throw new RuntimeException("Delivery " + deliveryId + " is not assigned to tour " + tourId);
        }

        delivery.setTour(null);
        delivery.setOrder(null);
        deliveryRepository.save(delivery);
    }

    @Transactional
    public Tour updateTour(Long id, Tour tourDetails) {
        logger.info("Updating tour with id: " + id);

        Optional<Tour> tourOpt = tourRepository.findById(id);
        if (tourOpt.isEmpty()) {
            throw new RuntimeException("Tour not found with id: " + id);
        }

        Tour tour = tourOpt.get();

        // Mettre à jour les champs modifiables
        if (tourDetails.getDate() != null) {
            tour.setDate(tourDetails.getDate());
        }

        if (tourDetails.getAlgorithmUsed() != null) {
            tour.setAlgorithmUsed(tourDetails.getAlgorithmUsed());
        }

        if (tourDetails.getTotalDistance() != null) {
            tour.setTotalDistance(tourDetails.getTotalDistance());
        }

        // Mettre à jour le véhicule si fourni
        if (tourDetails.getVehicle() != null && tourDetails.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(tourDetails.getVehicle().getId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + tourDetails.getVehicle().getId()));
            tour.setVehicle(vehicle);
        }

        // Mettre à jour l'entrepôt si fourni
        if (tourDetails.getWarehouse() != null && tourDetails.getWarehouse().getId() != null) {
            Warehouse warehouse = warehouseRepository.findById(tourDetails.getWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + tourDetails.getWarehouse().getId()));
            tour.setWarehouse(warehouse);
        }

        try {
            tour.validate();
            logger.info("Tour updated successfully for id: " + id);
        } catch (IllegalArgumentException e) {
            logger.severe("Validation error while updating tour: " + e.getMessage());
            throw new RuntimeException("Erreur de validation: " + e.getMessage());
        }

        return tourRepository.save(tour);
    }

    @Transactional
    public void deleteTour(Long id) {
        logger.info("Deleting tour with id: " + id);

        Optional<Tour> tourOpt = tourRepository.findById(id);
        if (tourOpt.isEmpty()) {
            throw new RuntimeException("Tour not found with id: " + id);
        }

        Tour tour = tourOpt.get();

        // Désassocier les livraisons de cette tournée
        if (!tour.getDeliveries().isEmpty()) {
            for (Delivery delivery : tour.getDeliveries()) {
                delivery.setTour(null);
                delivery.setOrder(null);
                deliveryRepository.save(delivery);
            }
            tour.getDeliveries().clear();
        }

        tourRepository.delete(tour);
        logger.info("Tour deleted successfully with id: " + id);
    }
}