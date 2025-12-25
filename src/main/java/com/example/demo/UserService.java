package com.example.demo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

//new all
@Service
public class UserService {

    private final AppUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(String email, String rawPassword) {
        userRepo.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("E-Mail bereits vergeben");
        });

        String hash = passwordEncoder.encode(rawPassword);
        AppUser user = new AppUser(email, hash);
        return userRepo.save(user);
    }

    public AppUser login(String email, String rawPassword) {
        AppUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Falsches Passwort");
        }

        return user;
    }

    public void resetPassword(String email, String newPassword) {
        AppUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        String hash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hash);
        userRepo.save(user);
    }
}
