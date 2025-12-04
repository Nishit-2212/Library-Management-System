package com.oops.library.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oops.library.entity.BorrowLog;
import com.oops.library.entity.Role;
import com.oops.library.entity.User;
import com.oops.library.repository.BorrowLogRepository;
import com.oops.library.repository.UserRepository;
import com.oops.library.strategy.LateFeeService;

@Component
public class OverdueNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueNotificationScheduler.class);
    
    private final BorrowLogRepository borrowLogRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final LateFeeService lateFeeService;

    public OverdueNotificationScheduler(BorrowLogRepository borrowLogRepository,
                                        UserRepository userRepository,
                                        EmailService emailService,
                                        LateFeeService lateFeeService) {  // Added LateFeeService parameter
        this.borrowLogRepository = borrowLogRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.lateFeeService = lateFeeService;
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void sendOverdueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<BorrowLog> overdueLogs = borrowLogRepository.findByReturnedFalseAndReturnDateBefore(now);
        
        if (overdueLogs.isEmpty()) {
            log.info("No overdue books found.");
            return;
        }

        // Group overdue logs by user for personalized emails
        Map<User, List<BorrowLog>> overdueByUser = overdueLogs.stream()
                .collect(Collectors.groupingBy(BorrowLog::getBorrower));

        // Send personalized reminders to each user
        sendUserReminders(overdueByUser, now);

        // Send summary to librarians
        sendLibrarianSummary(overdueLogs, overdueByUser.size());
    }

    private void sendUserReminders(Map<User, List<BorrowLog>> overdueByUser, LocalDateTime now) {
        int remindersSent = 0;
        
        for (Map.Entry<User, List<BorrowLog>> entry : overdueByUser.entrySet()) {
            User user = entry.getKey();
            List<BorrowLog> userOverdueLogs = entry.getValue();
            
            try {
                // Calculate total late fee for this user
                double totalLateFee = userOverdueLogs.stream()
                        .mapToDouble(lateFeeService::calculateLateFee)
                        .sum();
                
                // Prepare template variables
                Map<String, Object> variables = new HashMap<>();
                variables.put("userName", user.getName());
                variables.put("overdueBooks", userOverdueLogs);
                variables.put("totalLateFee", totalLateFee);
                variables.put("currentDate", now.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                variables.put("now", now);
                
                // Send templated email to user
                boolean sent = emailService.sendTemplatedMessage(
                    user.getEmail(),
                    "📚 Overdue Book Reminder - Action Required",
                    "overdue-reminder-user",  // We'll create this template
                    variables
                );
                
                if (sent) {
                    remindersSent++;
                    log.info("✅ Sent overdue reminder to user: {} ({})", user.getName(), user.getEmail());
                } else {
                    log.warn("⚠️ Failed to send templated email to user: {} ({}), falling back to plain text", 
                            user.getName(), user.getEmail());
                    
                    // Fallback to plain text
                    sendPlainTextUserReminder(user, userOverdueLogs, totalLateFee, now);
                }
                
            } catch (Exception e) {
                log.error("❌ Failed to send reminder to user: {} ({})", user.getName(), user.getEmail(), e);
            }
        }
        
        log.info("📧 Successfully sent {} user reminders", remindersSent);
    }

    private void sendPlainTextUserReminder(User user, List<BorrowLog> userOverdueLogs, double totalLateFee, LocalDateTime now) {
        String userSummary = userOverdueLogs.stream()
                .map(log -> {
                    double lateFee = lateFeeService.calculateLateFee(log);
                    long overdueDays = ChronoUnit.DAYS.between(log.getReturnDate(), now);
                    return String.format("• %s by %s | Due: %s | Overdue: %d days | Late Fee: $%.2f",
                            log.getBook().getTitle(),
                            log.getBook().getAuthor(),
                            log.getReturnDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")),
                            overdueDays,
                            lateFee);
                })
                .collect(Collectors.joining("\n"));
        
        String userMessage = String.format(
            "Dear %s,\n\n" +
            "The following book(s) you borrowed from Enchanted Library are overdue:\n\n%s\n\n" +
            "Total Late Fee: $%.2f\n\n" +
            "Please return the book(s) as soon as possible to avoid additional charges.\n" +
            "You can return the books during library operating hours.\n\n" +
            "If you have already returned the books, please contact the library.\n\n" +
            "Thank you,\nThe Enchanted Library Team",
            user.getName(), userSummary, totalLateFee);
        
        emailService.sendSimpleMessage(
            user.getEmail(),
            "Overdue Book Reminder - Enchanted Library",
            userMessage
        );
    }

    private void sendLibrarianSummary(List<BorrowLog> overdueLogs, int usersNotified) {
        LocalDateTime now = LocalDateTime.now();
        String summary = overdueLogs.stream()
                .map(log -> {
                    double lateFee = lateFeeService.calculateLateFee(log);
                    long overdueDays = ChronoUnit.DAYS.between(log.getReturnDate(), now);
                    return String.format("• %s | Borrower: %s (%s) | Due: %s | Overdue: %d days | Late Fee: $%.2f",
                            log.getBook().getTitle(),
                            log.getBorrower().getName(),
                            log.getBorrower().getEmail(),
                            log.getReturnDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")),
                            overdueDays,
                            lateFee);
                })
                .collect(Collectors.joining("\n"));

        String librarianMessage = String.format(
            "Overdue Books Summary - %s\n\n" +
            "Total Overdue Items: %d\n" +
            "Users Notified: %d\n\n" +
            "Details:\n%s\n\n" +
            "Please follow up with borrowers if books are not returned soon.",
            now.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")),
            overdueLogs.size(),
            usersNotified,
            summary);

        List<String> librarianEmails = userRepository.findByRole(Role.LIBRARIAN).stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        emailService.sendOverdueSummary(librarianEmails, librarianMessage);
        log.info("📋 Sent overdue summary to {} librarians", librarianEmails.size());
    }
}