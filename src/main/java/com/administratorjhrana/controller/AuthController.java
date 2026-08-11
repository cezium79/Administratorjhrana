package com.administratorjhrana.controller;

import com.administratorjhrana.dto.LoginRequest;
import com.administratorjhrana.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "username", userDetails.getUsername()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        if (username != null && jwtService.isTokenValid(jwt)) {
            String newToken = jwtService.generateToken(username);
            return ResponseEntity.ok(Map.of("token", newToken));
        }

        return ResponseEntity.status(401).build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verify(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        boolean valid = jwtService.isTokenValid(jwt);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
