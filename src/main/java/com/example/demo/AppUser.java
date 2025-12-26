package com.example.demo;

import jakarta.persistence.*;

/**
 * Die AppUser-Klasse repräsentiert einen Benutzer im System.
 * Sie speichert die Anmeldedaten und den eindeutigen User-Code.
 */
@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // statt email → username
    @Column(unique = true, nullable = false)
    private String username;

    // öffentliche 5-stellige User-ID als String
    @Column(unique = true, nullable = false, length = 5)
    private String userCode;

    @Column(nullable = false)
    private String passwordHash;

    public AppUser() {}

    /**
     * Konstruktor zum Erstellen eines neuen Benutzers.
     * @param username Der gewählte Name.
     * @param userCode Der generierte 5-stellige Sicherheitscode.
     * @param passwordHash Das bereits verschlüsselte Passwort.
     */
    public AppUser(String username, String userCode, String passwordHash) {
        this.username = username;
        this.userCode = userCode;
        this.passwordHash = passwordHash;
    }

    // Getter und Setter
    public Long getId() {return id;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getUserCode() {return userCode;}
    public void setUserCode(String userCode) {this.userCode = userCode;}

    public String getPasswordHash() {return passwordHash;}
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}
}
