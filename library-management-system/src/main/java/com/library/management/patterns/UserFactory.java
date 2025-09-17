package com.library.management.patterns;

import com.library.management.dto.RegistrationDto;
import com.library.management.entity.User;
import com.library.management.entity.Librarian;
import com.library.management.entity.Scholar;
import com.library.management.entity.Guest;
import com.library.management.entity.Role;

public class UserFactory {

    public static User createUser(RegistrationDto dto) {
        String role = dto.getRole().toUpperCase();

        return switch (role) {
            case "LIBRARIAN" -> {
                Librarian librarian = new Librarian();
                yield populateUserFields(librarian, dto, Role.LIBRARIAN);
            }
            case "SCHOLAR" -> {
                Scholar scholar = new Scholar();
                yield populateUserFields(scholar, dto, Role.SCHOLAR);
            }
            case "GUEST" -> {
                Guest guest = new Guest();
                yield populateUserFields(guest, dto, Role.GUEST);
            }
            default -> throw new IllegalArgumentException("Invalid role selected");
        };
    }

    private static User populateUserFields(User user, RegistrationDto dto, Role role) {
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // password will be encoded in service
        user.setRole(role);
        return user;
    }
}
