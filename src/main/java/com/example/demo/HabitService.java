package com.example.demo;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

        LocalDate today = LocalDate.now();

        if (updated.isCompleted()) {
            // Erledigt → Streak erhöhen
            if (existing.getLastCompletedDate() == null ||
                    !existing.getLastCompletedDate().equals(today)) {
                existing.setStreakCount(existing.getStreakCount() + 1);
            }
            existing.setLastCompletedDate(today);
        } else {
            // Rückgängig → Streak für heute zurücksetzen
            if (existing.getLastCompletedDate() != null &&
                    existing.getLastCompletedDate().equals(today)) {
                existing.setStreakCount(Math.max(0, existing.getStreakCount() - 1));
                existing.setLastCompletedDate(null);
            }
        }

        existing.setName(updated.getName());
        existing.setCompleted(updated.isCompleted());

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

    // Manuelles Resetting (Wird von Cron Job eigentlich automatisch übernommen)
    public void resetAllHabitsForNewDay() {
        Iterable<Habit> habits = repo.findAll();
        habits.forEach(Habit::resetForNewDay);
        repo.saveAll(habits);
    }

    public Habit completeHabit(Long id, boolean completed) {
        Habit habit = repo.findById(id).orElseThrow();
        LocalDate today = LocalDate.now();

        if (completed) {
            // Cron hat gestern gecheckt → immer +1!
            habit.setStreakCount(habit.getStreakCount() + 1);
            habit.setLastCompletedDate(today);
            habit.setCompleted(true);
        } else {
            // Offen → Reset (sofort!)
            habit.setStreakCount(0);
            habit.setLastCompletedDate(null);
            habit.setCompleted(false);
        }

        return repo.save(habit);
    }




}

