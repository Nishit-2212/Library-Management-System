package com.oops.library.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.oops.library.enchanted.exception.EnchantedLibraryException;
import com.oops.library.entity.Book;
import com.oops.library.entity.Guest;
import com.oops.library.entity.Role;
import com.oops.library.entity.User;
import com.oops.library.entity.UserDetailsDTO;
import com.oops.library.service.BookService;
import com.oops.library.service.FileStorageService;
import com.oops.library.service.UserInformationService;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // VERY IMPORTANT
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserInformationService userService;

    @MockBean
    private BookService bookService;

    @MockBean
    private FileStorageService fileStorageService;


    // ---------------------------------------------------------
    // 1️⃣ TEST: showAdminDashboard()
    // ---------------------------------------------------------
    @Test
    void testShowAdminDashboard() throws Exception {

        User guest = Mockito.mock(User.class);
        when(guest.getRole()).thenReturn(Role.GUEST);

        User scholar = Mockito.mock(User.class);
        when(scholar.getRole()).thenReturn(Role.SCHOLAR);

        List<User> mockUsers = Arrays.asList(guest, scholar);

        Book mockBook = Mockito.mock(Book.class);
        List<Book> mockBooks = Arrays.asList(mockBook);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBorrowed", 10);

        when(userService.getAllRegisteredUsers()).thenReturn(mockUsers);
        when(bookService.getAllBooks()).thenReturn(mockBooks);
        when(userService.getDashboardStatistics()).thenReturn(stats);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andExpect(model().attribute("totalUsers", 2))
                .andExpect(model().attribute("totalBooks", 1))
                .andExpect(model().attributeExists("userStats"))
                .andExpect(model().attributeExists("dashboardStats"));

        verify(userService).getAllRegisteredUsers();
        verify(bookService).getAllBooks();
        verify(userService).getDashboardStatistics();
    }



    // ---------------------------------------------------------
    // 2️⃣ TEST: triggerPasswordReset()
    // ---------------------------------------------------------
    @Test
    void testTriggerPasswordResetSuccess() throws Exception {

        doNothing().when(userService).triggerPasswordResetForUser(1L);

        mockMvc.perform(post("/admin/users/1/trigger-reset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        verify(userService).triggerPasswordResetForUser(1L);
    }

    @Test
    void testTriggerPasswordResetFailure() throws Exception {

        doThrow(new EnchantedLibraryException("User not found"))
                .when(userService).triggerPasswordResetForUser(1L);

        mockMvc.perform(post("/admin/users/1/trigger-reset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        verify(userService).triggerPasswordResetForUser(1L);
    }


    // ---------------------------------------------------------
    // 3️ TEST: getUserDetails()
    // ---------------------------------------------------------
    @Test
    void testGetUserDetails() throws Exception {

        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setName("Alice");

        when(userService.getUserDetails(5L)).thenReturn(dto);

        mockMvc.perform(get("/admin/users/5/details"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-details :: userDetailsModal"))
                .andExpect(model().attributeExists("userDetails"));

        verify(userService).getUserDetails(5L);
    }

    @Test
    void testGetUserDetailsError() throws Exception {

        when(userService.getUserDetails(5L)).thenThrow(new RuntimeException("Something failed"));

        mockMvc.perform(get("/admin/users/5/details"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-details :: error"))
                .andExpect(model().attributeExists("error"));

        verify(userService).getUserDetails(5L);
    }


    // ---------------------------------------------------------
    // 4️ TEST: getStatisticsData()
    // ---------------------------------------------------------
    @Test
    void testGetStatisticsData() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeUsers", 20);

        when(userService.getDashboardStatistics()).thenReturn(stats);

        mockMvc.perform(get("/admin/statistics/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeUsers").value(20));

        verify(userService).getDashboardStatistics();
    }


    // ---------------------------------------------------------
    // 5️ TEST: deleteUser()
    // ---------------------------------------------------------
    @Test
    void testDeleteUserSuccess() throws Exception {

        User mockUser = new Guest();
        mockUser.setProfileImagePath("images/profile1.jpg");

        when(userService.findById(10L)).thenReturn(mockUser);
        doNothing().when(userService).deleteUser(10L);
        doNothing().when(fileStorageService).deleteFile("images/profile1.jpg");

        mockMvc.perform(post("/admin/users/10/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard?userDeleted=true"));

        verify(userService).findById(10L);
        verify(userService).deleteUser(10L);
        verify(fileStorageService).deleteFile("images/profile1.jpg");
    }

    @Test
    void testDeleteUserFailure() throws Exception {

        when(userService.findById(10L)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/admin/users/10/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard?error=true"));

        verify(userService).findById(10L);
    }


    // ---------------------------------------------------------
    // 6️ TEST: deleteBook()
    // ---------------------------------------------------------
    @Test
    void testDeleteBookSuccess() throws Exception {

        doNothing().when(bookService).deleteBook(20L);

        mockMvc.perform(post("/admin/books/20/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard?bookDeleted=true"));

        verify(bookService).deleteBook(20L);
    }

    @Test
    void testDeleteBookFailure() throws Exception {

        doThrow(new RuntimeException("Book not found"))
                .when(bookService).deleteBook(20L);

        mockMvc.perform(post("/admin/books/20/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard?error=true"));

        verify(bookService).deleteBook(20L);
    }
}
