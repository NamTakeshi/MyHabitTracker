package com.example.demo;

import org.springframework.stereotype.Service;

// Frontend → Controller → Service → Repository → Datenbank
// Der Service ist das Gehirn zwischen Controller und Repository.

@Service
public class HabitService {

    private final HabitRepository repo;

    public HabitService(HabitRepository repo) {
        this.repo = repo;
    }

    public Iterable<Habit> getAll() {
        return repo.findAll();
    }

    public Habit addHabit(Habit h) {
        return repo.save(h);
    }

    public void deleteHabit(Long id) {
        repo.deleteById(id);
    }

    public Habit updateHabit(Long id, Habit updated) {
        Habit existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found: " + id));
        existing.setName(updated.getName());
        // weitere Felder bei Bedarf
        return repo.save(existing);
    }

    public Habit checkHabit(Long id) {
        Habit habit = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found: " + id));

        habit.setCompleted(true);
        habit.setLastCompletedDate(java.time.LocalDate.now());
        // Streak-Logik kannst du später ergänzen
        return repo.save(habit);
    }

    // Optional: simple Filter-Logik (nur nach completed)
    public Iterable<Habit> filterByStatus(String status) {
        if ("completed".equalsIgnoreCase(status)) {
            return repo.findByCompletedTrue();
        } else if ("active".equalsIgnoreCase(status)) {
            return repo.findByCompletedFalse();
        }
        return repo.findAll();
    }

}

