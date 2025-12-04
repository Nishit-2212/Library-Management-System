package com.oops.library.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.*;

import com.oops.library.entity.*;
import com.oops.library.strategy.LateFeeService;
import com.oops.library.repository.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class OverdueNotificationSchedulerTest {

    @Mock
    private BorrowLogRepository borrowLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private LateFeeService lateFeeService;

    @InjectMocks
    private OverdueNotificationScheduler scheduler;

   
    @Test
    void testSendOverdueReminders_NoOverdueBooks() {

        when(borrowLogRepository.findByReturnedFalseAndReturnDateBefore(any()))
                .thenReturn(Collections.emptyList());

        scheduler.sendOverdueReminders();

        verify(emailService, never()).sendTemplatedMessage(anyString(), anyString(), anyString(), anyMap());
        verify(emailService, never()).sendOverdueSummary(anyList(), anyString());
    }
}
