package com.oops.library.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oops.library.entity.UserDetailsDTO; // Check if this is correct package
import com.oops.library.entity.Book;
import com.oops.library.entity.Role;
import com.oops.library.entity.User;
import com.oops.library.enchanted.exception.EnchantedLibraryException;
import com.oops.library.service.BookService;
import com.oops.library.service.FileStorageService;
import com.oops.library.service.UserInformationService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserInformationService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        System.out.println("========== ADMIN DASHBOARD METHOD CALLED ==========");
        
        try {
            // Get all users
            List<User> users = userService.getAllRegisteredUsers();
            System.out.println("Fetched " + users.size() + " users");
            
            // Get all books
            List<Book> books = bookService.getAllBooks();
            System.out.println("Fetched " + books.size() + " books");
            
            // Get user statistics
            Map<Role, Long> userStats = users.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
            
            // Get enhanced dashboard statistics
            Map<String, Object> dashboardStats = userService.getDashboardStatistics();
            
            // Add attributes
            model.addAttribute("users", users);
            model.addAttribute("books", books);
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("totalBooks", books.size());
            model.addAttribute("userStats", userStats);
            model.addAttribute("dashboardStats", dashboardStats);
            
            return "admin-dashboard";
        } catch (Exception e) {
            System.out.println("ERROR in admin dashboard: " + e.getMessage());
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin-dashboard";
        }
    }

    /**
     * Trigger password reset for a user
     */
    @PostMapping("/users/{userId}/trigger-reset")
    public String triggerPasswordReset(@PathVariable Long userId, 
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.triggerPasswordResetForUser(userId);
            redirectAttributes.addFlashAttribute("success", 
                "Password reset email has been sent to the user.");
        } catch (EnchantedLibraryException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to trigger password reset: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users/{userId}/details")
    public String getUserDetails(@PathVariable Long userId, Model model) {
        System.out.println("====== DEBUG: getUserDetails START ======");
        System.out.println("User ID: " + userId);
        
        try {
            System.out.println("1. Calling userService.getUserDetails...");
            UserDetailsDTO userDetails = userService.getUserDetails(userId);
            System.out.println("2. User details fetched: " + userDetails.getName());
            
            System.out.println("3. Adding to model...");
            model.addAttribute("userDetails", userDetails);
            
            System.out.println("4. Returning fragment...");
            return "admin-user-details :: userDetailsModal";
            
        } catch (Exception e) {
            System.out.println("ERROR in getUserDetails: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load user details: " + e.getMessage());
            return "admin-user-details :: error";
        } finally {
            System.out.println("====== DEBUG: getUserDetails END ======");
        }
    }

    /**
     * Test endpoint for debugging
     */
    @GetMapping("/users/test/details")
    @ResponseBody
    public String testUserDetails() {
        System.out.println("TEST: User details endpoint called");
        return "Test successful - endpoint is working";
    }

    /**
     * Get statistics data for charts (AJAX endpoint)
     */
    @GetMapping("/statistics/data")
    @ResponseBody
    public Map<String, Object> getStatisticsData() {
        return userService.getDashboardStatistics();
    }

    /**
     * Delete user (existing - keep as is)
     */
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, 
                             RedirectAttributes redirectAttributes) {
        try {
            // Get user details before deletion to access profile image path
            User user = userService.findById(userId);
            String profileImagePath = user.getProfileImagePath();

            // Delete the user first
            userService.deleteUser(userId);

            // If user had a profile image, delete it using FileStorageService
            if (profileImagePath != null && !profileImagePath.isEmpty()) {
                fileStorageService.deleteFile(profileImagePath);
            }

            redirectAttributes.addFlashAttribute("success", 
                "User deleted successfully.");
            return "redirect:/admin/dashboard?userDeleted=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to delete user: " + e.getMessage());
            return "redirect:/admin/dashboard?error=true";
        }
    }

    /**
     * Delete book (existing - keep as is)
     */
    @PostMapping("/books/{bookId}/delete")
    public String deleteBook(@PathVariable Long bookId, 
                             RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(bookId);
            redirectAttributes.addFlashAttribute("success", 
                "Book deleted successfully.");
            return "redirect:/admin/dashboard?bookDeleted=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to delete book: " + e.getMessage());
            return "redirect:/admin/dashboard?error=true";
        }
    }
}