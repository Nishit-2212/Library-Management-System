package com.oops.library.strategy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component("restricted")
public class RestrictedReadingRoomStrategy implements LendingStrategy {

	@Override
	public LocalDateTime calculateReturnDate(LocalDateTime borrowDate) {
		return borrowDate.plusMinutes(30);
	}

}
