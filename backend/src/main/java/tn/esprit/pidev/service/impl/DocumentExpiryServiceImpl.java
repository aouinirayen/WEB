package tn.esprit.pidev.service.impl;

import tn.esprit.pidev.dto.ExpiringDocumentsResponse;
import tn.esprit.pidev.dto.ExpiryStatsDTO;
import tn.esprit.pidev.entity.LegalDocument;
import tn.esprit.pidev.repository.LegalDocumentRepository;
import tn.esprit.pidev.service.DocumentExpiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentExpiryServiceImpl implements DocumentExpiryService {

    private final LegalDocumentRepository legalDocumentRepository;

    @Override
    public ExpiringDocumentsResponse getExpiringDocuments(int days, Pageable pageable) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime futureDate = today.plusDays(days);
        Page<LegalDocument> page = legalDocumentRepository.findExpiringDocuments(today, futureDate, pageable);
        ExpiryStatsDTO stats = calculateExpiryStats();
        return new ExpiringDocumentsResponse(page, stats);
    }

    @Override
    public ExpiringDocumentsResponse getExpiredDocuments(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<LegalDocument> page = legalDocumentRepository.findAllExpiredDocuments(now, pageable);
        ExpiryStatsDTO stats = calculateExpiryStats();
        return new ExpiringDocumentsResponse(page, stats);
    }

    @Override
    public ExpiringDocumentsResponse getUserExpiringDocuments(Long userId, int days, Pageable pageable) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime futureDate = today.plusDays(days);
        Page<LegalDocument> page = legalDocumentRepository.findUserExpiringDocuments(userId, today, futureDate, pageable);
        ExpiryStatsDTO stats = calculateExpiryStats();
        return new ExpiringDocumentsResponse(page, stats);
    }

    @Override
    public ExpiringDocumentsResponse getExpiringDocumentsByType(Long documentTypeId, int days, Pageable pageable) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime futureDate = today.plusDays(days);
        Page<LegalDocument> page = legalDocumentRepository.findExpiringDocumentsByType(documentTypeId, today, futureDate, pageable);
        ExpiryStatsDTO stats = calculateExpiryStats();
        return new ExpiringDocumentsResponse(page, stats);
    }

    @Override
    public ExpiryStatsDTO getExpiryStatistics() {
        return calculateExpiryStats();
    }

    private ExpiryStatsDTO calculateExpiryStats() {
        LocalDateTime today = LocalDateTime.now();
        long within7 = legalDocumentRepository.countDocumentsExpiringWithinSevenDays(today, today.plusDays(7));
        long within30 = legalDocumentRepository.countDocumentsExpiringWithinThirtyDays(today, today.plusDays(30));
        long within90 = legalDocumentRepository.countDocumentsExpiringWithinNinetyDays(today, today.plusDays(90));
        long expired = legalDocumentRepository.countAlreadyExpiredDocuments(today);
        return new ExpiryStatsDTO(within7, within30, within90, expired);
    }
}
