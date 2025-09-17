package com.library.management.patterns;

import com.library.management.exception.EnchantedLibraryException;
import java.util.Collections;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FacadeDashboard {

	public Map<String, ?> getBooksAndUsers() throws EnchantedLibraryException {
		return Collections.emptyMap();
	}

}
