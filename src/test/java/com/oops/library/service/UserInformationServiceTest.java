package com.oops.library.service;

import com.oops.library.enchanted.exception.EnchantedLibraryException;
import com.oops.library.entity.*;
import com.oops.library.repository.*;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserInformationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetService passwordResetService;
    @Mock private BorrowLogRepository borrowLogRepository;
    @Mock private BookRepository bookRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private UserInformationService userInformationService;

    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    // ----------------------------------------------------------------
    // getAllRegisteredUsers()
    // ----------------------------------------------------------------
    @Test
    void testGetAllRegisteredUsers() {
        User u1 = new Guest();
        u1.setName("John");
        u1.setEmail("j@x.com");

        User u2 = new Guest();
        u2.setName("Mary");
        u2.setEmail("m@x.com");

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<User> result = userInformationService.getAllRegisteredUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    // ----------------------------------------------------------------
    // findByEmail()
    // ----------------------------------------------------------------
    @Test
    void testFindByEmail_Success() throws Exception {
        User user = new Guest();
        user.setEmail("test@abc.com");

        when(userRepository.findByEmail("test@abc.com"))
                .thenReturn(Optional.of(user));

        User result = userInformationService.findByEmail("test@abc.com");
        assertEquals("test@abc.com", result.getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(userRepository.findByEmail("x@y.com"))
                .thenReturn(Optional.empty());

        assertThrows(EnchantedLibraryException.class,
                () -> userInformationService.findByEmail("x@y.com"));
    }

    // ----------------------------------------------------------------
    // findById()
    // ----------------------------------------------------------------
    @Test
    void testFindById_Success() throws Exception {
        User user = new Guest();
        user.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userInformationService.findById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindById_NotFound() {
        when(userRepository.findById(5L))
                .thenReturn(Optional.empty());

        assertThrows(EnchantedLibraryException.class,
                () -> userInformationService.findById(5L));
    }

    // ----------------------------------------------------------------
    // deleteUser()
    // ----------------------------------------------------------------
    @Test
    void testDeleteUser_Success() throws Exception {
        User user = new Guest();
        user.setId(10L);
        user.setProfileImagePath("profile/u1.png");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        userInformationService.deleteUser(10L);

        // 1. Password reset tokens deletion
        verify(entityManager).createNativeQuery(anyString());
        
        // 2. Borrow logs delete
        verify(borrowLogRepository).deleteByBorrower(user);

        // 3. Image delete
        verify(fileStorageService).deleteFile("profile/u1.png");

        // 4. User delete
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EnchantedLibraryException.class,
                () -> userInformationService.deleteUser(99L));
    }

    // ----------------------------------------------------------------
    // triggerPasswordResetForUser()
    // ----------------------------------------------------------------
    @Test
    void testTriggerPasswordResetForUser() throws Exception {
        User user = new Guest();
        user.setId(1L);
        user.setEmail("reset@abc.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userInformationService.triggerPasswordResetForUser(1L);

        verify(passwordResetService).requestPasswordReset("reset@abc.com");
    }

    // ----------------------------------------------------------------
    // getUserDetails()
    // ----------------------------------------------------------------
    @Test
    void testGetUserDetails() throws Exception {
        User user = new Guest();
        user.setId(2L);
        user.setName("Alice");
        user.setEmail("alice@x.com");
        user.setRole(Role.GUEST);
        

        Book book = new GeneralBook();
        book.setId(99L);
        book.setTitle("Sample Book");
        book.setAuthor("Author X");

        BorrowLog log1 = new BorrowLog();
        log1.setBook(book);
        log1.setReturnDate(LocalDateTime.now().plusDays(2)); // not overdue

        BorrowLog log2 = new BorrowLog();
        log2.setBook(book);
        log2.setReturnDate(LocalDateTime.now().minusDays(2)); // overdue

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(borrowLogRepository.findByBorrower(user))
                .thenReturn(List.of(log1, log2));

        when(borrowLogRepository.findByBorrowerAndReturnedFalse(user))
                .thenReturn(List.of(log1, log2));

        UserDetailsDTO dto = userInformationService.getUserDetails(2L);

        assertEquals(2, dto.getTotalBooksBorrowed());
        assertEquals(2, dto.getCurrentlyBorrowedCount());
        assertEquals(1, dto.getOverdueBooksCount()); // one overdue
    }

    // ----------------------------------------------------------------
    // getDashboardStatistics()
    // ----------------------------------------------------------------
    @Test
    void testGetDashboardStatistics() {
        User u1 = new Admin();
        u1.setRole(Role.ADMIN);
        

        User u2 = new Guest();
        u2.setRole(Role.GUEST);
        

        Book b1 = new GeneralBook();
        b1.setStatus(BookStatus.AVAILABLE);

        Book b2 = new GeneralBook();
        b2.setStatus(BookStatus.BORROWED);

        when(userRepository.count()).thenReturn(2L);
        when(bookRepository.count()).thenReturn(2L);
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));
        when(bookRepository.findAll()).thenReturn(List.of(b1, b2));
        when(borrowLogRepository.countByReturnedFalse()).thenReturn(5L);
        when(borrowLogRepository.countByReturnedFalseAndReturnDateBefore(any()))
                .thenReturn(1L);

        Map<String, Object> stats = userInformationService.getDashboardStatistics();

        assertEquals(2L, stats.get("totalUsers"));
        assertEquals(2L, stats.get("totalBooks"));
        assertEquals(5L, stats.get("activeBorrowings"));
        assertEquals(1L, stats.get("overdueBooks"));
    }
}
