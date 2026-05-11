package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.PricingPlanRequest;
import tn.esprit.pidev.dto.PricingPlanResponse;
import tn.esprit.pidev.entity.PricingPlan;
import tn.esprit.pidev.entity.PricingType;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.repository.PricingPlanRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PricingPlanServiceImpl implements IPricingPlanService {

    private final PricingPlanRepository planRepo;
    private final UserRepository userRepo;

    public PricingPlanServiceImpl(PricingPlanRepository planRepo, UserRepository userRepo) {
        this.planRepo = planRepo;
        this.userRepo = userRepo;
    }

    @Override
    public PricingPlanResponse create(PricingPlanRequest request, Long operatorId) {
        User operator = getOperator(operatorId);

        String trimmedName = request.getNom() == null ? "" : request.getNom().trim();
        if (trimmedName.isEmpty()) {
            throw new RuntimeException("Plan name is required.");
        }
        boolean nameExists = planRepo.findAll().stream()
                .anyMatch(p -> p.getNom().trim().equalsIgnoreCase(trimmedName));
        if (nameExists) {
            throw new RuntimeException("A pricing plan with the name \"" + trimmedName + "\" already exists.");
        }

        Integer dureeEnJours = request.getDureeEnJours();
        if (dureeEnJours == null || dureeEnJours <= 0) {
            throw new RuntimeException("Plan duration (in days) is required and must be greater than 0.");
        }
        if (request.getPrix() == null || request.getPrix() < 0) {
            throw new RuntimeException("Plan price is required and must be >= 0.");
        }
        if (request.getType() == null) {
            throw new RuntimeException("Plan type is required (FREE, BASIC or PREMIUM).");
        }

        PricingPlan plan = new PricingPlan();
        plan.setNom(trimmedName);
        plan.setDescription(request.getDescription());
        plan.setPrix(request.getPrix());
        plan.setDureeEnJours(dureeEnJours);
        plan.setType(request.getType());
        plan.setCreatedBy(operator);
        // ── FIX : inherit transportType from the operator ──
        plan.setTransportType(operator.getTransportType());

        return PricingPlanResponse.fromEntity(planRepo.save(plan));
    }

    @Override
    public PricingPlanResponse getById(Long id) {
        return PricingPlanResponse.fromEntity(findById(id));
    }

    @Override
    public List<PricingPlanResponse> getAll() {
        return planRepo.findAll().stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PricingPlanResponse update(Long id, PricingPlanRequest request, Long operatorId) {
        // ── FIX : keep operator reference to set transportType ──
        User operator = getOperator(operatorId);

        String trimmedName = request.getNom() == null ? "" : request.getNom().trim();
        if (trimmedName.isEmpty()) {
            throw new RuntimeException("Plan name is required.");
        }

        boolean nameExists = planRepo.findAll().stream()
                .anyMatch(p -> !p.getId().equals(id)
                        && p.getNom().trim().equalsIgnoreCase(trimmedName));
        if (nameExists) {
            throw new RuntimeException("A pricing plan with the name \"" + trimmedName + "\" already exists.");
        }

        Integer dureeEnJours = request.getDureeEnJours();
        if (dureeEnJours == null || dureeEnJours <= 0) {
            throw new RuntimeException("Plan duration (in days) is required and must be greater than 0.");
        }
        if (request.getPrix() == null || request.getPrix() < 0) {
            throw new RuntimeException("Plan price is required and must be >= 0.");
        }

        PricingPlan plan = findById(id);
        plan.setNom(trimmedName);
        plan.setDescription(request.getDescription());
        plan.setPrix(request.getPrix());
        plan.setDureeEnJours(dureeEnJours);
        plan.setType(request.getType());
        // ── FIX : inherit transportType from the operator ──
        plan.setTransportType(operator.getTransportType());

        return PricingPlanResponse.fromEntity(planRepo.save(plan));
    }

    @Override
    public void delete(Long id, Long operatorId) {
        getOperator(operatorId);
        findById(id);
        planRepo.deleteById(id);
    }

    @Override
    public List<PricingPlanResponse> getByType(PricingType type) {
        return planRepo.findByType(type).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingPlanResponse> getByMaxPrice(Double maxPrix) {
        return planRepo.findByPrixLessThanEqual(maxPrix).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingPlanResponse> getByOperator(Long operatorId) {
        return planRepo.findByCreatedById(operatorId).stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private PricingPlan findById(Long id) {
        return planRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing plan not found: " + id));
    }

    private User getOperator(Long operatorId) {
        User user = userRepo.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("User not found: " + operatorId));
        if (user.getRole() != RoleEnum.OPERATOR && user.getRole() != RoleEnum.ADMIN) {
            throw new RuntimeException("Access denied: only OPERATOR and ADMIN can manage pricing plans.");
        }
        return user;
    }
}