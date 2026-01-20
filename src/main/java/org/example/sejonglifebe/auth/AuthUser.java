package org.example.sejonglifebe.auth;

import org.example.sejonglifebe.user.Role;

public record AuthUser(
        String studentId,
        Role role
) {
}
