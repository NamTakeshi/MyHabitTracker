package com.example.demo;

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
@CrossOrigin(origins = "https://myhabittracker-frontend.onrender.com")
public class HabitController {

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



}

