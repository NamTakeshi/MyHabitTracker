package com.example.demo;

import jakarta.persistence.*;

/**
 * Entity für App-Benutzer.
 *
 * <p>Speichert Anmeldeinformationen und sicherheitsrelevante Daten.
 *  * Sensible Werte wie Passwort und User-Code werden ausschließlich
 *  * in gehashter Form persistiert.</p>
 */

@Entity
@Table(name = "users")
public class AppUser {

    /**
     * Technischer Primärschlüssel.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Eindeutiger Benutzername.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Gehashter Sicherheitscode für Passwort-Resets.
     *
     * <p>Der Klartext-Code wird niemals gespeichert oder zurückgegeben.</p>
     */
    @Column(name = "user_code", nullable = false, unique = true, length = 100)
    private String userCode;

    /**
     * Gehashter Passwortwert.
     */
    @Column(nullable = false)
    private String password;

    public AppUser() {}

    /**
     * Konstruktor zum Erstellen eines neuen Benutzers.
     * @param username Der gewählte Name.
     * @param userCode Der einmalig generierte Sicherheitscode (gehasht gespeichert).
     * @param password Das bereits verschlüsselte Passwort.
     */
    public AppUser(String username, String userCode, String password) {
        this.username = username;
        this.userCode = userCode;
        this.password = password;
    }

    // Getter und Setter
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getUserCode() {return userCode;}
    public void setUserCode(String userCode) {this.userCode = userCode;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
}
