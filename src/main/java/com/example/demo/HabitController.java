package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/*
Controller:

1. Liefert Daten an das Frontend
GET /habits → gibt alle Habits zurück

2. Nimmt Daten vom Frontend an
POST /habits → speichert neuen Habit mit repo.save()

3. Gibt Antworten im JSON-Format zurück
Damit dein Frontend sie anzeigen kann.
*/

/**
 * Der Controller ist die Schnittstelle nach außen.
 * Er empfängt HTTP-Anfragen vom Frontend und leitet sie an den HabitService weiter.
 */
@RequestMapping("/habits")
@RestController
@CrossOrigin(origins = {
        "https://myhabittracker-frontend.onrender.com", // Render frontend
        "http://localhost:5173/"                        // Local frontend
}) public class HabitController {

    private final HabitService service;

    public HabitController(HabitService service) { this.service = service;}

    /**
     * Holt alle Gewohnheiten für einen bestimmten User.
     * @param userId Die ID des angemeldeten Benutzers (kommt aus der URL: ?userId=...)
     */
    @GetMapping
    public Iterable<Habit> getHabits(@RequestParam Long userId) {return service.getAll(userId);}

    /**
     * Liefert die Daten für die Heatmap (Kalender-Übersicht).
     * @param id Die ID des spezifischen Habits (aus dem Pfad /{id}).
     * @param userId Wer fragt an?
     * @param daysBack Wie viele Tage rückwärts (Standard: 90).
     */
    @GetMapping("/{id}/completions")
    public List<HabitCompletion> getCompletions( @PathVariable Long id, @RequestParam Long userId, @RequestParam(defaultValue = "90") int daysBack) {
        return service.getCompletions(id, userId, daysBack);
    }

    /**
     * Erstellt ein neues Habit.
     * @param h Das Habit-Objekt (kommt als JSON im RequestBody).
     * @param userId Die ID des Users, dem das Habit gehören soll.
     */
    @PostMapping // Nimmt Daten vom Frontend an
    public Habit createHabit(@RequestBody Habit h, @RequestParam Long userId) {return service.addHabit(h, userId);}

    /**
     * Löscht ein Habit anhand seiner ID.
     */
    @DeleteMapping("/{id}")
    public void deleteHabit(@PathVariable Long id, @RequestParam Long userId) {service.deleteHabit(id, userId);}

    /**
     * Aktualisiert ein bestehendes Habit (z.B. Name, Farbe oder Status geändert).
     * @param id Welches Habit soll geändert werden?
     * @param h Die neuen Daten.
     */
    @PutMapping("/{id}")
    public Habit updateHabit( @PathVariable Long id, @RequestBody Habit h, @RequestParam Long userId) {return service.updateHabit(id, h, userId);}

    /**
     * Schnelles Markieren als "erledigt" für den heutigen Tag.
     */
    @PostMapping("/{id}/check")
    public Habit checkHabit( @PathVariable Long id, @RequestParam Long userId) {return service.checkHabit(id, userId);}

    /**
     * Setzt alle Habits manuell auf "nicht erledigt" zurück.
     */
    @PostMapping("/reset-today")
    public void resetToday() { service.resetAllHabitsForNewDay();}

    /**
     * Filtert die Habits (z.B. nur die aktiven oder nur die erledigten).
     * @param status Der Filter-Status ("active", "completed").
     */
    @GetMapping("/filter")
    public Iterable<Habit> filterHabits(@RequestParam Long userId, @RequestParam String status) {return service.filterByStatus(userId, status);}

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeHabit(
            @PathVariable Long id,
            @RequestParam boolean completed,
            @RequestParam(required = false) String date,
            @RequestParam Long userId
    ) {
        // Datum umwandeln oder heutiges Datum nehmen
        LocalDate anfrageDatum = (date == null || date.isEmpty())
                ? LocalDate.now()
                : LocalDate.parse(date);

        // SICHERHEIT: Nur das heutige Datum ist für Änderungen erlaubt
        if (!anfrageDatum.equals(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("Fehler: Du kannst Gewohnheiten nur für den aktuellen Tag als erledigt markieren.");
        }

        Habit habit = service.completeHabit(id, completed, date, userId);
        return ResponseEntity.ok(habit);
    }
}