package com.library.management.service;

import com.library.management.dto.RegistrationDto;
import com.library.management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceNegativeTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        RegistrationDto dto = new RegistrationDto();
        dto.setName("Existing");
        dto.setEmail("existing@example.com");
        dto.setPassword("validPassword");
        dto.setRole("GUEST");

        // simulate that email already exists
    when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new com.library.management.entity.Guest()));

        // Expected behavior: registration should fail when email already exists.
        // Current implementation does not check duplicates, so this assertion will fail until code is refactored.
        assertThrows(IllegalStateException.class, () -> registrationService.registerUser(dto));
    }

    @Test
    void shouldThrowWhenPasswordTooShort() {
        RegistrationDto dto = new RegistrationDto();
        dto.setName("ShortPwd");
        dto.setEmail("shortpwd@example.com");
        dto.setPassword("123"); // intentionally too short
        dto.setRole("GUEST");

        // Expected: service validates password length and throws IllegalArgumentException
        // Current implementation does not validate length, so this will fail until validation is added.
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerUser(dto));
    }
}
