package com.delivery.service;

import com.delivery.entity.Vehicle;
import com.delivery.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@Transactional
public class VehicleService {

    private static final Logger logger = Logger.getLogger(VehicleService.class.getName());

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAllVehicles() {
        logger.info("Fetching all vehicles");
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        logger.info("Fetching vehicle with id: " + id);
        return vehicleRepository.findById(id);
    }

    @Transactional
    public Vehicle createVehicle(Vehicle vehicle) {
        logger.info("Creating new vehicle: " + vehicle.getLicensePlate());

        try {
            vehicle.validateConstraints();
            logger.info("Contraintes validées pour: " + vehicle.getLicensePlate());
        } catch (IllegalArgumentException e) {
            logger.severe("Erreur contraintes véhicule: " + e.getMessage());
            throw new RuntimeException("Erreur de validation: " + e.getMessage());
        }

        // Vérifier si la plaque d'immatriculation existe déjà
        Vehicle existing = vehicleRepository.findByLicensePlate(vehicle.getLicensePlate());
        if (existing != null) {
            throw new RuntimeException("Vehicle with license plate " + vehicle.getLicensePlate() + " already exists");
        }

        return vehicleRepository.save(vehicle);
    }

    // NOUVELLE MÉTHODE POUR LE BATCH
    @Transactional
    public List<Vehicle> createVehiclesBatch(List<Vehicle> vehicles) {
        logger.info("Creating " + vehicles.size() + " vehicles in batch");

        List<Vehicle> createdVehicles = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            try {
                vehicle.validateConstraints();
                logger.info("Contraintes validées pour: " + vehicle.getLicensePlate());

                // Vérifier si la plaque d'immatriculation existe déjà
                Vehicle existing = vehicleRepository.findByLicensePlate(vehicle.getLicensePlate());
                if (existing != null) {
                    logger.warning("Vehicle with license plate " + vehicle.getLicensePlate() + " already exists. Skipping.");
                    continue;
                }

                Vehicle savedVehicle = vehicleRepository.save(vehicle);
                createdVehicles.add(savedVehicle);
                logger.info("Successfully created vehicle: " + vehicle.getLicensePlate());

            } catch (IllegalArgumentException e) {
                logger.severe("Erreur contraintes véhicule " + vehicle.getLicensePlate() + ": " + e.getMessage());
                // Continuer avec les autres véhicules même en cas d'erreur
            } catch (Exception e) {
                logger.severe("Failed to create vehicle: " + vehicle.getLicensePlate() + " - " + e.getMessage());
                // Continuer avec les autres véhicules même en cas d'erreur
            }
        }

        logger.info("Batch creation completed. Success: " + createdVehicles.size() + "/" + vehicles.size());
        return createdVehicles;
    }

    @Transactional
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        logger.info("Updating vehicle with id: " + id);

        try {
            vehicleDetails.validateConstraints();
            logger.info("Contraintes validées pour la mise à jour: " + vehicleDetails.getLicensePlate());
        } catch (IllegalArgumentException e) {
            logger.severe("Erreur contraintes véhicule: " + e.getMessage());
            throw new RuntimeException("Erreur de validation: " + e.getMessage());
        }

        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
            vehicle.setType(vehicleDetails.getType());
            vehicle.setMaxWeight(vehicleDetails.getMaxWeight());
            vehicle.setMaxVolume(vehicleDetails.getMaxVolume());
            vehicle.setMaxDeliveries(vehicleDetails.getMaxDeliveries());
            vehicle.setRange(vehicleDetails.getRange());
            return vehicleRepository.save(vehicle);
        }
        throw new RuntimeException("Vehicle not found with id: " + id);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        logger.info("Deleting vehicle with id: " + id);
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
        } else {
            throw new RuntimeException("Vehicle not found with id: " + id);
        }
    }

    public List<Vehicle> getVehiclesByType(Vehicle.VehicleType type) {
        logger.info("Fetching vehicles of type: " + type);
        return vehicleRepository.findByType(type);
    }

    public List<Vehicle> getAvailableVehicles() {
        logger.info("Fetching available vehicles");
        return vehicleRepository.findAvailableVehicles();
    }

    public List<Vehicle> getSuitableVehicles(Double requiredWeight, Double requiredVolume) {
        logger.info("Fetching suitable vehicles for weight: " + requiredWeight + ", volume: " + requiredVolume);
        return vehicleRepository.findSuitableVehicles(requiredWeight, requiredVolume);
    }

    public Vehicle getVehicleByLicensePlate(String licensePlate) {
        logger.info("Fetching vehicle with license plate: " + licensePlate);
        return vehicleRepository.findByLicensePlate(licensePlate);
    }
}