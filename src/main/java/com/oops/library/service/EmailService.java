package com.oops.library.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String senderAddress;
    private final String baseUrl;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        @Value("${spring.mail.username:}") String senderAddress,
                        @Value("${app.base-url:http://localhost:9300}") String baseUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.senderAddress = senderAddress;
        this.baseUrl = baseUrl;
        log.info("EmailService initialized with sender: {}, baseUrl: {}", senderAddress, baseUrl);
    }

    public boolean sendLoginNotification(String recipient, String name) {
        log.info("=== STARTING LOGIN NOTIFICATION EMAIL ===");
        log.info("Recipient: {}, Name: {}", recipient, name);
        
        // First, let's test if the template exists and can be processed
        boolean templateTest = testTemplateProcessing("login-notification", name);
        if (!templateTest) {
            log.error("Template test failed! Falling back to plain text immediately.");
            return sendSimpleMessage(recipient,
                    "Login Alert",
                    "Hi " + name + ",\n\nYour Enchanted Library account was just accessed."
                            + "\nIf this wasn't you, please reset your password immediately.\n\nRegards,\nEnchanted Library Security");
        }
        
        // Try HTML template
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        
        log.info("Attempting to send HTML email...");
        boolean htmlSent = sendTemplatedMessage(recipient,
                "Login Alert",
                "login-notification",
                variables);
        
        if (htmlSent) {
            log.info("✅ HTML email sent successfully!");
            return true;
        } else {
            log.warn("❌ HTML email failed, falling back to plain text");
            return sendSimpleMessage(recipient,
                    "Login Alert",
                    "Hi " + name + ",\n\nYour Enchanted Library account was just accessed."
                            + "\nIf this wasn't you, please reset your password immediately.\n\nRegards,\nEnchanted Library Security");
        }
    }

    public boolean sendSignupConfirmation(String recipient, String name) {
        log.info("=== STARTING SIGNUP CONFIRMATION EMAIL ===");
        
        boolean templateTest = testTemplateProcessing("signup-confirmation-email", name);
        if (!templateTest) {
            return sendSimpleMessage(recipient,
                    "Welcome to the Enchanted Library",
                    "Hello " + name + ",\n\nYour Enchanted Library account is now active."
                            + "\nStart exploring magical books and resources today!\n\nHappy Reading,\nEnchanted Library Team");
        }
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        
        boolean htmlSent = sendTemplatedMessage(recipient,
                "✨ Welcome to the Enchanted Library ✨",
                "signup-confirmation-email",  // Updated template name
                variables);
        
        if (!htmlSent) {
            return sendSimpleMessage(recipient,
                    "Welcome to the Enchanted Library",
                    "Hello " + name + ",\n\nYour Enchanted Library account is now active."
                            + "\nStart exploring magical books and resources today!\n\nHappy Reading,\nEnchanted Library Team");
        }
        return true;
    }
    

    public boolean sendPasswordResetOtp(String recipient, String otp) {
        boolean templateTest = testTemplateProcessing("password-reset-otp", "Test User");
        if (!templateTest) {
            return sendSimpleMessage(recipient,
                    "Enchanted Library Password Reset OTP",
                    "Your one-time password to reset your Enchanted Library account is "
                            + otp + ".\n\nThis code expires in 10 minutes."
                            + "\n\nUse this link to reset your password: "
                            + buildResetLink(recipient)
                            + "\nEnter the OTP along with your new password." 
                            + "\n\nIf you did not request a reset, you can safely ignore this message.");
        }
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("otp", otp);
        variables.put("resetLink", buildResetLink(recipient));
        variables.put("expiryMinutes", 10);
        
        boolean htmlSent = sendTemplatedMessage(recipient,
                "Enchanted Library Password Reset OTP",
                "password-reset-otp",
                variables);
        
        if (!htmlSent) {
            return sendSimpleMessage(recipient,
                    "Enchanted Library Password Reset OTP",
                    "Your one-time password to reset your Enchanted Library account is "
                            + otp + ".\n\nThis code expires in 10 minutes."
                            + "\n\nUse this link to reset your password: "
                            + buildResetLink(recipient)
                            + "\nEnter the OTP along with your new password." 
                            + "\n\nIf you did not request a reset, you can safely ignore this message.");
        }
        return true;
    }

    public boolean sendBorrowConfirmation(String recipient, String name, String bookTitle, LocalDateTime dueDate) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("bookTitle", bookTitle);
        variables.put("dueDate", DATE_FORMAT.format(dueDate));
        
        boolean htmlSent = sendTemplatedMessage(recipient,
                "Book Borrowed Successfully",
                "borrow-confirmation",
                variables);
        
        if (!htmlSent) {
            return sendSimpleMessage(recipient,
                    "Book Borrowed Successfully",
                    "Hi " + name + ",\n\nYou have borrowed '" + bookTitle + "'."
                            + "\nPlease return it by " + DATE_FORMAT.format(dueDate) + "."
                            + "\n\nHappy Reading!\nEnchanted Library Team");
        }
        return true;
    }

    public boolean sendReturnConfirmation(String recipient, String name, String bookTitle) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("bookTitle", bookTitle);
        
        boolean htmlSent = sendTemplatedMessage(recipient,
                "Book Return Confirmation",
                "return-confirmation",
                variables);
        
        if (!htmlSent) {
            return sendSimpleMessage(recipient,
                    "Book Return Confirmation",
                    "Hi " + name + ",\n\nThank you for returning '" + bookTitle + "'."
                            + "\nWe hope you enjoyed it!\n\nSee you again soon.\nEnchanted Library Team");
        }
        return true;
    }

    public void sendOverdueSummary(List<String> librarianEmails, String summaryBody) {
        if (librarianEmails == null || librarianEmails.isEmpty()) {
            log.warn("Attempted to send overdue summary to empty email list");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("summaryBody", summaryBody);

        int successCount = 0;
        int failureCount = 0;

        for (String email : librarianEmails) {
            boolean htmlSent = sendTemplatedMessage(email,
                    "Daily Overdue Report",
                    "overdue-summary",
                    variables);
            
            boolean sent = htmlSent;
            
            if (!htmlSent) {
                sent = sendSimpleMessage(email,
                        "Daily Overdue Report",
                        summaryBody);
            }
            
            if (sent) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        log.info("Overdue summary sent: {} successful, {} failed", successCount, failureCount);
    }

    // Enhanced template message method with detailed debugging
    boolean sendTemplatedMessage(String recipient, String subject, String templateName, Map<String, Object> variables) {
        try {
            log.info("🔧 sendTemplatedMessage called for template: {}", templateName);
            
            if (recipient == null || recipient.isBlank()) {
                log.error("Invalid recipient: '{}'", recipient);
                return false;
            }

            if (!isValidEmail(recipient)) {
                log.error("Invalid email format: '{}'", recipient);
                return false;
            }

            if (mailSender == null) {
                log.error("JavaMailSender is null");
                return false;
            }

            log.info("Creating MimeMessage...");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            if (senderAddress != null && !senderAddress.isBlank() && isValidEmail(senderAddress)) {
                helper.setFrom(senderAddress);
                log.info("Sender address set to: {}", senderAddress);
            }
            
            helper.setTo(recipient.trim());
            helper.setSubject(subject);
            
            // Process Thymeleaf template
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
                log.info("Template variables: {}", variables);
            }
            
            log.info("🔄 Processing Thymeleaf template: {}", templateName);
            String htmlContent = templateEngine.process(templateName, context);
            log.info("✅ Template processed successfully! Content length: {} characters", htmlContent.length());
            log.debug("First 200 chars of content: {}", htmlContent.substring(0, Math.min(200, htmlContent.length())));
            
            helper.setText(htmlContent, true);
            
            log.info("📤 Sending HTML email to: {}", recipient);
            mailSender.send(message);
            log.info("✅ HTML email sent successfully to: {}", recipient);
            return true;
            
        } catch (Exception ex) {
            log.error("❌ FAILED to send templated email to {}: {}", recipient, ex.getMessage());
            log.error("Stack trace:", ex);
            return false;
        }
    }

    // Enhanced template testing method
    private boolean testTemplateProcessing(String templateName, String testName) {
        try {
            log.info("🧪 TESTING Template: {}", templateName);
            
            Map<String, Object> testVars = new HashMap<>();
            testVars.put("name", testName);
            
            Context context = new Context();
            testVars.forEach(context::setVariable);
            
            log.info("Attempting to process template: {}", templateName);
            String htmlContent = templateEngine.process(templateName, context);
            log.info("✅ Template '{}' processed SUCCESSFULLY! Length: {} chars", templateName, htmlContent.length());
            
            // Check if it's actually HTML content
            if (htmlContent.contains("<html") && htmlContent.contains("</html>")) {
                log.info("✅ Valid HTML content detected");
            } else {
                log.warn("⚠️  Content may not be proper HTML");
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ Template '{}' processing FAILED: {}", templateName, e.getMessage());
            log.error("Template error details:", e);
            return false;
        }
    }

    // ORIGINAL: Keep the existing simple message method unchanged
    boolean sendSimpleMessage(String recipient, String subject, String body) {
        try {
            if (recipient == null || recipient.isBlank()) {
                log.error("Attempted to send email with invalid recipient: '{}'", recipient);
                return false;
            }

            if (!isValidEmail(recipient)) {
                log.error("Invalid email format for recipient: '{}'", recipient);
                return false;
            }

            if (mailSender == null) {
                log.error("JavaMailSender is not initialized - cannot send email to: {}", recipient);
                return false;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            
            if (senderAddress != null && !senderAddress.isBlank() && isValidEmail(senderAddress)) {
                message.setFrom(senderAddress);
            }
            
            message.setTo(recipient.trim());
            message.setSubject(subject);
            message.setText(body);
            
            log.info("Sending PLAIN TEXT email to: {}", recipient);
            mailSender.send(message);
            log.info("Plain text email sent successfully to: {}", recipient);
            return true;
            
        } catch (MailException ex) {
            log.error("MailException sending email to {}: {}", recipient, ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("Unexpected error sending email to {}: {}", recipient, ex.getMessage());
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private String buildResetLink(String email) {
        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            return baseUrl + "/reset-password?email=" + encodedEmail;
        } catch (Exception e) {
            log.error("Error building reset link for email: {}", email, e);
            return baseUrl + "/reset-password";
        }
    }

    public boolean isHealthy() {
        try {
            return mailSender != null;
        } catch (Exception e) {
            log.warn("Email service health check failed: {}", e.getMessage());
            return false;
        }
    }
}