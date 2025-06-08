package com.hoz.back.controller;

import com.hoz.back.dto.AuthResponse;
import com.hoz.back.exception.UserNotFoundException;
import com.hoz.back.model.Users;
import com.hoz.back.repository.UserRepository;
import com.hoz.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody Users newUser) {
        newUser.setRole("ROLE_USER");
        return userService.registerUser(newUser);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody Users user) {
        return userService.verify(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return ResponseEntity.ok(userService.logout(token));
        } else {
            return ResponseEntity.badRequest().body("Authorization header is missing or invalid");
        }
    }


    @GetMapping("/users")
    public List<Users> getAllUsers() {
        return userRepo.findAll();
    }


    @GetMapping("/user/{id}")
    public Users getUserById(@PathVariable Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }


    @PutMapping("/user/{id}")
    public Users updateUser(@RequestBody Users newUser, @PathVariable Long id) {
        return userRepo.findById(id)
                .map(user -> {
                    user.setUsername(newUser.getUsername());
                    user.setPassword(newUser.getPassword());
                    user.setEmail(newUser.getEmail());
                    return userRepo.save(user);
                }).orElseThrow(() -> new UserNotFoundException(id));
    }


    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        if (!userRepo.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepo.deleteById(id);
        return "User with id " + id + " has been deleted successfully.";
    }
}
