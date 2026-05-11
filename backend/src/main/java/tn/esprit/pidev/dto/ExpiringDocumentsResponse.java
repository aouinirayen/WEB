package tn.esprit.pidev.dto;

import org.springframework.data.domain.Page;
import tn.esprit.pidev.entity.LegalDocument;

import java.util.List;

/**
 * Response object for expiring documents endpoints
 * Includes both paginated documents and expiry statistics
 */
public class ExpiringDocumentsResponse {
    private List<LegalDocument> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private boolean first;
    private boolean last;
    private ExpiryStatsDTO expiryStats;

    // Constructors
    public ExpiringDocumentsResponse() {}

    public ExpiringDocumentsResponse(Page<LegalDocument> page, ExpiryStatsDTO expiryStats) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.expiryStats = expiryStats;
    }

    // Getters and Setters
    public List<LegalDocument> getContent() {
        return content;
    }

    public void setContent(List<LegalDocument> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public ExpiryStatsDTO getExpiryStats() {
        return expiryStats;
    }

    public void setExpiryStats(ExpiryStatsDTO expiryStats) {
        this.expiryStats = expiryStats;
    }
}

