package com.delivery.optimizer;

import com.delivery.entity.Delivery;
import com.delivery.entity.Warehouse;
import com.delivery.entity.Vehicle;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ClarkeWrightOptimizer implements TourOptimizer {

    private static final Logger logger = Logger.getLogger(ClarkeWrightOptimizer.class.getName());

    @Override
    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        logger.info("Calculating tour using Clarke & Wright algorithm for " + deliveries.size() + " deliveries");

        if (deliveries.isEmpty()) {
            return new ArrayList<>();
        }

        if (deliveries.size() == 1) {
            // Cas simple : une seule livraison
            deliveries.get(0).setOrder(1);
            return deliveries;
        }

        // Étape 1: Calculer les économies
        List<Savings> savings = calculateSavings(warehouse, deliveries);

        // Étape 2: Initialiser les tours individuelles
        List<List<Delivery>> tours = initializeIndividualTours(deliveries);

        // Étape 3: Fusionner les tours par ordre d'économie décroissante
        tours = mergeTours(warehouse, tours, savings, vehicle);

        // Étape 4: Retourner la tournée optimisée (on prend la première tournée fusionnée)
        List<Delivery> optimizedRoute = tours.get(0);

        // Assigner l'ordre aux livraisons
        for (int i = 0; i < optimizedRoute.size(); i++) {
            optimizedRoute.get(i).setOrder(i + 1);
        }

        logger.info("Clarke & Wright optimization completed. Route with " + optimizedRoute.size() + " deliveries");
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

    private List<Savings> calculateSavings(Warehouse warehouse, List<Delivery> deliveries) {
        List<Savings> savings = new ArrayList<>();

        for (int i = 0; i < deliveries.size(); i++) {
            for (int j = i + 1; j < deliveries.size(); j++) {
                Delivery d1 = deliveries.get(i);
                Delivery d2 = deliveries.get(j);

                double distWarehouseD1 = calculateDistance(
                        warehouse.getLatitude(), warehouse.getLongitude(),
                        d1.getLatitude(), d1.getLongitude()
                );

                double distWarehouseD2 = calculateDistance(
                        warehouse.getLatitude(), warehouse.getLongitude(),
                        d2.getLatitude(), d2.getLongitude()
                );

                double distD1D2 = calculateDistance(
                        d1.getLatitude(), d1.getLongitude(),
                        d2.getLatitude(), d2.getLongitude()
                );

                double saving = distWarehouseD1 + distWarehouseD2 - distD1D2;
                savings.add(new Savings(d1, d2, saving));
            }
        }

        // Trier par économie décroissante
        savings.sort(Comparator.comparingDouble(Savings::getSaving).reversed());
        return savings;
    }

    private List<List<Delivery>> initializeIndividualTours(List<Delivery> deliveries) {
        return deliveries.stream()
                .map(d -> new ArrayList<>(List.of(d)))
                .collect(Collectors.toList());
    }

    private List<List<Delivery>> mergeTours(Warehouse warehouse, List<List<Delivery>> tours,
                                            List<Savings> savings, Vehicle vehicle) {
        for (Savings saving : savings) {
            List<Delivery> tour1 = findTourContaining(tours, saving.getDelivery1());
            List<Delivery> tour2 = findTourContaining(tours, saving.getDelivery2());

            if (tour1 != null && tour2 != null && tour1 != tour2) {
                if (canMerge(warehouse, tour1, tour2, vehicle)) {
                    List<Delivery> mergedTour = mergeTwoTours(tour1, tour2, saving);
                    tours.remove(tour1);
                    tours.remove(tour2);
                    tours.add(mergedTour);

                    // Si toutes les livraisons sont dans une seule tournée, on arrête
                    if (tours.size() == 1) {
                        break;
                    }
                }
            }
        }

        return tours;
    }

    private List<Delivery> findTourContaining(List<List<Delivery>> tours, Delivery delivery) {
        return tours.stream()
                .filter(tour -> tour.contains(delivery))
                .findFirst()
                .orElse(null);
    }

    private boolean canMerge(Warehouse warehouse, List<Delivery> tour1, List<Delivery> tour2, Vehicle vehicle) {
        if (vehicle == null) return true;

        // Vérifier les contraintes de capacité
        double totalWeight = tour1.stream().mapToDouble(Delivery::getWeight).sum() +
                tour2.stream().mapToDouble(Delivery::getWeight).sum();
        double totalVolume = tour1.stream().mapToDouble(Delivery::getVolume).sum() +
                tour2.stream().mapToDouble(Delivery::getVolume).sum();
        int totalDeliveries = tour1.size() + tour2.size();

        return totalWeight <= vehicle.getMaxWeight() &&
                totalVolume <= vehicle.getMaxVolume() &&
                totalDeliveries <= vehicle.getMaxDeliveries();
    }

    private List<Delivery> mergeTwoTours(List<Delivery> tour1, List<Delivery> tour2, Savings saving) {
        // Vérifier si les livraisons sont aux extrémités des tours
        boolean tour1StartsWithD1 = tour1.get(0).equals(saving.getDelivery1());
        boolean tour1EndsWithD1 = tour1.get(tour1.size() - 1).equals(saving.getDelivery1());
        boolean tour2StartsWithD2 = tour2.get(0).equals(saving.getDelivery2());
        boolean tour2EndsWithD2 = tour2.get(tour2.size() - 1).equals(saving.getDelivery2());

        if (tour1EndsWithD1 && tour2StartsWithD2) {
            List<Delivery> merged = new ArrayList<>(tour1);
            merged.addAll(tour2);
            return merged;
        } else if (tour1StartsWithD1 && tour2EndsWithD2) {
            List<Delivery> merged = new ArrayList<>(tour2);
            merged.addAll(tour1);
            return merged;
        } else if (tour1EndsWithD1 && tour2EndsWithD2) {
            // Inverser la deuxième tournée
            Collections.reverse(tour2);
            List<Delivery> merged = new ArrayList<>(tour1);
            merged.addAll(tour2);
            return merged;
        } else if (tour1StartsWithD1 && tour2StartsWithD2) {
            // Inverser la première tournée
            Collections.reverse(tour1);
            List<Delivery> merged = new ArrayList<>(tour1);
            merged.addAll(tour2);
            return merged;
        }

        // Si les conditions de fusion ne sont pas remplies, retourner tour1
        return tour1;
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

    private static class Savings {
        private final Delivery delivery1;
        private final Delivery delivery2;
        private final double saving;

        public Savings(Delivery delivery1, Delivery delivery2, double saving) {
            this.delivery1 = delivery1;
            this.delivery2 = delivery2;
            this.saving = saving;
        }

        public Delivery getDelivery1() { return delivery1; }
        public Delivery getDelivery2() { return delivery2; }
        public double getSaving() { return saving; }
    }
}