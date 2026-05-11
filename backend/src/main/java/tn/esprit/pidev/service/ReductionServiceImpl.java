package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.ReductionRequest;
import tn.esprit.pidev.dto.ReductionResponse;
import tn.esprit.pidev.entity.Reduction;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.repository.ReductionRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReductionServiceImpl implements IReductionService {

    private final ReductionRepository reductionRepo;
    private final UserRepository userRepo;

    public ReductionServiceImpl(ReductionRepository reductionRepo, UserRepository userRepo) {
        this.reductionRepo = reductionRepo;
        this.userRepo = userRepo;
    }

    @Override
    public ReductionResponse create(ReductionRequest request, Long operatorId) {
        User operator = getOperator(operatorId);

        // ── Input validation ──────────────────────────────────────────────────
        String trimmedCode = request.getCode() == null ? "" : request.getCode().trim().toUpperCase();
        if (trimmedCode.isEmpty()) {
            throw new RuntimeException("Promo code is required.");
        }

        // ── Remarque 2 : duplicate code check ────────────────────────────────
        if (reductionRepo.findByCode(trimmedCode).isPresent()) {
            throw new RuntimeException("A discount with code \"" + trimmedCode + "\" already exists.");
        }

        // ── Remarque 3 : percentage must be between 1 and 100 ─────────────────
        validatePercentage(request.getPourcentage());

        if (request.getDateExpiration() == null) {
            throw new RuntimeException("Expiration date is required.");
        }
        if (request.getDateExpiration().isBefore(LocalDate.now())) {
            throw new RuntimeException("Expiration date must be in the future.");
        }
        if (request.getPointsRequis() == null || request.getPointsRequis() < 0) {
            throw new RuntimeException("Required points must be >= 0.");
        }

        Reduction r = new Reduction(
                trimmedCode,
                request.getPourcentage(),
                request.getDateExpiration(),
                request.getPointsRequis(),
                operator
        );
        return ReductionResponse.fromEntity(reductionRepo.save(r));
    }

    @Override
    public ReductionResponse getById(Long id) {
        return ReductionResponse.fromEntity(findById(id));
    }

    @Override
    public ReductionResponse getByCode(String code) {
        return ReductionResponse.fromEntity(
                reductionRepo.findByCode(code.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Discount code not found: " + code))
        );
    }

    @Override
    public List<ReductionResponse> getAll() {
        return reductionRepo.findAll().stream()
                .map(ReductionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ReductionResponse update(Long id, ReductionRequest request) {
        Reduction r = findById(id);

        String trimmedCode = request.getCode() == null ? "" : request.getCode().trim().toUpperCase();
        if (trimmedCode.isEmpty()) {
            throw new RuntimeException("Promo code is required.");
        }

        // ── Remarque 2 : duplicate code check (exclude current record) ────────
        reductionRepo.findByCode(trimmedCode).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("A discount with code \"" + trimmedCode + "\" already exists.");
            }
        });

        // ── Remarque 3 : percentage must be between 1 and 100 ─────────────────
        validatePercentage(request.getPourcentage());

        if (request.getDateExpiration() == null) {
            throw new RuntimeException("Expiration date is required.");
        }
        if (request.getPointsRequis() == null || request.getPointsRequis() < 0) {
            throw new RuntimeException("Required points must be >= 0.");
        }

        r.setCode(trimmedCode);
        r.setPourcentage(request.getPourcentage());
        r.setDateExpiration(request.getDateExpiration());
        r.setPointsRequis(request.getPointsRequis());
        return ReductionResponse.fromEntity(reductionRepo.save(r));
    }

    @Override
    public void delete(Long id) {
        findById(id);
        reductionRepo.deleteById(id);
    }

    @Override
    public List<ReductionResponse> getValides() {
        return reductionRepo.findByDateExpirationAfter(LocalDate.now()).stream()
                .map(ReductionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReductionResponse> getAccessibles(Integer points) {
        return reductionRepo.findByPointsRequisLessThanEqual(points).stream()
                .map(ReductionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReductionResponse> getByOperator(Long operatorId) {
        return reductionRepo.findByCreatedById(operatorId).stream()
                .map(ReductionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validatePercentage(Double pct) {
        if (pct == null) {
            throw new RuntimeException("Discount percentage is required.");
        }
        if (pct < 1 || pct > 100) {
            throw new RuntimeException("Discount percentage must be between 1 and 100.");
        }
    }

    private Reduction findById(Long id) {
        return reductionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));
    }

    private User getOperator(Long operatorId) {
        User user = userRepo.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("User not found: " + operatorId));
        if (user.getRole() != RoleEnum.OPERATOR && user.getRole() != RoleEnum.ADMIN) {
            throw new RuntimeException("Access denied: only OPERATOR and ADMIN can manage discounts.");
        }
        return user;
    }
}
