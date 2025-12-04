package com.oops.library.strategy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.oops.library.entity.BorrowLog;

@Service
public class LateFeeService {
    public double calculateLateFee(BorrowLog borrowLog) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime returnDate = borrowLog.getReturnDate();

        if (returnDate != null && today.isAfter(returnDate)) {
            long overdueDays = ChronoUnit.DAYS.between(returnDate, today);
            double lateFeeRate = borrowLog.getBook().getLateFeeRate();
            return overdueDays * lateFeeRate;
        }
        return 0.0;
    }
}