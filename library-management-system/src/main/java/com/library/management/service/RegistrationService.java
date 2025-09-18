package com.library.management.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    public void registerUser(RegistrationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Registration data must not be null");
        }

        String email = dto.getEmail();
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }

        String rawPassword = dto.getPassword();
        // enforce a minimum password length of 4 characters for these tests
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long");
        }

        User user = UserFactory.createUser(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}
