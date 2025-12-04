package com.oops.library.strategy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component("public")
public class PublicLendingStrategy implements LendingStrategy{

	@Override
	public LocalDateTime calculateReturnDate(LocalDateTime borrowDate) {
	    return borrowDate.plusDays(2);	
	    
	}

}
