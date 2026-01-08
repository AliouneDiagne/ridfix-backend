package it.ridfix.backend.services;

import it.ridfix.backend.dto.AuthDTOs;
import it.ridfix.backend.dto.UserDTOs;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.entities.enums.Role;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.mappers.MapperUtils;
import it.ridfix.backend.repositories.UserRepository;
import it.ridfix.backend.security.JwtService;
import it.ridfix.backend.security.SecurityUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw new ApiExceptions.Conflict("Email already registered");
        }
        User u = new User(
                req.email().toLowerCase(),
                encoder.encode(req.password()),
                req.name(),
                req.surname(),
                Role.CUSTOMER
        );
        users.save(u);

        String token = jwtService.generateToken(u.getEmail(), Map.of("role", u.getRole().name(), "uid", u.getId().toString()));
        return new AuthDTOs.AuthResponse(token, MapperUtils.userToResponse(u));
    }

    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (AuthenticationException ex) {
            throw new ApiExceptions.Unauthorized("Invalid credentials");
        }

        User u = users.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiExceptions.Unauthorized("Invalid credentials"));

        String token = jwtService.generateToken(u.getEmail(), Map.of("role", u.getRole().name(), "uid", u.getId().toString()));
        return new AuthDTOs.AuthResponse(token, MapperUtils.userToResponse(u));
    }

    public UserDTOs.UserResponse me() {
        User u = users.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiExceptions.NotFound("User not found"));
        return MapperUtils.userToResponse(u);
    }
}
