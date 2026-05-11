package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.ActionSendResponse;
import tn.esprit.pidev.dto.ChurnPredictionResponse;
import tn.esprit.pidev.dto.CLVResponse;
import tn.esprit.pidev.dto.PlanRecommendationResponse;
import tn.esprit.pidev.service.IMLService;

import java.util.List;

@RestController
@RequestMapping("/api/ml")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "AI & ML", description = "Plan recommendation, churn prediction, CLV and automated actions")
public class MLController {

    private final IMLService service;

    public MLController(IMLService service) {
        this.service = service;
    }

    @GetMapping("/recommend/{passengerId}")
    @Operation(summary = "Recommend a plan for a passenger (Random Forest)")
    public ResponseEntity<PlanRecommendationResponse> recommend(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.recommendPlan(passengerId));
    }

    @GetMapping("/churn/{passengerId}")
    @Operation(summary = "Predict churn risk for a passenger (Random Forest)")
    public ResponseEntity<ChurnPredictionResponse> churn(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.predictChurn(passengerId));
    }

    @GetMapping("/churn/all")
    @Operation(summary = "Predict churn for all passengers — OPERATOR / ADMIN")
    public ResponseEntity<List<ChurnPredictionResponse>> churnAll() {
        return ResponseEntity.ok(service.predictChurnAll());
    }

    @GetMapping("/clv/{passengerId}")
    @Operation(summary = "Predict Customer Lifetime Value (Gradient Boosting)")
    public ResponseEntity<CLVResponse> clv(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.predictCLV(passengerId));
    }

    // ── NEW : Send action — generates promo code + sends email ──
    @PostMapping("/action/send/{passengerId}")
    @Operation(summary = "Send recommended action to passenger — generates unique promo code and sends email")
    public ResponseEntity<ActionSendResponse> sendAction(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.sendAction(passengerId));
    }
}
