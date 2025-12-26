package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** Hilfsklasse für den Passwort-Reset */
    public static class ResetPasswordRequest {
        public String username;
        public String userCode;
        public String newPassword;
    }

    /**
     * Erstellt ein neues Benutzerkonto.
     * @param request Enthält Username und Passwort vom Frontend.
     * @return Die Daten des neuen Users (ID, Name, Code).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        AppUser user = userService.register(request.username, request.password);
        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getUserCode()
        ));
    }

    /**
     * Prüft die Anmeldedaten eines Benutzers.
     * @param request Benutzername und Passwort.
     * @return Bei Erfolg: Die User-Daten für das Frontend (zum Speichern im LocalStorage).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AppUser user = userService.login(request.username, request.password);
        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getUserCode()
        ));
    }

    /**
     * Ermöglicht das Ändern des Passworts, wenn der geheime User-Code bekannt ist.
     * @param request Enthält Username, User-Code und das neue Wunschpasswort.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.username, request.userCode, request.newPassword);
        return ResponseEntity.ok().build();
    }

    /**
     * Löscht das Konto eines Benutzers dauerhaft.
     * @param userId Die ID des Users, der gelöscht werden soll.
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Zeigt alle registrierten Benutzer an.
     * @return Eine Liste aller User-Objekte.
     */
    @GetMapping("/users")
    public List<AppUser> getAllUsers() {return userService.getAllUsers();}
}