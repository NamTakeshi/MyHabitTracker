package com.example.demo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service-Klasse zur Verwaltung von Benutzern.
 * Enthält die Logik für Registrierung, Login, Passwort-Reset und Löschen.
 */
@Service
public class UserService {

    private final AppUserRepository userRepo;
    private final HabitRepository habitRepo;
    private final PasswordEncoder passwordEncoder;

    // Konstruktor: Initialisiert die Repositories und den Password-Encoder
    public UserService(AppUserRepository userRepo, HabitRepository habitRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.habitRepo = habitRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registriert einen neuen Benutzer.
     * @param username Der gewünschte Name des Nutzers.
     * @param rawPassword Das Passwort im Klartext (wird vor Speicherung verschlüsselt).
     */
    public AppUser register(String username, String rawPassword) {
        userRepo.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Benutzername bereits vergeben");
        });
        String userCode = generateUniqueUserCode();
        String hash = passwordEncoder.encode(rawPassword);

        AppUser user = new AppUser(username, userCode, hash);
        return userRepo.save(user);
    }

    /**
     * Authentifiziert einen Benutzer beim Login.
     * @param username Der eingegebene Benutzername.
     * @param rawPassword Das eingegebene Passwort zum Abgleich.
     */
    public AppUser login(String username, String rawPassword) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Falsches Passwort");
        }
        return user;
    }

    /**
     * Setzt das Passwort eines Benutzers zurück.
     * @param username Name des Kontos.
     * @param userCode Die geheime 5-stellige ID zur Verifizierung.
     * @param newPassword Das neue Passwort, das gesetzt werden soll.
     */
    public void resetPassword(String username, String userCode, String newPassword) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        if (!user.getUserCode().equals(userCode)) {
            throw new IllegalArgumentException("Username und User-ID passen nicht zusammen");
        }

        String hash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hash);
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
     * Interne Hilfsmethode: Generiert eine eindeutige 5-stellige Nummer (z.B. "08152").
     * @return Ein String mit genau 5 Ziffern, z.B. "02739".
     */
    private String generateUniqueUserCode() {
        String code;
        do {
            int num = ThreadLocalRandom.current().nextInt(0, 100000);
            code = String.format("%05d", num);
        } while (userRepo.findByUserCode(code).isPresent());
        return code;
    }

    /**
     * Abfrage-Methode: Listet alle vorhandenen Benutzer auf.
     * @return Eine Liste mit allen AppUser-Objekten aus der Datenbank.
     */
    public List<AppUser> getAllUsers() {
        return userRepo.findAll();
    }
}
