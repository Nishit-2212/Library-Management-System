package com.oops.library.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.oops.library.entity.Book;
import com.oops.library.entity.Role;
import com.oops.library.entity.User;
import com.oops.library.entity.AncientScript;
import com.oops.library.service.BookService;
import com.oops.library.service.FileStorageService;
import com.oops.library.service.UserInformationService;
import com.oops.library.entity.User;
import com.oops.library.service.BookService;
import com.oops.library.service.UserInformationService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserInformationService userService;

    @Autowired
    private BookService bookService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        try {
            // Get all users
            List<User> users = userService.getAllRegisteredUsers();
            
            // Get user statistics
            Map<Role, Long> userStats = users.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
            
            // Get all books
            List<Book> books = bookService.getAllBooks();
            
            model.addAttribute("users", users);
            model.addAttribute("userStats", userStats);
            model.addAttribute("books", books);
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("totalBooks", books.size());
            
            return "admin-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin-dashboard";
        }
    }

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, Model model) {
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

            return "redirect:/admin/dashboard?userDeleted=true";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete user: " + e.getMessage());
            return "redirect:/admin/dashboard?error=true";
        }
    }

    @PostMapping("/books/{bookId}/delete")
    public String deleteBook(@PathVariable Long bookId, Model model) {
        try {
            bookService.deleteBook(bookId);
            return "redirect:/admin/dashboard?bookDeleted=true";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete book: " + e.getMessage());
            return "redirect:/admin/dashboard?error=true";
        }
    }
}