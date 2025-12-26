package com.example.demo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserService {

    private final AppUserRepository userRepo;
    private final HabitRepository habitRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepo, HabitRepository habitRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.habitRepo = habitRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(String username, String rawPassword) {
        userRepo.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Benutzername bereits vergeben");
        });

        String userCode = generateUniqueUserCode();
        String hash = passwordEncoder.encode(rawPassword);

        AppUser user = new AppUser(username, userCode, hash);
        return userRepo.save(user);
    }

    public AppUser login(String username, String rawPassword) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Falsches Passwort");
        }

        return user;
    }

    // Passwort-Reset: username + userCode + neues Passwort
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

    // Konto löschen + alle Habits des Users
    public void deleteUser(Long userId) {
        // zuerst Habits löschen
        habitRepo.deleteByUserId(userId);
        // dann User löschen
        userRepo.deleteById(userId);
    }

    // 5-stellige numerische ID, z.B. "02739"
    private String generateUniqueUserCode() {
        String code;
        do {
            int num = ThreadLocalRandom.current().nextInt(0, 100000);
            code = String.format("%05d", num);
        } while (userRepo.findByUserCode(code).isPresent());
        return code;
    }
}
