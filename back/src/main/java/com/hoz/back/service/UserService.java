package com.hoz.back.service;

import com.hoz.back.dto.AuthResponse;
import com.hoz.back.model.Users;
import com.hoz.back.model.UserPrinciple;
import com.hoz.back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = repo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        System.out.println("Loading user: " + username + " with role: " + user.getRole());
        return new UserPrinciple(user);
    }

    public AuthResponse registerUser(Users user) {
        try {
            // Check if username already exists
            if (repo.findByUsername(user.getUsername()) != null) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check if email already exists
            if (repo.findByEmail(user.getEmail()) != null) {
                throw new RuntimeException("Email already exists");
            }

            // Set default role if not provided
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                user.setRole("USER");
            }

            user.setPassword(encoder.encode(user.getPassword()));
            Users savedUser = repo.save(user);
            String token = jwtService.generateToken(savedUser);
            System.out.println("Generated token for new user: " + token);
            return new AuthResponse(token, savedUser.getId(), savedUser.getRole());
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse verify(Users user) {
        try {
            System.out.println("Attempting to verify user: " + user.getUsername());
            
            // First check if user exists
            Users foundUser = repo.findByUsername(user.getUsername());
            if (foundUser == null) {
                throw new RuntimeException("User not found");
            }

            // Attempt authentication
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            System.out.println("Authentication result: " + auth.isAuthenticated());
            System.out.println("User authorities: " + auth.getAuthorities());

            if (auth.isAuthenticated()) {
                String token = jwtService.generateToken(foundUser);
                System.out.println("Generated token: " + token);
                return new AuthResponse(token, foundUser.getId(), foundUser.getRole());
            }

            throw new RuntimeException("Invalid username/password");
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    @Autowired
    private JWTBlacklistService blacklistService;

    public String logout(String token) {
        if (token != null && !token.isBlank()) {
            blacklistService.blacklistToken(token);
            return "User logged out successfully.";
        }
        return "Invalid token provided.";
    }
}
