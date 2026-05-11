package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.PartSuggestionDTO;
import tn.esprit.pidev.dto.PartUsageRequest;
import tn.esprit.pidev.entity.MaintenanceOrder;
import tn.esprit.pidev.entity.MaintenancePartUsage;
import tn.esprit.pidev.entity.SparePart;
import tn.esprit.pidev.entity.SparePart.PartCategory;
import tn.esprit.pidev.repository.MaintenanceOrderRepository;
import tn.esprit.pidev.repository.MaintenancePartUsageRepository;
import tn.esprit.pidev.repository.SparePartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SparePartService {

    private final SparePartRepository partRepo;
    private final MaintenancePartUsageRepository usageRepo;
    private final MaintenanceOrderRepository maintenanceRepo;

    /**
     * AUTO-SUGGEST MAP:
     * Maps maintenance type → relevant part categories.
     * PREVENTIVE → Filters, Engine parts (routine service)
     * CORRECTIVE → Brakes, Electrical, Transmission (repairs)
     */
    private static final Map<String, List<PartCategory>> SUGGEST_MAP = Map.of(
        "PREVENTIVE", List.of(PartCategory.FILTERS, PartCategory.ENGINE,
                              PartCategory.TIRES, PartCategory.HVAC),
        "CORRECTIVE", List.of(PartCategory.BRAKES, PartCategory.ELECTRICAL,
                              PartCategory.TRANSMISSION, PartCategory.BODYWORK,
                              PartCategory.ENGINE)
    );

    private static final Map<String, String> SUGGEST_REASON = Map.of(
        "PREVENTIVE", "Commonly used in routine PREVENTIVE maintenance",
        "CORRECTIVE", "Commonly needed for CORRECTIVE repairs"
    );

    public SparePartService(SparePartRepository partRepo,
                            MaintenancePartUsageRepository usageRepo,
                            MaintenanceOrderRepository maintenanceRepo) {
        this.partRepo        = partRepo;
        this.usageRepo       = usageRepo;
        this.maintenanceRepo = maintenanceRepo;
    }

    // ── Spare Part CRUD ───────────────────────────────────────────

    public List<SparePart> getAll() { return partRepo.findAll(); }

    public SparePart getById(Long id) {
        return partRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Spare part not found: " + id));
    }

    public List<SparePart> getLowStock() {
        return partRepo.findLowStockParts();
    }

    public SparePart create(SparePart part) {
        if (partRepo.existsByReferenceCode(part.getReferenceCode())) {
            throw new RuntimeException("Reference code '" + part.getReferenceCode() + "' already exists.");
        }
        return partRepo.save(part);
    }

    public SparePart update(Long id, SparePart updated) {
        SparePart existing = getById(id);
        if (!existing.getReferenceCode().equals(updated.getReferenceCode())
                && partRepo.existsByReferenceCode(updated.getReferenceCode())) {
            throw new RuntimeException("Reference code '" + updated.getReferenceCode() + "' already exists.");
        }
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setReferenceCode(updated.getReferenceCode());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setMinStockThreshold(updated.getMinStockThreshold());
        existing.setUnitCost(updated.getUnitCost());
        existing.setSupplier(updated.getSupplier());
        existing.setNotes(updated.getNotes());
        return partRepo.save(existing);
    }

    public void delete(Long id) { partRepo.deleteById(id); }

    // ── Auto-suggest ──────────────────────────────────────────────

    /**
     * Returns suggested parts based on the maintenance order type.
     * Filters out parts with zero stock and marks low-stock ones.
     */
    public List<PartSuggestionDTO> suggestForOrder(Long maintenanceOrderId) {
        MaintenanceOrder order = maintenanceRepo.findById(maintenanceOrderId)
            .orElseThrow(() -> new RuntimeException("Maintenance order not found: " + maintenanceOrderId));

        String type = order.getType().name(); // PREVENTIVE or CORRECTIVE
        List<PartCategory> relevantCategories = SUGGEST_MAP.getOrDefault(type,
            List.of(PartCategory.ENGINE, PartCategory.OTHER));
        String reason = SUGGEST_REASON.getOrDefault(type, "Suggested for this maintenance type");

        return partRepo.findByCategoryIn(relevantCategories).stream()
            .map(p -> new PartSuggestionDTO(
                p.getId(), p.getName(), p.getReferenceCode(),
                p.getCategory().name(), p.getStockQuantity(),
                p.getUnitCost(), p.isLowStock(), reason
            ))
            .collect(Collectors.toList());
    }

    // ── Part Usage (auto-deduct) ──────────────────────────────────

    /**
     * Records a part being used in a maintenance order.
     * Automatically deducts the quantity from stock.
     * If stock goes below minThreshold — still saves but warns via isLowStock().
     */
    @Transactional
    public MaintenancePartUsage recordUsage(PartUsageRequest request) {
        MaintenanceOrder order = maintenanceRepo.findById(request.getMaintenanceOrderId())
            .orElseThrow(() -> new RuntimeException("Maintenance order not found"));

        SparePart part = partRepo.findById(request.getSparePartId())
            .orElseThrow(() -> new RuntimeException("Spare part not found"));

        // ── Auto-deduct stock ─────────────────────────────────────
        int newStock = part.getStockQuantity() - request.getQuantityUsed();
        if (newStock < 0) newStock = 0; // no blocking, just floor at 0
        part.setStockQuantity(newStock);
        partRepo.save(part);

        // ── Snapshot price at time of use ─────────────────────────
        double unitCostSnapshot = part.getUnitCost();
        double totalCost        = Math.round(unitCostSnapshot * request.getQuantityUsed() * 100.0) / 100.0;

        MaintenancePartUsage usage = new MaintenancePartUsage();
        usage.setMaintenanceOrder(order);
        usage.setSparePart(part);
        usage.setQuantityUsed(request.getQuantityUsed());
        usage.setUnitCostAtUsage(unitCostSnapshot);
        usage.setTotalCost(totalCost);
        usage.setUsedDate(request.getUsedDate() != null
            ? request.getUsedDate()
            : LocalDate.now().toString());

        return usageRepo.save(usage);
    }

    public List<MaintenancePartUsage> getUsageByOrder(Long maintenanceOrderId) {
        return usageRepo.findByMaintenanceOrderId(maintenanceOrderId);
    }

    public List<MaintenancePartUsage> getUsageByPart(Long partId) {
        return usageRepo.findBySparePartId(partId);
    }

    public void deleteUsage(Long usageId) {
        MaintenancePartUsage usage = usageRepo.findById(usageId)
            .orElseThrow(() -> new RuntimeException("Usage record not found"));

        // ── Restore stock when usage is deleted ───────────────────
        SparePart part = usage.getSparePart();
        part.setStockQuantity(part.getStockQuantity() + usage.getQuantityUsed());
        partRepo.save(part);

        usageRepo.deleteById(usageId);
    }

    public Double getTotalCostForOrder(Long maintenanceOrderId) {
        Double total = usageRepo.sumCostByMaintenanceOrder(maintenanceOrderId);
        return total != null ? total : 0.0;
    }
}
