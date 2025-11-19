package com.delivery.optimizer;

import com.delivery.entity.Delivery;
import com.delivery.entity.Warehouse;
import com.delivery.entity.Vehicle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

@Component
public class NearestNeighborOptimizer implements TourOptimizer {

    private static final Logger logger = Logger.getLogger(NearestNeighborOptimizer.class.getName());

    @Override
    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        logger.info("Calculating tour using Nearest Neighbor algorithm for " + deliveries.size() + " deliveries");

        if (deliveries.isEmpty()) {
            return new ArrayList<>();
        }

        List<Delivery> unvisited = new ArrayList<>(deliveries);
        List<Delivery> optimizedRoute = new ArrayList<>();

        // Point de départ : entrepôt
        double currentLat = warehouse.getLatitude();
        double currentLon = warehouse.getLongitude();

        while (!unvisited.isEmpty()) {
            final double finalCurrentLat = currentLat;
            final double finalCurrentLon = currentLon;

            // Trouver la livraison la plus proche
            Delivery nearest = unvisited.stream()
                    .min(Comparator.comparingDouble(d ->
                            calculateDistance(finalCurrentLat, finalCurrentLon, d.getLatitude(), d.getLongitude())))
                    .orElse(null);

            if (nearest != null) {
                optimizedRoute.add(nearest);
                unvisited.remove(nearest);
                currentLat = nearest.getLatitude();
                currentLon = nearest.getLongitude();
            }
        }

        // Assigner l'ordre aux livraisons
        for (int i = 0; i < optimizedRoute.size(); i++) {
            optimizedRoute.get(i).setOrder(i + 1);
        }

        logger.info("Nearest Neighbor optimization completed. Route with " + optimizedRoute.size() + " deliveries");
        return optimizedRoute;
    }

    @Override
    public Double calculateTotalDistance(Warehouse warehouse, List<Delivery> deliveries) {
        if (deliveries.isEmpty()) {
            return 0.0;
        }

        List<Delivery> optimizedDeliveries = calculateOptimalTour(warehouse, deliveries, null);
        double totalDistance = 0.0;

        // Distance entre l'entrepôt et la première livraison
        totalDistance += calculateDistance(
                warehouse.getLatitude(), warehouse.getLongitude(),
                optimizedDeliveries.get(0).getLatitude(), optimizedDeliveries.get(0).getLongitude()
        );

        // Distances entre les livraisons
        for (int i = 0; i < optimizedDeliveries.size() - 1; i++) {
            totalDistance += calculateDistance(
                    optimizedDeliveries.get(i).getLatitude(), optimizedDeliveries.get(i).getLongitude(),
                    optimizedDeliveries.get(i + 1).getLatitude(), optimizedDeliveries.get(i + 1).getLongitude()
            );
        }

        // Distance entre la dernière livraison et l'entrepôt
        totalDistance += calculateDistance(
                optimizedDeliveries.get(optimizedDeliveries.size() - 1).getLatitude(),
                optimizedDeliveries.get(optimizedDeliveries.size() - 1).getLongitude(),
                warehouse.getLatitude(), warehouse.getLongitude()
        );

        logger.info("Total distance calculated: " + totalDistance + " km");
        return totalDistance;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}