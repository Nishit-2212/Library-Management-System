package com.oops.library.strategy;

import java.time.LocalDateTime;

public interface LendingStrategy {

	LocalDateTime calculateReturnDate(LocalDateTime borrowDate);
}
