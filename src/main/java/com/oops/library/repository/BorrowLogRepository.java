package com.oops.library.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.oops.library.entity.*;

@Repository
public interface BorrowLogRepository extends JpaRepository<BorrowLog, Long> {
    
    // Existing methods - keep these
    List<BorrowLog> findByReturnedFalseAndReturnDateBefore(LocalDateTime localDateTime);
    List<BorrowLog> findByBorrowerEmailAndReturnedFalse(String email);
    
    @Modifying
    @Transactional
    void deleteByBook(Book book);
    
    // === CORRECTED METHODS ===
    // Use "borrower" not "user" (matching entity field name)
    
    // 1. Find all borrow logs for a borrower
    List<BorrowLog> findByBorrower(User borrower);
    
    // 2. Find current (not returned) borrowings for a borrower
    List<BorrowLog> findByBorrowerAndReturnedFalse(User borrower);
    
    // 3. Count total borrowings for a borrower
    long countByBorrower(User borrower);
    
    // 4. Count current borrowings for a borrower
    long countByBorrowerAndReturnedFalse(User borrower);
    
    // 5. Find overdue borrowings for a borrower
    @Query("SELECT bl FROM BorrowLog bl WHERE bl.borrower = :borrower AND bl.returned = false AND bl.returnDate < CURRENT_TIMESTAMP")
    List<BorrowLog> findOverdueByBorrower(@Param("borrower") User borrower);
    
    // 6. Delete all borrow logs for a borrower
    @Modifying
    @Transactional
    @Query("DELETE FROM BorrowLog bl WHERE bl.borrower = :borrower")
    void deleteByBorrower(@Param("borrower") User borrower);
    
    // 7. Other existing methods
    @Query("SELECT bl FROM BorrowLog bl WHERE bl.borrower = :borrower AND bl.returned = true ORDER BY bl.returnDate DESC")
    List<BorrowLog> findRecentlyReturnedByBorrower(@Param("borrower") User borrower, Pageable pageable);

    long countByReturnedFalse();
    long countByReturnedFalseAndReturnDateBefore(LocalDateTime date);
	
}