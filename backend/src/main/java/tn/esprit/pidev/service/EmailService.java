package tn.esprit.pidev.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Service for sending emails (password reset, notifications, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final JavaMailSender mailSender;
    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@transittn.tn}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Send contract expiry reminder email
     */
    @Async
    public void sendContractExpiryReminderEmail(String toEmail, String partnerName,
            String orgName, Long contractId, int daysLeft, java.util.Date endDate) {
        try {
            if (javaMailSender == null) {
                logger.warn("Email service not available for: {}", toEmail);
                return;
            }

            // For 3 days or less, send critical template
            if (daysLeft <= 3) {
                sendCriticalContractExpiryEmail(toEmail, partnerName, orgName, contractId, daysLeft, endDate);
                return;
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TransitTN] Contract Expiry Reminder - " + daysLeft + " days remaining");

            String urgencyColor = daysLeft <= 7 ? "#d32f2f" : daysLeft <= 15 ? "#f57c00" : "#1a73e8";
            String urgencyLabel = daysLeft <= 7 ? "URGENT" : daysLeft <= 15 ? "IMPORTANT" : "REMINDER";
            String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(endDate);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head>");
            html.append("<body style=\"font-family:Arial,sans-serif;margin:0;padding:0;background:#f5f5f5;\">");
            html.append("<div style=\"max-width:600px;margin:30px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);\">");
            html.append("<div style=\"background:linear-gradient(135deg,#1a237e,#1a73e8);padding:30px;text-align:center;\">");
            html.append("<div style=\"display:inline-block;background:white;padding:12px 20px;border-radius:8px;margin-bottom:15px;\">");
            html.append("<span style=\"color:#1a237e;font-size:1.5rem;font-weight:bold;letter-spacing:2px;\">TransitTN</span></div>");
            html.append("<h1 style=\"color:white;margin:0;font-size:1.4rem;\">National Tunisian Transport Platform</h1>");
            html.append("<p style=\"color:rgba(255,255,255,0.9);margin:8px 0 0;font-size:0.9rem;\">Contract Management System</p></div>");
            html.append("<div style=\"background:").append(urgencyColor).append(";padding:15px;text-align:center;\">");
            html.append("<span style=\"color:white;font-weight:bold;font-size:1.1rem;letter-spacing:1px;\">").append(urgencyLabel).append(" - CONTRACT EXPIRING SOON</span></div>");
            html.append("<div style=\"padding:30px;\">");
            html.append("<p style=\"font-size:1rem;color:#333;\">Dear <strong>").append(partnerName).append("</strong>,</p>");
            html.append("<p style=\"color:#555;line-height:1.6;\">We would like to inform you that your partnership contract with <strong>").append(orgName);
            html.append("</strong> will expire in <strong style=\"color:").append(urgencyColor).append(";font-size:1.1rem;\">").append(daysLeft).append(" days</strong>.</p>");
            html.append("<div style=\"background:#f8f9ff;border-left:4px solid ").append(urgencyColor).append(";border-radius:8px;padding:20px;margin:20px 0;\">");
            html.append("<h3 style=\"color:#1a237e;margin:0 0 15px;font-size:1.1rem;\">Contract Details</h3>");
            html.append("<table style=\"width:100%;border-collapse:collapse;font-size:0.95rem;\">");
            html.append("<tr><td style=\"padding:8px 0;color:#666;width:45%;\">Contract ID</td><td style=\"font-weight:bold;color:#333;\">#").append(contractId).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Organization</td><td style=\"font-weight:bold;color:#333;\">").append(orgName).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Partner</td><td style=\"font-weight:bold;color:#333;\">").append(partnerName).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Expiration Date</td><td style=\"font-weight:bold;color:").append(urgencyColor).append(";\">").append(dateStr).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Days Remaining</td><td style=\"font-weight:bold;color:").append(urgencyColor).append(";\">").append(daysLeft).append(" days</td></tr>");
            html.append("</table></div>");
            html.append("<div style=\"background:#fff8e1;border:1px solid #ffe082;border-radius:8px;padding:15px;margin:20px 0;\">");
            html.append("<p style=\"color:#e65100;margin:0;font-size:0.9rem;\"><strong>Action Required:</strong> Please contact TransitTN to renew your contract before the expiration date.</p></div>");
            html.append("<div style=\"text-align:center;margin:25px 0;\">");
            html.append("<a href=\"").append(frontendUrl).append("/admin/contracts\" style=\"background:linear-gradient(135deg,#1a73e8,#0d47a1);color:white;padding:14px 35px;border-radius:25px;text-decoration:none;font-weight:bold;display:inline-block;\">Renew Contract Now</a></div>");
            html.append("<p style=\"color:#888;font-size:0.85rem;text-align:center;border-top:1px solid #eee;padding-top:20px;margin-bottom:0;\">");
            html.append("Automated email from TransitTN.<br>&copy; 2026 TransitTN - National Tunisian Transport Platform</p>");
            html.append("</div></div></body></html>");

            helper.setText(html.toString(), true);
            javaMailSender.send(message);
            logger.info("Expiry reminder sent to: {} ({} days left)", toEmail, daysLeft);
        } catch (Exception e) {
            logger.error("Error sending expiry reminder: {}", e.getMessage());
        }
    }

    /**
     * Send CRITICAL contract expiry email (3 days or less)
     */
    public void sendCriticalContractExpiryEmail(String toEmail, String partnerName,
            String orgName, Long contractId, int daysLeft, java.util.Date endDate) {
        try {
            if (javaMailSender == null) return;

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[CRITICAL] Contract Expiring in " + daysLeft + " Days - Immediate Action Required");

            String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(endDate);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head>");
            html.append("<body style=\"font-family:Arial,sans-serif;margin:0;padding:0;background:#f5f5f5;\">");
            html.append("<div style=\"max-width:600px;margin:30px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 30px rgba(183,28,28,0.3);\">");
            html.append("<div style=\"background:linear-gradient(135deg,#b71c1c,#d32f2f);padding:35px;text-align:center;\">");
            html.append("<div style=\"display:inline-block;background:white;padding:15px 25px;border-radius:10px;margin-bottom:15px;\">");
            html.append("<span style=\"color:#b71c1c;font-size:1.8rem;font-weight:bold;letter-spacing:2px;\">TransitTN</span></div>");
            html.append("<h1 style=\"color:white;margin:0;font-size:1.8rem;letter-spacing:1px;\">URGENT ACTION REQUIRED</h1>");
            html.append("<p style=\"color:rgba(255,255,255,0.9);margin:10px 0 0;\">Contract expires in ").append(daysLeft).append(" day(s)</p></div>");
            html.append("<div style=\"background:#1a237e;padding:12px;text-align:center;\">");
            html.append("<span style=\"color:white;font-weight:bold;\">TransitTN - National Tunisian Transport Platform</span></div>");
            html.append("<div style=\"padding:35px;\">");
            html.append("<p style=\"font-size:1.05rem;color:#333;\">Dear <strong style=\"color:#b71c1c;\">").append(partnerName).append("</strong>,</p>");
            html.append("<div style=\"background:#ffebee;border:2px solid #ffcdd2;border-radius:10px;padding:20px;margin:20px 0;\">");
            html.append("<p style=\"color:#b71c1c;margin:0;font-weight:bold;font-size:1.05rem;\">CRITICAL WARNING</p>");
            html.append("<p style=\"color:#c62828;margin:8px 0 0;line-height:1.6;\">Your partnership contract with <strong>").append(orgName);
            html.append("</strong> expires in <strong>").append(daysLeft).append(" day(s)</strong>. Without immediate renewal, your TransitTN services will be <strong>automatically suspended</strong>.</p></div>");
            html.append("<div style=\"background:#f8f9ff;border-left:5px solid #b71c1c;border-radius:8px;padding:20px;margin:20px 0;\">");
            html.append("<h3 style=\"color:#1a237e;margin:0 0 15px;\">Contract #").append(contractId).append(" Details</h3>");
            html.append("<table style=\"width:100%;border-collapse:collapse;\">");
            html.append("<tr><td style=\"padding:8px 0;color:#666;width:45%;\">Organization</td><td style=\"font-weight:bold;color:#333;\">").append(orgName).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Partner</td><td style=\"font-weight:bold;color:#333;\">").append(partnerName).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Expiration Date</td><td style=\"font-weight:bold;color:#b71c1c;\">").append(dateStr).append("</td></tr>");
            html.append("<tr><td style=\"padding:8px 0;color:#666;\">Days Remaining</td><td style=\"font-weight:bold;color:#b71c1c;font-size:1.2rem;\">").append(daysLeft).append(" day(s)</td></tr>");
            html.append("</table></div>");
            html.append("<div style=\"background:#fff9c4;border:1px solid #fff176;border-radius:8px;padding:18px;margin:20px 0;\">");
            html.append("<p style=\"color:#f57f17;margin:0 0 10px;font-weight:bold;\">Consequences of Non-Renewal:</p>");
            html.append("<ul style=\"margin:0;padding-left:20px;color:#555;line-height:1.8;\">");
            html.append("<li>Suspension of TransitTN services</li>");
            html.append("<li>Contract status: EXPIRED</li>");
            html.append("<li>Removal from active partners list</li>");
            html.append("<li>Need to sign a new contract</li></ul></div>");
            html.append("<div style=\"text-align:center;margin:30px 0;\">");
            html.append("<a href=\"").append(frontendUrl).append("/admin/contracts\" style=\"background:linear-gradient(135deg,#b71c1c,#d32f2f);color:white;padding:16px 40px;border-radius:30px;text-decoration:none;font-weight:bold;display:inline-block;margin-right:10px;\">Renew Now</a>");
            html.append("<a href=\"mailto:contact@transittn.tn\" style=\"background:white;color:#1a237e;padding:16px 40px;border:2px solid #1a237e;border-radius:30px;text-decoration:none;font-weight:bold;display:inline-block;\">Contact TransitTN</a></div>");
            html.append("<p style=\"color:#888;font-size:0.85rem;text-align:center;border-top:1px solid #eee;padding-top:20px;margin-bottom:0;\">");
            html.append("TransitTN - Tunisian Startup<br>National platform for public transport management<br>");
            html.append("&copy; 2026 TransitTN - All rights reserved</p>");
            html.append("</div></div></body></html>");

            helper.setText(html.toString(), true);
            javaMailSender.send(message);
            logger.info("CRITICAL expiry email sent to: {} ({} days left)", toEmail, daysLeft);
        } catch (Exception e) {
            logger.error("Error sending critical expiry email: {}", e.getMessage());
        }
    }

    /**
     * Send password reset email
     * Send password reset email with reset link
     * @param toEmail Recipient email
     * @param username User's username
     * @param resetToken Reset token
     */
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            if (javaMailSender == null) {
                logger.warn("⚠️ Email service not configured. Password reset token for user '{}': {}", username, resetToken);
                return;
            }

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TransitTN] Password Reset Request");

            // Add headers to improve email deliverability
            message.setHeader("X-Mailer", "PIDEV");
            message.setHeader("X-Priority", "3");
            message.setHeader("X-MSMail-Priority", "Normal");
            message.setHeader("Importance", "Normal");

            String htmlContent = buildPasswordResetEmailContent(username, resetLink);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            logger.info("Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendRenewalReminderEmail(String toEmail, String passengerName,
                                         String planName, LocalDate dateFin,
                                         int daysLeft, boolean autoRenewEnabled) {
        String subject = "TransitTN — Your subscription expires in " + daysLeft + " day(s)";
        String renewalMsg = autoRenewEnabled
                ? "<div style='background:#e8f5e9;border-radius:8px;padding:12px;margin:16px 0;color:#2e7d32'>"
                  + "<strong>✓ Auto-renewal is enabled.</strong> Your subscription will be renewed automatically.</div>"
                : "<div style='background:#fff8e1;border-radius:8px;padding:12px;margin:16px 0;color:#f57f17'>"
                  + "<strong>⚠ Auto-renewal is disabled.</strong> "
                  + "<a href='" + frontendUrl + "/passenger/subscriptions'>Enable it here</a> to avoid interruption.</div>";

        String body = baseLayout(
                "Subscription expiring soon",
                passengerName,
                "<p>Your subscription to the <strong>" + planName + "</strong> plan expires on "
                        + "<strong>" + dateFin.format(FMT) + "</strong> (in <strong>" + daysLeft + " day(s)</strong>).</p>"
                        + renewalMsg
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>View my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }
    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {} — {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
    public void sendRenewalConfirmationEmail(String toEmail, String passengerName,
                                             String planName, LocalDate newDateFin) {
        String subject = "TransitTN — Your subscription has been renewed";
        String body = baseLayout(
                "Subscription renewed",
                passengerName,
                "<p>Your subscription to the <strong>" + planName + "</strong> plan has been successfully renewed.</p>"
                        + "<p>New expiry date: <strong>" + newDateFin.format(FMT) + "</strong>.</p>"
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>View my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }


    // ── HTML layout ───────────────────────────────────────────────────────────
    private String baseLayout(String title, String name, String content) {
        return """
            <div style="font-family:sans-serif;max-width:540px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;border:1px solid #e0e0e0">
              <div style="background:#1a237e;padding:20px 28px;display:flex;align-items:center;gap:12px">
                <span style="font-size:22px">🚌</span>
                <span style="color:#fff;font-size:1.2rem;font-weight:700">TransitTN</span>
              </div>
              <div style="padding:28px">
                <h2 style="color:#1a237e;margin-bottom:8px">%s</h2>
                <p>Hello <strong>%s</strong>,</p>
                %s
              </div>
              <div style="background:#f5f5f5;padding:14px 28px;font-size:0.78rem;color:#999;text-align:center">
                TransitTN — Please do not reply to this email.
              </div>
            </div>
            <style>.btn{display:inline-block;margin-top:16px;padding:10px 20px;background:#1a237e;color:#fff;border-radius:8px;text-decoration:none;font-weight:600}</style>
            """.formatted(title, name, content);
    }

    /**
     * Send password change confirmation email
     * @param toEmail Recipient email
     * @param username User's username
     */
    public void sendPasswordChangeConfirmationEmail(String toEmail, String username) {
        try {
            if (javaMailSender == null) {
                logger.warn("⚠️ Email service not configured. Password changed for user: {}", username);
                return;
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TransitTN] Password Changed Successfully");

            String html = "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif;background:#f5f5f5;padding:30px;\">"
                + "<div style=\"max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);\">"
                + "<div style=\"background:linear-gradient(135deg,#1b5e20,#2e7d32);padding:30px;text-align:center;\">"
                + "<h1 style=\"color:white;margin:0;\">TransitTN</h1>"
                + "<p style=\"color:rgba(255,255,255,0.9);margin:5px 0 0;\">Password Changed</p></div>"
                + "<div style=\"padding:30px;\">"
                + "<p>Hello,</p>"
                + "<p>Your password has been changed successfully.</p>"
                + "<div style=\"background:#e8f5e9;border:1px solid #c8e6c9;border-radius:8px;padding:15px;margin:20px 0;\">"
                + "<p style=\"color:#1b5e20;margin:0;font-weight:bold;\">If you did not make this change, please contact us immediately.</p></div>"
                + "<p style=\"color:#888;font-size:0.85rem;text-align:center;border-top:1px solid #eee;padding-top:20px;\">&copy; 2026 TransitTN</p>"
                + "</div></div></body></html>";

            helper.setText(html, true);
            javaMailSender.send(message);
            logger.info("Password change confirmation email sent to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send password change confirmation email to: {}", toEmail, e);
        }
    }

    /**
     * Build HTML content for password reset email
     * Simplified to avoid Gmail spam filters while maintaining professional appearance
     */
    private String buildPasswordResetEmailContent(String username, String resetLink) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f0f2f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "    <center style=\"width: 100%; table-layout: fixed; background-color: #f0f2f5;\">\n" +
                "        <table align=\"center\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width: 100%; max-width: 600px; background-color: #ffffff; border-collapse: collapse; mso-table-lspace: 0; mso-table-rspace: 0;\">\n" +
                "            <tr>\n" +
                "                <td style=\"padding: 0;\">\n" +
                "                    <!-- Header -->\n" +
                "                    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #3b3b8f; border-collapse: collapse;\">\n" +
                "                        <tr>\n" +
                "                            <td align=\"center\" style=\"padding: 40px 30px;\">\n" +
                "                                <h1 style=\"margin: 0 0 8px 0; font-size: 28px; font-weight: 700; color: #ffffff;\">Password Reset</h1>\n" +
                "                                <p style=\"margin: 0; font-size: 16px; color: #c7d2fe;\">Secure your account</p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </table>\n" +
                "                    \n" +
                "                    <!-- Content -->\n" +
                "                    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse: collapse;\">\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 30px 30px 20px 30px;\">\n" +
                "                                <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.5; color: #1f2937;\">Hello <strong>" + username + "</strong>,</p>\n" +
                "                                <p style=\"margin: 0 0 25px 0; font-size: 15px; line-height: 1.6; color: #4b5563;\">We received a request to reset the password for your PIDEV account. Click the button below to create a new password:</p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <!-- Button -->\n" +
                "                        <tr>\n" +
                "                            <td align=\"center\" style=\"padding: 0 30px 20px 30px;\">\n" +
                "                                <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 0 auto; border-collapse: collapse;\">\n" +
                "                                    <tr>\n" +
                "                                        <td align=\"center\" bgcolor=\"#4f46e5\" style=\"padding: 14px 40px; border-radius: 8px; background-color: #4f46e5;\">\n" +
                "                                            <a href=\"" + resetLink + "\" style=\"display: inline-block; color: #ffffff; font-size: 16px; font-weight: 600; text-decoration: none; line-height: 1.4;\">Reset Password</a>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 0 30px 15px 30px;\">\n" +
                "                                <p style=\"margin: 0 0 10px 0; font-size: 13px; font-weight: 600; color: #6b7280;\">Or copy this link ↓</p>\n" +
                "                                <p style=\"margin: 0; padding: 12px; background-color: #f3f4f6; border-left: 4px solid #4f46e5; font-size: 12px; word-break: break-all; color: #374151; font-family: monospace;\">" + resetLink + "</p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <!-- Time Limit Warning -->\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 15px 30px 0 30px;\">\n" +
                "                                <table width=\"100%\" cellpadding=\"12\" cellspacing=\"0\" border=\"0\" style=\"background-color: #fffbeb; border-left: 4px solid #f59e0b; border-collapse: collapse;\">\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 12px;\">\n" +
                "                                            <p style=\"margin: 0; font-size: 13px; color: #92400e;\"><strong style=\"color: #d97706;\">⏱️ Time Limit:</strong> This link will expire in 24 hours.</p>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <!-- Security Notice -->\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 15px 30px 0 30px;\">\n" +
                "                                <table width=\"100%\" cellpadding=\"12\" cellspacing=\"0\" border=\"0\" style=\"background-color: #eff6ff; border-left: 4px solid #3b82f6; border-collapse: collapse;\">\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 12px;\">\n" +
                "                                            <p style=\"margin: 0; font-size: 13px; color: #1e40af;\"><strong style=\"color: #2563eb;\">🔒 Security:</strong> If you didn't request this, ignore this email. Your account is secure.</p>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        \n" +
                "                        <!-- Footer -->\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 30px 30px 40px 30px;\">\n" +
                "                                <hr style=\"border: none; border-top: 1px solid #e5e7eb; margin: 0 0 25px 0;\">\n" +
                "                                <p style=\"margin: 0 0 5px 0; text-align: center; font-size: 13px; font-weight: 600; color: #4f46e5;\">PIDEV</p>\n" +
                "                                <p style=\"margin: 0 0 5px 0; text-align: center; font-size: 12px; color: #6b7280;\">Security Team</p>\n" +
                "                                <p style=\"margin: 15px 0 0 0; text-align: center; font-size: 11px; color: #9ca3af;\">This is an automated email. Please do not reply.</p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </table>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "    </center>\n" +
                "</body>\n" +
                "</html>";
    }

    public void sendActionPromoEmail(String toEmail, String passengerName,
                                     String promoCode, double discountPct,
                                     String actionMessage, LocalDate expiryDate) {
        String subject = "TransitTN — An exclusive offer just for you 🎁";
        String body = baseLayout(
                "Your personal promo code",
                passengerName,
                "<p>" + actionMessage + "</p>"
                        + "<p>As a valued member of TransitTN, we are pleased to offer you an exclusive discount:</p>"

                        // Big promo code display
                        + "<div style='text-align:center;margin:24px 0'>"
                        + "  <div style='display:inline-block;background:#e3f2fd;border:2px dashed #1a73e8;"
                        + "       border-radius:12px;padding:16px 32px'>"
                        + "    <div style='font-size:0.8rem;color:#555;margin-bottom:6px;letter-spacing:1px'>YOUR PROMO CODE</div>"
                        + "    <div style='font-size:2rem;font-weight:700;font-family:monospace;color:#1a237e;letter-spacing:4px'>"
                        +        promoCode
                        + "    </div>"
                        + "    <div style='font-size:1.1rem;font-weight:600;color:#2e7d32;margin-top:8px'>-" + (int) discountPct + "% discount</div>"
                        + "  </div>"
                        + "</div>"

                        + "<div style='background:#fff8e1;border-radius:8px;padding:12px 16px;margin-bottom:16px'>"
                        + "  <i>⏰ Valid until <strong>" + expiryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</strong> "
                        + "  (7 days only)</i>"
                        + "</div>"

                        + "<p>Apply this code when subscribing to any available plan:</p>"
                        + "<a href='" + frontendUrl + "/passenger/plans' class='btn'>Browse plans &amp; subscribe</a>"

                        + "<p style='margin-top:20px;font-size:0.8rem;color:#999'>"
                        + "This code was generated exclusively for your account and can only be used once.</p>"
        );
        send(toEmail, subject, body);
    }
    public void sendAutoRenewalEnabledEmail(String toEmail, String passengerName,
                                            String planName, LocalDate dateFin) {
        String subject = "TransitTN — Auto-renewal enabled";
        String body = baseLayout(
                "Auto-renewal enabled",
                passengerName,
                "<p>Auto-renewal has been <strong>enabled</strong> for your <strong>" + planName + "</strong> plan.</p>"
                        + "<p>Your subscription will be automatically renewed on <strong>" + dateFin.format(FMT) + "</strong>.</p>"
                        + "<p>You will receive reminder emails 7 days and 1 day before the renewal date.</p>"
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>Manage my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }
    public void sendAutoRenewalActivatedEmail(String toEmail, String passengerName,
                                              String planName, LocalDate dateFin) {
        String subject = "TransitTN — Auto-renewal activated for your subscription";
        String body = baseLayout(
                "Auto-renewal activated",
                passengerName,
                "<p>Your subscription to the <strong>" + planName + "</strong> plan is now active.</p>"
                        + "<p>You have enabled <strong>auto-renewal</strong>. Your subscription will be automatically "
                        + "renewed on <strong>" + dateFin.format(FMT) + "</strong>.</p>"
                        + "<p>You will receive reminder emails 7 days and 1 day before the renewal date.</p>"
                        + "<p>To disable auto-renewal at any time, visit your subscriptions page:</p>"
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>Manage my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }
    // ── 3. Email when auto-renewal is toggled OFF from the subscriptions list ──
    public void sendAutoRenewalDisabledEmail(String toEmail, String passengerName,
                                             String planName, LocalDate dateFin) {
        String subject = "TransitTN — Auto-renewal disabled";
        String body = baseLayout(
                "Auto-renewal disabled",
                passengerName,
                "<p>Auto-renewal has been <strong>disabled</strong> for your <strong>" + planName + "</strong> plan.</p>"
                        + "<p>Your subscription will expire on <strong>" + dateFin.format(FMT) + "</strong> "
                        + "and will <strong>not</strong> be renewed automatically.</p>"
                        + "<p>You can re-enable it at any time from your subscriptions page:</p>"
                        + "<a href='" + frontendUrl + "/passenger/subscriptions' class='btn'>Manage my subscriptions</a>"
        );
        send(toEmail, subject, body);
    }
}


