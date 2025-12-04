package com.oops.library.controller;

import com.oops.library.entity.BorrowLog;
import com.oops.library.entity.Book;
import com.oops.library.service.BorrowLogService;
import com.oops.library.strategy.LateFeeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BorrowLogControllerTest {

    private BorrowLogService borrowLogService;
    private LateFeeService lateFeeService;
    private BorrowLogController controller;
    private Model model;

    @BeforeEach
    void setUp() {
        borrowLogService = mock(BorrowLogService.class);
        lateFeeService = mock(LateFeeService.class);
        model = mock(Model.class);

        controller = new BorrowLogController(borrowLogService, lateFeeService);
    }

    @Test
    void testViewBorrowLogs_success() {
        // Arrange
        BorrowLog log1 = mock(BorrowLog.class);
        BorrowLog log2 = mock(BorrowLog.class);

        Book book = mock(Book.class);

        when(log1.getBook()).thenReturn(book);
        when(log2.getBook()).thenReturn(null); // test null safety

        List<BorrowLog> logs = Arrays.asList(log1, log2);

        when(borrowLogService.findAll()).thenReturn(logs);
        when(lateFeeService.calculateLateFee(log1)).thenReturn(50.0);

        // Act
        String viewName = controller.viewBorrowLogs(model);

        // Assert view
        assertEquals("borrowlogs", viewName);

        // Verify Model attribute for logs
        verify(model).addAttribute("borrowLogs", logs);

        // Capture "lateFees" map
        verify(model).addAttribute(eq("lateFees"), any(Map.class));

        // Validate interactions
        verify(lateFeeService).calculateLateFee(log1); // for valid book
        verifyNoMoreInteractions(lateFeeService); // should NOT calculate for log2
    }

    @Test
    void testViewBorrowLogs_emptyList() {
        // Arrange
        when(borrowLogService.findAll()).thenReturn(Collections.emptyList());

        // Act
        String view = controller.viewBorrowLogs(model);

        // Assert
        assertEquals("borrowlogs", view);
        verify(model).addAttribute("borrowLogs", Collections.emptyList());
        verify(model).addAttribute(eq("lateFees"), any(Map.class));
        verifyNoInteractions(lateFeeService);
    }
}
