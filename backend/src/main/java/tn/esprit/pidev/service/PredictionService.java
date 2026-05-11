package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.PartPredictionDTO;
import tn.esprit.pidev.dto.PartPredictionDTO.Urgency;
import tn.esprit.pidev.entity.MaintenancePartUsage;
import tn.esprit.pidev.entity.SparePart;
import tn.esprit.pidev.entity.Vehicle;
import tn.esprit.pidev.repository.MaintenancePartUsageRepository;
import tn.esprit.pidev.repository.SparePartRepository;
import tn.esprit.pidev.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart Spare Parts Prediction Service
 *
 * HYBRID APPROACH:
 * ─────────────────────────────────────────────────────────────────
 * < 3 usage records for a part → FREQUENCY_ANALYSIS
 *   - Uses average interval between past uses
 *   - Confidence: LOW (0.40 - 0.60)
 *
 * ≥ 3 usage records → LINEAR_REGRESSION
 *   - Fits a line through usage dates (days since epoch as X)
 *   - Predicts next usage date from the trend slope
 *   - Confidence: based on R² fit quality (0.60 - 0.95)
 *
 * URGENCY:
 *   daysUntilNeeded <= 7   → URGENT
 *   daysUntilNeeded <= 30  → SOON
 *   else                   → MONITOR
 */
@Service
public class PredictionService {

    private static final int URGENCY_URGENT_DAYS = 7;
    private static final int URGENCY_SOON_DAYS   = 30;
    private static final int AI_THRESHOLD        = 3; // min records for regression

    private final MaintenancePartUsageRepository usageRepo;
    private final SparePartRepository            partRepo;
    private final VehicleRepository              vehicleRepo;

    public PredictionService(MaintenancePartUsageRepository usageRepo,
                             SparePartRepository partRepo,
                             VehicleRepository vehicleRepo) {
        this.usageRepo   = usageRepo;
        this.partRepo    = partRepo;
        this.vehicleRepo = vehicleRepo;
    }

    // ── Fleet-wide predictions ────────────────────────────────────

    public List<PartPredictionDTO> predictFleet() {
        List<SparePart> parts  = partRepo.findAll();
        List<MaintenancePartUsage> allUsages = usageRepo.findAll();

        return parts.stream()
            .map(part -> predictForPart(part, allUsages, null, null))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(PartPredictionDTO::getDaysUntilNeeded))
            .collect(Collectors.toList());
    }

    // ── Per-vehicle predictions ───────────────────────────────────

    public List<PartPredictionDTO> predictForVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepo.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));

        List<MaintenancePartUsage> vehicleUsages = usageRepo.findAll().stream()
            .filter(u -> u.getMaintenanceOrder().getVehicle().getId().equals(vehicleId))
            .collect(Collectors.toList());

        List<SparePart> partsUsedOnVehicle = vehicleUsages.stream()
            .map(MaintenancePartUsage::getSparePart)
            .distinct()
            .collect(Collectors.toList());

        return partsUsedOnVehicle.stream()
            .map(part -> predictForPart(part, vehicleUsages, vehicle.getId(), vehicle.getPlateNumber()))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(PartPredictionDTO::getDaysUntilNeeded))
            .collect(Collectors.toList());
    }

    // ── Core prediction logic ─────────────────────────────────────

    private PartPredictionDTO predictForPart(SparePart part,
                                              List<MaintenancePartUsage> usages,
                                              Long vehicleId,
                                              String vehiclePlate) {
        // Filter usages for this specific part
        List<MaintenancePartUsage> partUsages = usages.stream()
            .filter(u -> u.getSparePart().getId().equals(part.getId()))
            .sorted(Comparator.comparing(MaintenancePartUsage::getUsedDate))
            .collect(Collectors.toList());

        if (partUsages.isEmpty()) return null;

        // Collect usage dates as LocalDate list
        List<LocalDate> usageDates = partUsages.stream()
            .map(u -> LocalDate.parse(u.getUsedDate().substring(0, 10)))
            .collect(Collectors.toList());

        // Collect affected vehicle plates
        List<String> affectedVehicles = partUsages.stream()
            .map(u -> u.getMaintenanceOrder().getVehicle().getPlateNumber())
            .distinct()
            .collect(Collectors.toList());

        // Average quantity used per maintenance
        int avgQtyUsed = (int) Math.ceil(
            partUsages.stream().mapToInt(MaintenancePartUsage::getQuantityUsed).average().orElse(1)
        );

        // Choose method based on data volume
        PartPredictionDTO dto;
        if (partUsages.size() < AI_THRESHOLD) {
            dto = frequencyAnalysis(part, usageDates, avgQtyUsed);
        } else {
            dto = linearRegression(part, usageDates, avgQtyUsed);
        }

        dto.setCurrentStock(part.getStockQuantity());
        dto.setAffectedVehicles(affectedVehicles);
        dto.setVehicleId(vehicleId);
        dto.setVehiclePlate(vehiclePlate);

        return dto;
    }

    // ── METHOD 1: Frequency Analysis ─────────────────────────────

    private PartPredictionDTO frequencyAnalysis(SparePart part,
                                                 List<LocalDate> dates,
                                                 int avgQty) {
        PartPredictionDTO dto = baseDTO(part, avgQty);
        dto.setMethod("FREQUENCY_ANALYSIS");

        if (dates.size() == 1) {
            // Only one data point — assume monthly interval
            LocalDate predicted = dates.get(0).plusDays(30);
            int days = (int) ChronoUnit.DAYS.between(LocalDate.now(), predicted);
            dto.setPredictedDateNeeded(predicted.toString());
            dto.setDaysUntilNeeded(days);
            dto.setConfidenceScore(0.40);
            dto.setExplanation("Only 1 past use recorded. Assuming 30-day interval.");
        } else {
            // Average interval between usages
            long totalGap = 0;
            for (int i = 1; i < dates.size(); i++) {
                totalGap += ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i));
            }
            long avgInterval = totalGap / (dates.size() - 1);
            LocalDate lastUse  = dates.get(dates.size() - 1);
            LocalDate predicted = lastUse.plusDays(avgInterval);
            int days = (int) ChronoUnit.DAYS.between(LocalDate.now(), predicted);

            dto.setPredictedDateNeeded(predicted.toString());
            dto.setDaysUntilNeeded(days);
            dto.setConfidenceScore(0.55);
            dto.setExplanation(String.format(
                "Used %d times. Average interval: %d days. Last used: %s.",
                dates.size(), avgInterval, lastUse));
        }

        dto.setUrgency(urgencyFor(dto.getDaysUntilNeeded()));
        return dto;
    }

    // ── METHOD 2: Linear Regression ──────────────────────────────

    /**
     * Fits a linear regression on (index → days since epoch).
     * Predicts the next date as: y = a + b * (n+1)
     * Confidence based on R² (coefficient of determination).
     */
    private PartPredictionDTO linearRegression(SparePart part,
                                                List<LocalDate> dates,
                                                int avgQty) {
        PartPredictionDTO dto = baseDTO(part, avgQty);
        dto.setMethod("LINEAR_REGRESSION");

        int n = dates.size();
        double[] x = new double[n]; // index (0,1,2...)
        double[] y = new double[n]; // days since epoch

        LocalDate epoch = LocalDate.of(2020, 1, 1);
        for (int i = 0; i < n; i++) {
            x[i] = i;
            y[i] = ChronoUnit.DAYS.between(epoch, dates.get(i));
        }

        // Calculate means
        double meanX = Arrays.stream(x).average().orElse(0);
        double meanY = Arrays.stream(y).average().orElse(0);

        // Calculate slope (b) and intercept (a)
        double ssXY = 0, ssXX = 0, ssTot = 0;
        for (int i = 0; i < n; i++) {
            ssXY += (x[i] - meanX) * (y[i] - meanY);
            ssXX += (x[i] - meanX) * (x[i] - meanX);
            ssTot += (y[i] - meanY) * (y[i] - meanY);
        }

        double slope     = ssXX == 0 ? 30 : ssXY / ssXX;
        double intercept = meanY - slope * meanX;

        // Predict next (index = n)
        double predictedDaysFromEpoch = intercept + slope * n;
        LocalDate predictedDate = epoch.plusDays((long) predictedDaysFromEpoch);

        // R² for confidence
        double ssRes = 0;
        for (int i = 0; i < n; i++) {
            double yPred = intercept + slope * x[i];
            ssRes += (y[i] - yPred) * (y[i] - yPred);
        }
        double r2 = ssTot == 0 ? 0.5 : 1.0 - (ssRes / ssTot);
        double confidence = Math.max(0.60, Math.min(0.95, r2));

        int days = (int) ChronoUnit.DAYS.between(LocalDate.now(), predictedDate);

        dto.setPredictedDateNeeded(predictedDate.toString());
        dto.setDaysUntilNeeded(days);
        dto.setConfidenceScore(Math.round(confidence * 100.0) / 100.0);
        dto.setUrgency(urgencyFor(days));
        dto.setExplanation(String.format(
            "AI prediction based on %d past uses. Avg interval: %.0f days. R²: %.2f.",
            n, slope, r2));

        return dto;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private PartPredictionDTO baseDTO(SparePart part, int avgQty) {
        PartPredictionDTO dto = new PartPredictionDTO();
        dto.setPartName(part.getName());
        dto.setReferenceCode(part.getReferenceCode());
        dto.setCategory(part.getCategory().name());
        dto.setPredictedQuantityNeeded(avgQty);
        return dto;
    }

    private Urgency urgencyFor(int days) {
        if (days <= URGENCY_URGENT_DAYS) return Urgency.URGENT;
        if (days <= URGENCY_SOON_DAYS)   return Urgency.SOON;
        return Urgency.MONITOR;
    }
}
