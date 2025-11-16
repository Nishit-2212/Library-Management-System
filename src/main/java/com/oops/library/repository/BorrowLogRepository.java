package com.oops.library.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.oops.library.entity.*;

@Repository
public interface BorrowLogRepository extends JpaRepository<BorrowLog,Long>{
	
	List<BorrowLog> findByReturnedFalseAndReturnDateBefore(LocalDate today);
	
	List<BorrowLog> findByBorrowerEmailAndReturnedFalse(String email);
	
	@Modifying
	@Transactional
	void deleteByBook(Book book);

}
