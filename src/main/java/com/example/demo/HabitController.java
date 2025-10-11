package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HabitController {

    // GET-Route für /habits → gibt alle Habits als JSON-Liste (JavaScript Object Notation Format) zurück
    @GetMapping("/habits")
    public List<Habit> getHabits() {
        return List.of(
                new Habit(1L, "Joggen"),
                new Habit(2L, "8 Stunden Schlaf")
        );
    }



}

