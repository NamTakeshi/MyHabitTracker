package com.example.demo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.security.SecureRandom;


/**
 * Service für Benutzerverwaltung.
 *
 * <p>Enthält die Geschäftslogik für Registrierung, Login,
 * Passwort-Reset und das Löschen von Benutzerkonten.</p>
 *
 * <p>Sensible Werte (Passwort, User-Code) werden ausschließlich
 * in gehashter Form gespeichert.</p>
 */

@Service
public class UserService {

    private final AppUserRepository userRepo;
    private final HabitRepository habitRepo;
    private final PasswordEncoder passwordEncoder;

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int USER_CODE_LENGTH = 12;
    private final SecureRandom secureRandom = new SecureRandom();

    // Konstruktor: Initialisiert die Repositories und den Password-Encoder
    public UserService(AppUserRepository userRepo, HabitRepository habitRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.habitRepo = habitRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Technisches Rückgabeobjekt für die Registrierung.
     *
     * <p>Enthält den gespeicherten Benutzer sowie den einmalig
     * ausgegebenen Klartext-User-Code.</p>
     */
    public static class RegisterResult {
        public final AppUser user;
        public final String userCodePlain;

        public RegisterResult(AppUser user, String userCodePlain) {
            this.user = user;
            this.userCodePlain = userCodePlain;
        }
    }

    /**
     * Registriert einen neuen Benutzer.
     *
     * @param username gewünschter Benutzername
     * @param rawPassword Passwort im Klartext
     * @return Benutzer + einmaliger User-Code
     */
    public RegisterResult register(String username, String rawPassword) {
        userRepo.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Benutzername bereits vergeben");
        });

        String userCodePlain = generateUserCodePlain();
        String userCodeHash = passwordEncoder.encode(userCodePlain);
        String passwordHash = passwordEncoder.encode(rawPassword);

        AppUser user = new AppUser(username, userCodeHash, passwordHash);
        AppUser saved = userRepo.save(user);

        return new RegisterResult(saved, userCodePlain);
    }

    /**
     * Authentifiziert einen Benutzer beim Login.
     * @param username Der eingegebene Benutzername.
     * @param rawPassword Das eingegebene Passwort zum Abgleich.
     */
    public AppUser login(String username, String rawPassword) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Falsches Passwort");
        }
        return user;
    }

    /**
     * Setzt ein neues Passwort anhand des User-Codes.
     *
     * @param username Benutzername
     * @param userCode Klartext-User-Code
     * @param newPassword neues Passwort im Klartext
     */
    public void resetPassword(String username, String userCode, String newPassword) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        // compare pakai hash
        if (!passwordEncoder.matches(userCode, user.getUserCode())) {
            throw new IllegalArgumentException("Username und User-ID passen nicht zusammen");
        }

        String hash = passwordEncoder.encode(newPassword);
        user.setPassword(hash);
        userRepo.save(user);
    }

    /**
     * Löscht einen Benutzer und alle zugehörigen Daten.
     * @param userId Die technische Datenbank-ID des Benutzers.
     */
    public void deleteUser(Long userId) {
        // zuerst Habits löschen
        habitRepo.deleteByUserId(userId);
        // dann User löschen
        userRepo.deleteById(userId);
    }

    /**
     * Generiert einen zufälligen Klartext-User-Code.
     */
    private String generateUserCodePlain() {
        StringBuilder sb = new StringBuilder(USER_CODE_LENGTH);
        for (int i = 0; i < USER_CODE_LENGTH; i++) {
            int idx = secureRandom.nextInt(CODE_ALPHABET.length());
            sb.append(CODE_ALPHABET.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * Abfrage-Methode: Listet alle vorhandenen Benutzer auf.
     * @return Eine Liste mit allen AppUser-Objekten aus der Datenbank.
     */
    public List<AppUser> getAllUsers() {
        return userRepo.findAll();
    }
}
