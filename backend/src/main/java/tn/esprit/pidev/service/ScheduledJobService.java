package tn.esprit.pidev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.entity.Organization;
import tn.esprit.pidev.entity.Partner;
import tn.esprit.pidev.repository.PartnerContractRepository;
import tn.esprit.pidev.service.admin.LegalDocumentService;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for scheduled jobs (expiry checks, notifications, contract reminders, audit log cleanup)
 */
@Service
public class ScheduledJobService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledJobService.class);

    @Autowired
    private LegalDocumentService legalDocumentService;

    @Autowired
    private PartnerContractRepository contractRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${app.audit-log.retention-days:7}")
    private int auditLogRetentionDays;

    /**
     * Runs daily at 00:00 to check for expired documents
     * Cron: 0 0 0 * * * (at 00:00 every day)
     */
    @Scheduled(cron = "${app.scheduled.expiry-check.cron:0 0 0 * * *}")
    public void checkExpiredDocumentsJob() {
        logger.info("========== SCHEDULED JOB: Check Expired Documents ==========");
        try {
            legalDocumentService.checkAndUpdateExpiredDocuments();
            logger.info("✓ Expiry check job completed successfully");
        } catch (Exception e) {
            logger.error("✗ Error in expiry check job: {}", e.getMessage(), e);
        }
    }

    /**
     * Runs daily at 10:00 to send expiry notifications
     * Cron: 0 0 10 * * * (at 10:00 every day)
     */
    @Scheduled(cron = "${app.scheduled.expiry-notification.cron:0 0 10 * * *}")
    public void sendExpiryNotificationsJob() {
        logger.info("========== SCHEDULED JOB: Send Expiry Notifications ==========");
        try {
            legalDocumentService.sendExpiryNotifications();
            logger.info("✓ Expiry notification job completed successfully");
        } catch (Exception e) {
            logger.error("✗ Error in expiry notification job: {}", e.getMessage(), e);
        }
    }

    /**
     * Runs daily at 09:00 - Check contracts expiring in 30, 15, 7, or ≤3 days
     * Cron: 0 0 9 * * * (at 09:00 every day)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringContractsJob() {
        logger.info("========== SCHEDULED JOB: Check Expiring Contracts ==========");
        try {
            Date now = new Date();
            List<PartnerContract> allContracts = contractRepository.findAll();
            int remindersSent = 0;

            for (PartnerContract contract : allContracts) {
                if (contract.getEndDate() == null) continue;

                long daysLeft = TimeUnit.MILLISECONDS.toDays(
                        contract.getEndDate().getTime() - now.getTime()
                );

                if (daysLeft == 30 || daysLeft == 15 || daysLeft == 7 || daysLeft <= 3) {
                    sendContractExpiryReminder(contract, (int) daysLeft);
                    remindersSent++;
                }
            }

            logger.info("Contract expiry reminders sent: {}", remindersSent);
        } catch (Exception e) {
            logger.error("Error in contract expiry check job: {}", e.getMessage(), e);
        }
    }

    /**
     * Runs daily at 01:00 to delete old activity logs
     * Cron: 0 0 1 * * * (at 01:00 every day)
     * By default, deletes audit logs older than 7 days
     */
    @Scheduled(cron = "${app.scheduled.audit-log-cleanup.cron:0 0 1 * * *}")
    public void cleanupOldAuditLogsJob() {
        logger.info("========== SCHEDULED JOB: Cleanup Old Audit Logs (Retention: {} days) ==========", auditLogRetentionDays);
        try {
            int deletedCount = auditLogService.deleteOldAuditLogs(auditLogRetentionDays);
            logger.info("✓ Audit log cleanup job completed successfully - Deleted {} old activity logs", deletedCount);
        } catch (Exception e) {
            logger.error("✗ Error in audit log cleanup job: {}", e.getMessage(), e);
        }
    }

    private void sendContractExpiryReminder(PartnerContract contract, int daysLeft) {
        try {
            Partner partner = contract.getPartner();
            Organization org = contract.getOrganization();

            String partnerName = partner != null ? partner.getName() : "N/A";
            String partnerEmail = partner != null ? partner.getEmail() : null;
            String orgName = org != null ? org.getName() : "N/A";

            logger.info("Sending expiry reminder for contract {} - {} days left - Partner: {}",
                    contract.getId(), daysLeft, partnerName);

            if (partnerEmail != null && !partnerEmail.isEmpty()) {
                emailService.sendContractExpiryReminderEmail(
                        partnerEmail,
                        partnerName,
                        orgName,
                        contract.getId(),
                        daysLeft,
                        contract.getEndDate()
                );
            }
        } catch (Exception e) {
            logger.error("Error sending expiry reminder for contract {}: {}",
                    contract.getId(), e.getMessage());
        }
    }
}