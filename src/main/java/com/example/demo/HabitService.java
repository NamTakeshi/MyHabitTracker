package com.example.demo;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Frontend → Controller → Service → Repository → Datenbank
// Der Service ist das Gehirn zwischen Controller und Repository.

@Service
public class HabitService {

    private final HabitRepository repo;
    private final HabitCompletionRepository completionRepo;

    public HabitService(HabitRepository repo, HabitCompletionRepository completionRepo) {
        this.repo = repo;
        this.completionRepo = completionRepo;
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

        // NEU: alle zusätzlichen Felder übernehmen
        existing.setCategory(updated.getCategory());
        existing.setTargetAmount(updated.getTargetAmount());
        existing.setTargetUnit(updated.getTargetUnit());
        existing.setFrequency(updated.getFrequency());
        existing.setNotes(updated.getNotes());
        existing.setColor(updated.getColor());
        existing.setIcon(updated.getIcon());

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

    public Habit completeHabit(Long id, boolean completed, String dateStr) {
        Habit habit = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found: " + id));

        LocalDate completionDate = parseDateOrToday(dateStr);

        HabitCompletion completion = completionRepo
                .findByHabitIdAndDate(id, completionDate)
                .orElseGet(() -> {
                    HabitCompletion c = new HabitCompletion();
                    c.setHabit(habit);
                    c.setDate(completionDate);
                    return c;
                });

        boolean wasCompleted = completion.isCompleted();
        completion.setCompleted(completed);
        completionRepo.save(completion);

        // Streak nur erhöhen, wenn von false -> true
        if (!wasCompleted && completed) {
            habit.setStreakCount(habit.getStreakCount() + 1);
            habit.setLastCompletedDate(completionDate);
            habit.setCompleted(true);
        }

        // optional: bei false ggf. Streak anpassen, je nach Business-Logik
        if (!completed) {
            habit.setCompleted(false);
            // habit.setStreakCount(0); // falls du beim Undo alles resetten willst
            // habit.setLastCompletedDate(null);
        }

        return repo.save(habit);
    }


    // 🔥 Parsing in Service (sauber!)
    private LocalDate parseDateOrToday(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        return LocalDate.parse(dateStr);
    }


    // 🔥 HEATMAP: 90 Tage Completions laden
    public List<HabitCompletion> getCompletions(Long habitId, int daysBack) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysBack - 1L);

        return completionRepo.findByHabitIdAndDateBetween(habitId, startDate, endDate);
    }




}

