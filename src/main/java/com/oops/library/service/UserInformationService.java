package com.oops.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oops.library.repository.BookRepository;
import com.oops.library.repository.BorrowLogRepository;
import com.oops.library.repository.UserRepository;

import jakarta.persistence.EntityManager;

import com.oops.library.enchanted.exception.EnchantedLibraryException;
import com.oops.library.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class UserInformationService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordResetService passwordResetService;
	
	@Autowired
	private BorrowLogRepository borrowLogRepository;
	
	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private EntityManager entityManager;
	
//	public List<User> getAllRegisteredUsers() {
//	    return userRepository.findAll();
//	}
	
	public List<User> getAllRegisteredUsers() {
        List<User> allUsers = userRepository.findAll();
        System.out.println("Total users in DB: " + allUsers.size()); // Add this for debugging
        for (User user : allUsers) {
            System.out.println("User: " + user.getName() + ", Email: " + user.getEmail() + ", Role: " + user.getRole());
        }
        return allUsers;
    }


	
	public User findByEmail(String email) throws EnchantedLibraryException {
		Optional<User> foundUser = userRepository.findByEmail(email);
		if(foundUser.isEmpty()) {
			throw new EnchantedLibraryException("There is no user with this email");
		}
		return foundUser.get();
	}

	public User findById(Long userId) throws EnchantedLibraryException {
		Optional<User> user = userRepository.findById(userId);
		if (user.isEmpty()) {
			throw new EnchantedLibraryException("User not found with id: " + userId);
		}
		return user.get();
	}

	@Autowired
	private FileStorageService fileStorageService;

//	@Transactional
//	public void deleteUser(Long userId) throws EnchantedLibraryException {
//		User user = findById(userId);
//		if (user == null) {
//			throw new EnchantedLibraryException("User not found with id: " + userId);
//		}
//
//		// Delete profile image if exists
//		if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
//			fileStorageService.deleteFile(user.getProfileImagePath());
//		}
//
//		userRepository.delete(user);
//	}
	
//	@Transactional
//	public void deleteUser(Long userId) throws EnchantedLibraryException {
//	    User user = findById(userId);
//	    if (user == null) {
//	        throw new EnchantedLibraryException("User not found with id: " + userId);
//	    }
//
//	    // 1. Delete password reset tokens first (if you have PasswordResetToken entity)
//	    try {
//	        // If you have PasswordResetTokenRepository
//	        // passwordResetTokenRepository.deleteByUser(user);
//	        
//	        // OR using native query if you don't have repository method
//	        entityManager.createQuery("DELETE FROM PasswordResetToken prt WHERE prt.user = :user")
//	                    .setParameter("user", user)
//	                    .executeUpdate();
//	    } catch (Exception e) {
//	        System.out.println("No password reset tokens found or error deleting: " + e.getMessage());
//	    }
//	    
//	    // 2. Delete associated borrow logs
//	    try {
//	        borrowLogRepository.deleteByUser(user);
//	    } catch (Exception e) {
//	        System.out.println("No borrow logs found or error deleting: " + e.getMessage());
//	    }
//
//	    // 3. Delete profile image if exists
//	    if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
//	        fileStorageService.deleteFile(user.getProfileImagePath());
//	    }
//
//	    // 4. Finally delete the user
//	    userRepository.delete(user);
//	}
//	@Transactional
//	public void deleteUser(Long userId) throws EnchantedLibraryException {
//	    User user = findById(userId);
//	    if (user == null) {
//	        throw new EnchantedLibraryException("User not found with id: " + userId);
//	    }
//
//	    // 1. Delete password reset tokens first (using native query)
//	    try {
//	        // Check if you have PasswordResetToken entity
//	        // If yes, uncomment and use appropriate method
//	        // passwordResetTokenRepository.deleteByUser(user);
//	        
//	        // OR use entity manager for native query
//	        EntityManager entityManager = null; // You need to inject this
//	        
//	        // Native SQL to delete password reset tokens
//	        entityManager.createNativeQuery("DELETE FROM password_reset_tokens WHERE user_id = :userId")
//	                    .setParameter("userId", userId)
//	                    .executeUpdate();
//	                    
//	    } catch (Exception e) {
//	        System.out.println("Note: No password reset tokens found or error deleting: " + e.getMessage());
//	    }
//	    
//	    // 2. Delete associated borrow logs (FIXED - use correct method name)
//	    try {
//	        // Use the correct method name from BorrowLogRepository
//	        List<BorrowLog> userBorrowLogs = borrowLogRepository.findByBorrower(user);
//	        if (!userBorrowLogs.isEmpty()) {
//	            borrowLogRepository.deleteAll(userBorrowLogs);
//	        }
//	    } catch (Exception e) {
//	        System.out.println("Note: No borrow logs found or error deleting: " + e.getMessage());
//	    }
//
//	    // 3. Delete profile image if exists
//	    if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
//	        fileStorageService.deleteFile(user.getProfileImagePath());
//	    }
//
//	    // 4. Finally delete the user
//	    userRepository.delete(user);
//	}
	
	@Transactional
	public void deleteUser(Long userId) throws EnchantedLibraryException {
	    User user = findById(userId);
	    
	    // 1. Delete password reset tokens
	    try {
	        // Use native query for password_reset_tokens table
	        entityManager.createNativeQuery("DELETE FROM password_reset_tokens WHERE user_id = :userId")
	                    .setParameter("userId", userId)
	                    .executeUpdate();
	    } catch (Exception e) {
	        System.out.println("Note deleting password tokens: " + e.getMessage());
	    }
	    
	    // 2. Delete borrow logs - FIXED: use deleteByBorrower
	    try {
	        borrowLogRepository.deleteByBorrower(user);
	    } catch (Exception e) {
	        System.out.println("Note deleting borrow logs: " + e.getMessage());
	    }
	    
	    // 3. Delete profile image
	    if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
	        fileStorageService.deleteFile(user.getProfileImagePath());
	    }
	    
	    // 4. Delete user
	    userRepository.delete(user);
	}
	
	@Transactional
    public void triggerPasswordResetForUser(Long userId) throws EnchantedLibraryException {
        User user = findById(userId);
        
        if (user == null) {
            throw new EnchantedLibraryException("User not found with id: " + userId);
        }
        
        // Use existing password reset service
        passwordResetService.requestPasswordReset(user.getEmail());
        
        // Log this action (you can create an AdminActionLog service later)
        System.out.println("Admin triggered password reset for user: " + user.getEmail() + 
                          " at " + LocalDateTime.now());
        
        // Optional: Send admin confirmation email
        // emailService.sendAdminActionConfirmation(adminEmail, "Password reset triggered for " + user.getEmail());
    }
	
	public UserDetailsDTO getUserDetails(Long userId) throws EnchantedLibraryException {
	    User user = findById(userId);
	    
	    UserDetailsDTO dto = new UserDetailsDTO();
	    dto.setId(user.getId());
	    dto.setName(user.getName());
	    dto.setEmail(user.getEmail());
	    dto.setRole(user.getRole() != null ? user.getRole().name() : "N/A");
	    dto.setUserType(user.getType());
	    dto.setProfileImagePath(user.getProfileImagePath());
	    
	    // Get borrowing statistics
	    List<BorrowLog> allBorrowings = borrowLogRepository.findByBorrower(user);
	    List<BorrowLog> currentBorrowings = borrowLogRepository.findByBorrowerAndReturnedFalse(user);
	    
	    dto.setTotalBooksBorrowed(allBorrowings.size());
	    dto.setCurrentlyBorrowedCount(currentBorrowings.size());
	    
	    // Calculate overdue books count
	    int overdueCount = 0;
	    List<BookBorrowingDTO> currentBorrowingDTOs = new ArrayList<>();
	    
	    for (BorrowLog log : currentBorrowings) {
	        BookBorrowingDTO borrowingDTO = new BookBorrowingDTO();
	        borrowingDTO.setBookId(log.getBook().getId());
	        borrowingDTO.setBookTitle(log.getBook().getTitle());
	        borrowingDTO.setAuthor(log.getBook().getAuthor());
	        borrowingDTO.setBorrowDate(log.getBorrowDate());
	        borrowingDTO.setDueDate(log.getReturnDate());
	        
	        boolean isOverdue = log.getReturnDate().isBefore(LocalDateTime.now());
	        borrowingDTO.setOverdue(isOverdue);
	        
	        if (isOverdue) {
	            overdueCount++;
	        }
	        
	        currentBorrowingDTOs.add(borrowingDTO);
	    }
	    
	    // SET THE OVERDUE COUNT
	    dto.setOverdueBooksCount(overdueCount);
	    dto.setCurrentBorrowings(currentBorrowingDTOs);
	    
	    return dto;
	}
	
	public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        long totalUsers = userRepository.count();
        long totalBooks = bookRepository.count();
        
        stats.put("totalUsers", totalUsers);
        stats.put("totalBooks", totalBooks);
        
        // User role distribution
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> userRoleDistribution = new HashMap<>();
        for (User user : allUsers) {
            String role = user.getRole() != null ? user.getRole().name() : "UNKNOWN";
            userRoleDistribution.put(role, userRoleDistribution.getOrDefault(role, 0L) + 1);
        }
        stats.put("userRoleDistribution", userRoleDistribution);
        
        // User type distribution
        Map<String, Long> userTypeDistribution = new HashMap<>();
        for (User user : allUsers) {
            String type = user.getType();
            userTypeDistribution.put(type, userTypeDistribution.getOrDefault(type, 0L) + 1);
        }
        stats.put("userTypeDistribution", userTypeDistribution);
        
        // Book status distribution
        List<Book> allBooks = bookRepository.findAll();
        Map<String, Long> bookStatusDistribution = new HashMap<>();
        for (Book book : allBooks) {
            String status = book.getStatus() != null ? book.getStatus().name() : "UNKNOWN";
            bookStatusDistribution.put(status, bookStatusDistribution.getOrDefault(status, 0L) + 1);
        }
        stats.put("bookStatusDistribution", bookStatusDistribution);
        
        // Active borrowings count
        long activeBorrowings = borrowLogRepository.countByReturnedFalse();
        stats.put("activeBorrowings", activeBorrowings);
        
        // Overdue books count (simplified)
        long overdueBooks = borrowLogRepository.countByReturnedFalseAndReturnDateBefore(LocalDateTime.now());
        stats.put("overdueBooks", overdueBooks);
        
        // New registrations in last 30 days (if you have createdAt field)
        // LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        // long newRegistrations = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        // stats.put("newRegistrationsLast30Days", newRegistrations);
        
        // Registration trends for chart (last 7 days - placeholder)
        Map<String, Long> registrationTrends = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            registrationTrends.put(date.format(formatter), 0L); // Placeholder
        }
        stats.put("registrationTrends", registrationTrends);
        
        // Borrowing trends for chart (last 6 months - placeholder)
        Map<String, Long> borrowingTrends = new LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int currentMonth = LocalDate.now().getMonthValue() - 1;
        for (int i = 5; i >= 0; i--) {
            int monthIndex = (currentMonth - i + 12) % 12;
            borrowingTrends.put(months[monthIndex], 0L); // Placeholder
        }
        stats.put("borrowingTrends", borrowingTrends);
        
        return stats;
    }
}
