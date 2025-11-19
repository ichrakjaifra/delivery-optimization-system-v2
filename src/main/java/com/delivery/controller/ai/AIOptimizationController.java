package com.delivery.controller.ai;

import com.delivery.dto.ai.AIOptimizationResponse;
import com.delivery.service.ai.AIAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Optimization", description = "APIs for AI-powered delivery optimization and analytics")
public class AIOptimizationController {

    private final AIAnalyticsService aiAnalyticsService;

    @Autowired
    public AIOptimizationController(AIAnalyticsService aiAnalyticsService) {
        this.aiAnalyticsService = aiAnalyticsService;
    }

    @GetMapping("/analytics/delivery-patterns")
    @Operation(summary = "Analyze delivery patterns using AI")
    public ResponseEntity<AIOptimizationResponse> analyzeDeliveryPatterns() {
        try {
            AIOptimizationResponse response = aiAnalyticsService.analyzeDeliveryPatterns();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/customer/{customerId}")
    @Operation(summary = "Analyze customer delivery patterns using AI")
    public ResponseEntity<AIOptimizationResponse> analyzeCustomerPatterns(@PathVariable Long customerId) {
        try {
            AIOptimizationResponse response = aiAnalyticsService.analyzeCustomerPatterns(customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Check AI service health")
    public ResponseEntity<String> checkAIHealth() {
        try {
            // Test simple de connexion à l'IA
            AIOptimizationResponse response = aiAnalyticsService.analyzeDeliveryPatterns();
            if (response.isSuccess()) {
                return ResponseEntity.ok("AI service is healthy and responsive");
            } else {
                return ResponseEntity.status(503).body("AI service responded with error: " + response.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body("AI service is unavailable: " + e.getMessage());
        }
    }
}