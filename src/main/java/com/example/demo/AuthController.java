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

    // kleine DTOs
    public static class AuthRequest {
        public String email;
        public String password;
    }

    public static class AuthResponse {
        public Long userId;
        public String email;

        public AuthResponse(Long userId, String email) {
            this.userId = userId;
            this.email = email;
        }
    }

    public static class ResetPasswordRequest {
        public String email;
        public String newPassword;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        AppUser user = userService.register(request.email, request.password);
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AppUser user = userService.login(request.email, request.password);
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.email, request.newPassword);
        return ResponseEntity.ok().build();
    }
}
