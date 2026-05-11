package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.PointTransaction;
import tn.esprit.pidev.repository.PointTransactionRepository;

import java.util.List;

@RestController
@RequestMapping("/api/point-transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Point transactions", description = "Historique des mouvements de points fidélité")
public class PointTransactionController {

    private final PointTransactionRepository repository;

    public PointTransactionController(PointTransactionRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Transactions liées à un compte fidélité")
    public ResponseEntity<List<PointTransaction>> byAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(repository.findByLoyaltyAccountId(accountId));
    }
}
