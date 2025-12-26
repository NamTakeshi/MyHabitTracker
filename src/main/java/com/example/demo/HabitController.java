package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/habits")
@RestController
@CrossOrigin(origins = {
        "https://myhabittracker-frontend.onrender.com", // Render frontend
        "http://localhost:5173/"                        // Local frontend
}) public class HabitController {

    private final HabitService service;

    public HabitController(HabitService service) { this.service = service;}

    //New appUser
    @GetMapping
    public Iterable<Habit> getHabits(@RequestParam Long userId) {return service.getAll(userId);}

    // 🔥 HEATMAP ENDPOINT
    @GetMapping("/{id}/completions")
    public List<HabitCompletion> getCompletions( @PathVariable Long id, @RequestParam Long userId, @RequestParam(defaultValue = "90") int daysBack) {
        return service.getCompletions(id, userId, daysBack);
    }

    //new App User
    @PostMapping // Nimmt Daten vom Frontend an
    public Habit createHabit(@RequestBody Habit h, @RequestParam Long userId) {return service.addHabit(h, userId);}

    @DeleteMapping("/{id}")
    public void deleteHabit(@PathVariable Long id, @RequestParam Long userId) {service.deleteHabit(id, userId);}

    // Habit bearbeiten
    @PutMapping("/{id}")
    public Habit updateHabit( @PathVariable Long id, @RequestBody Habit h, @RequestParam Long userId) {return service.updateHabit(id, h, userId);}

    // Als erledigt markieren
    @PostMapping("/{id}/check")
    public Habit checkHabit( @PathVariable Long id, @RequestParam Long userId) {return service.checkHabit(id, userId);}

    // Tägliches Reset
    @PostMapping("/reset-today")
    public void resetToday() { service.resetAllHabitsForNewDay();}

    //new App User
    // Habits filtern
    @GetMapping("/filter")
    public Iterable<Habit> filterHabits(@RequestParam Long userId, @RequestParam String status) {return service.filterByStatus(userId, status);}

    @PutMapping("/{id}/complete")
    public ResponseEntity<Habit> completeHabit( @PathVariable Long id, @RequestParam boolean completed, @RequestParam(required = false) String date, @RequestParam Long userId) {
        Habit habit = service.completeHabit(id, completed, date, userId);
        return ResponseEntity.ok(habit);
    }
}