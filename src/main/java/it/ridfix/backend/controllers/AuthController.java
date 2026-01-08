package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.AuthDTOs;
import it.ridfix.backend.dto.UserDTOs;
import it.ridfix.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public AuthDTOs.AuthResponse register(@Valid @RequestBody AuthDTOs.RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthDTOs.AuthResponse login(@Valid @RequestBody AuthDTOs.LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/me")
    public UserDTOs.UserResponse me() {
        return auth.me();
    }
}
