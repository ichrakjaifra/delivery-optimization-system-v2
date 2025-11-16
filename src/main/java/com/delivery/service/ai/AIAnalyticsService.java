package com.delivery.service.ai;

import com.delivery.dto.ai.AIOptimizationRequest;
import com.delivery.dto.ai.AIOptimizationResponse;
import com.delivery.entity.DeliveryHistory;
import com.delivery.repository.DeliveryHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalyticsService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final DeliveryHistoryRepository deliveryHistoryRepository;

    @Autowired
    public AIAnalyticsService(ChatClient chatClient,
                              DeliveryHistoryRepository deliveryHistoryRepository) {
        this.chatClient = chatClient;
        this.objectMapper = new ObjectMapper();
        this.deliveryHistoryRepository = deliveryHistoryRepository;
    }

    public AIOptimizationResponse analyzeDeliveryPatterns() {
        try {
            // Récupérer les données historiques
            List<DeliveryHistory> allHistory = deliveryHistoryRepository.findAll();

            if (allHistory.isEmpty()) {
                return createNoDataResponse();
            }

            // Préparer les données pour l'analyse
            AIOptimizationRequest.HistoricalData historicalData = prepareHistoricalDataForAnalysis(allHistory);

            // Appeler l'IA pour l'analyse
            return callAIAnalysis(historicalData);

        } catch (Exception e) {
            logger.error("AI analytics error", e);
            return createErrorResponse("Analytics service unavailable");
        }
    }

    public AIOptimizationResponse analyzeCustomerPatterns(Long customerId) {
        try {
            List<DeliveryHistory> customerHistory =
                    deliveryHistoryRepository.findByCustomerId(customerId);

            if (customerHistory.isEmpty()) {
                return createNoDataResponse();
            }

            AIOptimizationRequest.HistoricalData historicalData =
                    prepareHistoricalDataForAnalysis(customerHistory);

            return callAIAnalysis(historicalData);

        } catch (Exception e) {
            logger.error("Customer AI analytics error", e);
            return createErrorResponse("Customer analytics service unavailable");
        }
    }

    private AIOptimizationRequest.HistoricalData prepareHistoricalDataForAnalysis(
            List<DeliveryHistory> history) {

        List<AIOptimizationRequest.HistoricalData.DeliveryHistory> historicalDeliveries =
                history.stream()
                        .map(h -> new AIOptimizationRequest.HistoricalData.DeliveryHistory(
                                h.getCustomer().getName(),
                                h.getCustomer().getAddress(),
                                h.getDeliveryDate().format(DateTimeFormatter.ISO_DATE),
                                h.getDayOfWeek().toString(),
                                h.getDelayMinutes(),
                                h.getPlannedTime() != null ?
                                        h.getPlannedTime().format(DateTimeFormatter.ISO_LOCAL_TIME) : null,
                                h.getActualTime() != null ?
                                        h.getActualTime().format(DateTimeFormatter.ISO_LOCAL_TIME) : null
                        ))
                        .collect(Collectors.toList());

        AIOptimizationRequest.HistoricalData.PerformanceMetrics metrics =
                calculateDetailedPerformanceMetrics(history);

        return new AIOptimizationRequest.HistoricalData(historicalDeliveries, metrics);
    }

    private AIOptimizationRequest.HistoricalData.PerformanceMetrics calculateDetailedPerformanceMetrics(
            List<DeliveryHistory> history) {

        // Calculs détaillés des métriques
        double averageDelay = history.stream()
                .filter(h -> h.getDelayMinutes() != null && h.getDelayMinutes() > 0)
                .mapToInt(DeliveryHistory::getDelayMinutes)
                .average()
                .orElse(0.0);

        long onTimeCount = history.stream()
                .filter(h -> h.getDelayMinutes() != null && h.getDelayMinutes() <= 0)
                .count();
        double onTimeRate = (double) onTimeCount / history.size() * 100;

        // Analyser les performances par jour de la semaine
        var dayPerformance = history.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getDayOfWeek().toString(),
                        Collectors.averagingInt(h -> h.getDelayMinutes() != null ? h.getDelayMinutes() : 0)
                ));

        String bestDay = dayPerformance.entrySet().stream()
                .min((e1, e2) -> Double.compare(e1.getValue(), e2.getValue()))
                .map(e -> e.getKey())
                .orElse("UNKNOWN");

        String worstDay = dayPerformance.entrySet().stream()
                .max((e1, e2) -> Double.compare(e1.getValue(), e2.getValue()))
                .map(e -> e.getKey())
                .orElse("UNKNOWN");

        return new AIOptimizationRequest.HistoricalData.PerformanceMetrics(
                averageDelay, onTimeRate, bestDay, worstDay, new ArrayList<>()
        );
    }

    private AIOptimizationResponse callAIAnalysis(AIOptimizationRequest.HistoricalData historicalData) {
        try {
            String historicalDataJson = objectMapper.writeValueAsString(historicalData);

            String prompt = buildAnalyticsPrompt(historicalDataJson);

            logger.debug("Sending analytics request to AI");

            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            logger.debug("Received AI analytics response: {}", aiResponse);

            return parseAIAnalyticsResponse(aiResponse);

        } catch (JsonProcessingException e) {
            logger.error("JSON processing error in AI analytics", e);
            return createErrorResponse("JSON processing error");
        } catch (Exception e) {
            logger.error("AI analytics service error", e);
            return createErrorResponse("AI analytics service unavailable");
        }
    }

    private String buildAnalyticsPrompt(String historicalDataJson) {
        return """
            Vous êtes un expert en analyse de données de livraison. Analysez les données historiques fournies et identifiez les patterns, tendances et opportunités d'amélioration.

            DONNÉES HISTORIQUES (JSON):
            %s

            OBJECTIFS D'ANALYSE:
            1. Identifier les patterns de retard
            2. Analyser les performances par jour de la semaine
            3. Détecter les zones problématiques
            4. Proposer des recommandations d'optimisation
            5. Prédire les tendances futures

            FORMAT DE SORTIE ATTENDU (JSON):
            {
                "success": true,
                "message": "Analyse complétée avec succès",
                "result": {
                    "optimizedRoute": [],
                    "summary": {
                        "totalDistance": 0,
                        "totalDeliveries": %d,
                        "estimatedTotalTime": 0,
                        "averageSpeed": 0,
                        "recommendedStartTime": "",
                        "estimatedCompletionTime": ""
                    },
                    "recommendations": [
                        {
                            "type": "PATTERN_ANALYSIS",
                            "description": "Identification des patterns de livraison",
                            "impact": "HIGH",
                            "suggestion": "Optimiser les tournées du lundi en raison des retards fréquents"
                        }
                    ],
                    "predictions": [
                        {
                            "type": "TREND_PREDICTION",
                            "description": "Tendance d'amélioration des performances",
                            "probability": "HIGH",
                            "mitigation": "Maintenir les bonnes pratiques actuelles"
                        }
                    ]
                }
            }

            Répondez UNIQUEMENT avec le JSON, sans autre texte.
            """.formatted(historicalDataJson, historicalDataJson.length());
    }

    private AIOptimizationResponse parseAIAnalyticsResponse(String aiResponse) {
        try {
            String cleanedResponse = aiResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(cleanedResponse, AIOptimizationResponse.class);
        } catch (Exception e) {
            logger.error("Failed to parse AI analytics response: {}", aiResponse, e);
            return createErrorResponse("Failed to parse AI analytics response");
        }
    }

    private AIOptimizationResponse createNoDataResponse() {
        AIOptimizationResponse response = new AIOptimizationResponse();
        response.setSuccess(false);
        response.setMessage("No historical data available for analysis");
        return response;
    }

    private AIOptimizationResponse createErrorResponse(String message) {
        AIOptimizationResponse response = new AIOptimizationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}