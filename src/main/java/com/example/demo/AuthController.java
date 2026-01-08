package com.example.demo;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für Authentifizierung und Benutzerverwaltung.
 *
 * <p>Stellt Endpunkte für Registrierung, Login, Passwort-Reset
 * und das Löschen von Benutzerkonten bereit.</p>
 *
 * <p>Der User-Code wird beim Registrieren einmalig im Klartext
 * zurückgegeben und anschließend nur gehasht gespeichert.</p>
 */

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

    /**
     * Request-Daten für Registrierung und Login.
     */
    public static class AuthRequest {
        public String username;
        public String password;
    }

    /**
     * Response-Daten für einen erfolgreichen Login.
     */
    public static class LoginResponse {
        public Long userId;
        public String username;

        public LoginResponse(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    /**
     * Response-Daten für eine erfolgreiche Registrierung.
     *
     * <p>Der User-Code wird nur einmal im Klartext zurückgegeben.</p>
     */
    public static class RegisterResponse {
        public Long userId;
        public String username;
        public String userCode; // plaintext, ONCE

        public RegisterResponse(Long userId, String username, String userCode) {
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
     * Registriert einen neuen Benutzer.
     *
     * @param request Username und Passwort
     * @return User-ID, Username und einmaliger User-Code
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody AuthRequest request) {
        UserService.RegisterResult result =
                userService.register(request.username, request.password);

        return ResponseEntity.ok(
                new RegisterResponse(result.user.getId(), result.user.getUsername(), result.userCodePlain
                )
        );
    }

    /**
     * Authentifiziert einen Benutzer.
     *
     * @param request Username und Passwort
     * @return Basisdaten des eingeloggten Benutzers
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest request) {
        AppUser user = userService.login(request.username, request.password);

        return ResponseEntity.ok(
                new LoginResponse(
                        user.getId(),
                        user.getUsername()
                )
        );
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
     * Gibt alle Benutzer zurück.
     *
     * <p>Nur im Entwicklungsprofil verfügbar.</p>
     */
    @Profile("dev")
    @GetMapping("/users")
    public List<AppUser> getAllUsers() {return userService.getAllUsers();}
}