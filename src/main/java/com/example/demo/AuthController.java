package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {
        "https://myhabittracker-frontend.onrender.com",
        "http://localhost:5173"
})
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // DTOs
    public static class AuthRequest {
        public String username;
        public String password;
    }

    public static class AuthResponse {
        public Long userId;
        public String username;
        public String userCode;

        public AuthResponse(Long userId, String username, String userCode) {
            this.userId = userId;
            this.username = username;
            this.userCode = userCode;
        }
    }

    public static class ResetPasswordRequest {
        public String username;
        public String userCode;
        public String newPassword;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        AppUser user = userService.register(request.username, request.password);
        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getUserCode()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AppUser user = userService.login(request.username, request.password);
        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getUserCode()
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.username, request.userCode, request.newPassword);
        return ResponseEntity.ok().build();
    }

    // Konto löschen
    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}