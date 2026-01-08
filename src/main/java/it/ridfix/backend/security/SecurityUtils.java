package it.ridfix.backend.security;

import it.ridfix.backend.entities.enums.Role;
import it.ridfix.backend.exceptions.ApiExceptions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static SecurityUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser su)) {
            throw new ApiExceptions.Unauthorized("Not authenticated");
        }
        return su;
    }

    public static UUID currentUserId() {
        return currentUser().getId();
    }

    public static String currentUserEmail() {
        return currentUser().getUsername();
    }

    public static Role currentRole() {
        return currentUser().getRole();
    }

    public static boolean isStaffOrAdmin() {
        Role r = currentRole();
        return r == Role.STAFF || r == Role.ADMIN;
    }
}
