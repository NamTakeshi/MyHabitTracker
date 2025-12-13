package com.example.demo;

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
})public class HabitController {

    private final HabitService service;

    public HabitController(HabitService service) {
        this.service = service;
    }



    @GetMapping
    public Iterable<Habit> getHabits() {
        return service.getAll(); // <- jetzt aus DB
    }

    @PostMapping // Nimmt Daten vom Frontend an
    public Habit createHabit(@RequestBody Habit h) {
        return service.addHabit(h);
    }

    @DeleteMapping("/{id}")
    public void deleteHabit(@PathVariable Long id) {
        service.deleteHabit(id);
    }

    // Habit bearbeiten
    @PutMapping("/{id}")
    public Habit updateHabit(@PathVariable Long id, @RequestBody Habit h) {
        return service.updateHabit(id, h);
    }

    // Als erledigt markieren
    @PostMapping("/{id}/check")
    public Habit checkHabit(@PathVariable Long id) {
        return service.checkHabit(id);
    }

    // Tägliches Reset
    @PostMapping("/reset-today")
    public void resetToday() {
        service.resetAllHabitsForNewDay();
    }


    // Habits filtern
    @GetMapping("/filter")
    public Iterable<Habit> filterHabits(@RequestParam String status) {
        return service.filterByStatus(status); // z.B. "all", "active", "completed"
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Habit> completeHabit(@PathVariable Long id, @RequestParam boolean completed) {
        Habit habit = service.completeHabit(id, completed);
        return ResponseEntity.ok(habit);
    }








}

