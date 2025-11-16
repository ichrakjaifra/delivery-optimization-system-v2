package com.delivery.optimizer;

import com.delivery.dto.ai.AIOptimizationRequest;
import com.delivery.dto.ai.AIOptimizationResponse;
import com.delivery.entity.Delivery;
import com.delivery.entity.Warehouse;
import com.delivery.entity.Vehicle;
import com.delivery.entity.DeliveryHistory;
import com.delivery.repository.DeliveryHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.optimizer.active", havingValue = "ai", matchIfMissing = false)
public class AIOptimizer implements TourOptimizer {

    private static final Logger logger = LoggerFactory.getLogger(AIOptimizer.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final DeliveryHistoryRepository deliveryHistoryRepository;
    private final NearestNeighborOptimizer fallbackOptimizer;

    @Autowired
    public AIOptimizer(ChatClient chatClient,
                       DeliveryHistoryRepository deliveryHistoryRepository,
                       NearestNeighborOptimizer fallbackOptimizer) {
        this.chatClient = chatClient;
        this.objectMapper = new ObjectMapper();
        this.deliveryHistoryRepository = deliveryHistoryRepository;
        this.fallbackOptimizer = fallbackOptimizer;
    }

    @Override
    public List<Delivery> calculateOptimalTour(Warehouse warehouse, List<Delivery> deliveries, Vehicle vehicle) {
        logger.info("Starting AI optimization for {} deliveries from warehouse: {}",
                deliveries.size(), warehouse.getName());

        try {
            // Préparer les données pour l'IA
            AIOptimizationRequest request = prepareOptimizationRequest(warehouse, deliveries, vehicle);

            // Appeler l'IA
            AIOptimizationResponse response = callAIForOptimization(request);

            if (response.isSuccess()) {
                return applyAIOptimization(deliveries, response);
            } else {
                logger.warn("AI optimization failed, falling back to Nearest Neighbor");
                return fallbackOptimizer.calculateOptimalTour(warehouse, deliveries, vehicle);
            }

        } catch (Exception e) {
            logger.error("AI optimization error: {}", e.getMessage(), e);
            logger.info("Falling back to Nearest Neighbor due to AI error");
            return fallbackOptimizer.calculateOptimalTour(warehouse, deliveries, vehicle);
        }
    }

    @Override
    public Double calculateTotalDistance(Warehouse warehouse, List<Delivery> deliveries) {
        try {
            List<Delivery> optimizedDeliveries = calculateOptimalTour(warehouse, deliveries, null);
            return calculateTotalDistanceFromOptimizedRoute(warehouse, optimizedDeliveries);
        } catch (Exception e) {
            logger.error("AI distance calculation failed, using fallback", e);
            return fallbackOptimizer.calculateTotalDistance(warehouse, deliveries);
        }
    }

    private AIOptimizationRequest prepareOptimizationRequest(Warehouse warehouse,
                                                             List<Delivery> deliveries,
                                                             Vehicle vehicle) {
        AIOptimizationRequest request = new AIOptimizationRequest();

        // Données de l'entrepôt
        request.setWarehouse(new AIOptimizationRequest.WarehouseData(
                warehouse.getId(), warehouse.getName(), warehouse.getAddress(),
                warehouse.getLatitude(), warehouse.getLongitude(), warehouse.getOpeningHours()
        ));

        // Données des livraisons
        List<AIOptimizationRequest.DeliveryData> deliveryDataList = deliveries.stream()
                .map(delivery -> new AIOptimizationRequest.DeliveryData(
                        delivery.getId(),
                        delivery.getWeight(),
                        delivery.getVolume(),
                        delivery.getPreferredTimeSlot(),
                        delivery.getAddress(),
                        delivery.getLatitude(),
                        delivery.getLongitude(),
                        delivery.getCustomer().getName(),
                        delivery.getCustomer().getPreferredTimeSlot()
                ))
                .collect(Collectors.toList());
        request.setDeliveries(deliveryDataList);

        // Données du véhicule
        if (vehicle != null) {
            request.setVehicle(new AIOptimizationRequest.VehicleData(
                    vehicle.getLicensePlate(),
                    vehicle.getType().toString(),
                    vehicle.getMaxWeight(),
                    vehicle.getMaxVolume(),
                    vehicle.getMaxDeliveries(),
                    vehicle.getRange()
            ));
        }

        // Données historiques
        request.setHistoricalData(prepareHistoricalData());

        return request;
    }

    private AIOptimizationRequest.HistoricalData prepareHistoricalData() {
        List<DeliveryHistory> recentHistory = deliveryHistoryRepository.findAll().stream()
                .limit(100) // Limiter aux 100 dernières livraisons
                .collect(Collectors.toList());

        List<AIOptimizationRequest.HistoricalData.DeliveryHistory> historicalDeliveries =
                recentHistory.stream()
                        .map(history -> new AIOptimizationRequest.HistoricalData.DeliveryHistory(
                                history.getCustomer().getName(),
                                history.getCustomer().getAddress(),
                                history.getDeliveryDate().format(DateTimeFormatter.ISO_DATE),
                                history.getDayOfWeek().toString(),
                                history.getDelayMinutes(),
                                history.getPlannedTime() != null ?
                                        history.getPlannedTime().format(DateTimeFormatter.ISO_LOCAL_TIME) : null,
                                history.getActualTime() != null ?
                                        history.getActualTime().format(DateTimeFormatter.ISO_LOCAL_TIME) : null
                        ))
                        .collect(Collectors.toList());

        // Calculer les métriques de performance
        AIOptimizationRequest.HistoricalData.PerformanceMetrics metrics =
                calculatePerformanceMetrics(recentHistory);

        return new AIOptimizationRequest.HistoricalData(historicalDeliveries, metrics);
    }

    private AIOptimizationRequest.HistoricalData.PerformanceMetrics calculatePerformanceMetrics(
            List<DeliveryHistory> history) {

        if (history.isEmpty()) {
            return new AIOptimizationRequest.HistoricalData.PerformanceMetrics(
                    0.0, 100.0, "MONDAY", "SUNDAY", new ArrayList<>()
            );
        }

        double averageDelay = history.stream()
                .filter(h -> h.getDelayMinutes() != null && h.getDelayMinutes() > 0)
                .mapToInt(DeliveryHistory::getDelayMinutes)
                .average()
                .orElse(0.0);

        long onTimeCount = history.stream()
                .filter(h -> h.getDelayMinutes() != null && h.getDelayMinutes() <= 0)
                .count();
        double onTimeRate = (double) onTimeCount / history.size() * 100;

        return new AIOptimizationRequest.HistoricalData.PerformanceMetrics(
                averageDelay, onTimeRate, "MONDAY", "FRIDAY", new ArrayList<>()
        );
    }

    private AIOptimizationResponse callAIForOptimization(AIOptimizationRequest request) {
        try {
            String requestJson = objectMapper.writeValueAsString(request);

            String prompt = buildOptimizationPrompt(requestJson);

            logger.debug("Sending request to AI: {}", requestJson);

            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            logger.debug("Received AI response: {}", aiResponse);

            return parseAIResponse(aiResponse);

        } catch (JsonProcessingException e) {
            logger.error("JSON processing error in AI optimization", e);
            return createErrorResponse("JSON processing error");
        } catch (Exception e) {
            logger.error("AI service error", e);
            return createErrorResponse("AI service unavailable");
        }
    }

    private String buildOptimizationPrompt(String requestJson) {
        return """
            Vous êtes un expert en optimisation de tournées de livraison. Analysez les données fournies et proposez un plan d'optimisation.

            CONTEXTE:
            - Optimisation de tournées de livraison en milieu urbain
            - Prise en compte des créneaux horaires préférés des clients
            - Analyse des performances historiques
            - Contraintes de capacité des véhicules

            DONNÉES D'ENTRÉE (JSON):
            %s

            INSTRUCTIONS:
            1. Analysez les données historiques pour identifier les patterns
            2. Optimisez l'ordre des livraisons en considérant:
               - Proximité géographique
               - Créneaux horaires préférés
               - Performances historiques par zone
               - Contraintes du véhicule
            3. Générez des recommandations d'amélioration
            4. Prédisez les risques potentiels

            FORMAT DE SORTIE ATTENDU (JSON):
            {
                "success": true,
                "message": "Optimisation réussie",
                "result": {
                    "optimizedRoute": [
                        {
                            "deliveryId": 1,
                            "order": 1,
                            "customerName": "Nom Client",
                            "address": "Adresse",
                            "estimatedArrivalTime": "09:30",
                            "distanceFromPrevious": 2.5,
                            "timeSlot": "09:00-12:00"
                        }
                    ],
                    "summary": {
                        "totalDistance": 45.5,
                        "totalDeliveries": 10,
                        "estimatedTotalTime": 4.5,
                        "averageSpeed": 25.0,
                        "recommendedStartTime": "08:00",
                        "estimatedCompletionTime": "12:30"
                    },
                    "recommendations": [
                        {
                            "type": "TIME_OPTIMIZATION",
                            "description": "Optimisation des créneaux horaires",
                            "impact": "HIGH",
                            "suggestion": "Commencer plus tôt pour éviter le trafic"
                        }
                    ],
                    "predictions": [
                        {
                            "type": "DELAY_RISK",
                            "description": "Risque de retard dans le centre-ville",
                            "probability": "MEDIUM",
                            "mitigation": "Prévoir une marge de 15 minutes"
                        }
                    ]
                }
            }

            Si vous ne pouvez pas optimiser, retournez:
            {
                "success": false,
                "message": "Raison de l'échec"
            }

            Répondez UNIQUEMENT avec le JSON, sans autre texte.
            """.formatted(requestJson);
    }

    private AIOptimizationResponse parseAIResponse(String aiResponse) {
        try {
            // Nettoyer la réponse de l'IA (enlever les markdown, etc.)
            String cleanedResponse = aiResponse.replaceAll("```json", "").replaceAll("```", "").trim();

            return objectMapper.readValue(cleanedResponse, AIOptimizationResponse.class);
        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", aiResponse, e);
            return createErrorResponse("Failed to parse AI response");
        }
    }

    private AIOptimizationResponse createErrorResponse(String message) {
        AIOptimizationResponse response = new AIOptimizationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    private List<Delivery> applyAIOptimization(List<Delivery> deliveries, AIOptimizationResponse response) {
        // Créer une map pour accéder rapidement aux livraisons par ID
        var deliveryMap = deliveries.stream()
                .collect(Collectors.toMap(Delivery::getId, delivery -> delivery));

        // Appliquer l'ordre optimisé
        List<Delivery> optimizedDeliveries = new ArrayList<>();

        for (AIOptimizationResponse.OptimizedDelivery optimized :
                response.getResult().getOptimizedRoute()) {

            Delivery delivery = deliveryMap.get(optimized.getDeliveryId());
            if (delivery != null) {
                delivery.setOrder(optimized.getOrder());
                optimizedDeliveries.add(delivery);
            }
        }

        // Trier par ordre
        optimizedDeliveries.sort(Comparator.comparing(Delivery::getOrder));

        logger.info("AI optimization applied successfully. Optimized {} deliveries",
                optimizedDeliveries.size());

        return optimizedDeliveries;
    }

    private Double calculateTotalDistanceFromOptimizedRoute(Warehouse warehouse,
                                                            List<Delivery> optimizedDeliveries) {
        if (optimizedDeliveries.isEmpty()) {
            return 0.0;
        }

        double totalDistance = 0.0;

        // Distance entre l'entrepôt et la première livraison
        totalDistance += calculateDistance(
                warehouse.getLatitude(), warehouse.getLongitude(),
                optimizedDeliveries.get(0).getLatitude(),
                optimizedDeliveries.get(0).getLongitude()
        );

        // Distances entre les livraisons
        for (int i = 0; i < optimizedDeliveries.size() - 1; i++) {
            totalDistance += calculateDistance(
                    optimizedDeliveries.get(i).getLatitude(),
                    optimizedDeliveries.get(i).getLongitude(),
                    optimizedDeliveries.get(i + 1).getLatitude(),
                    optimizedDeliveries.get(i + 1).getLongitude()
            );
        }

        // Distance entre la dernière livraison et l'entrepôt
        totalDistance += calculateDistance(
                optimizedDeliveries.get(optimizedDeliveries.size() - 1).getLatitude(),
                optimizedDeliveries.get(optimizedDeliveries.size() - 1).getLongitude(),
                warehouse.getLatitude(), warehouse.getLongitude()
        );

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