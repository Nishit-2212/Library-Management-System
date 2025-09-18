package com.library.management.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.library.management.dto.RegistrationDto;
import com.library.management.patterns.UserFactory;
import com.library.management.repository.UserRepository;
import com.library.management.entity.User;

@Service
public class RegistrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final int MIN_PASSWORD_LENGTH = 4;

    @Transactional
    public void registerUser(RegistrationDto dto) {
        validate(dto);

        // normalize email
        String normalizedEmail = dto.getEmail() == null ? null : dto.getEmail().trim().toLowerCase();
        if (normalizedEmail != null) {
            dto.setEmail(normalizedEmail);
        }

        User user = UserFactory.createUser(dto);

        // encode the raw password from DTO explicitly (clearer intent)
        String rawPassword = dto.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));

        userRepository.save(user);
    }

    private void validate(RegistrationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Registration data must not be null");
        }

        String email = dto.getEmail();
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }

        String rawPassword = dto.getPassword();
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
    }
}
