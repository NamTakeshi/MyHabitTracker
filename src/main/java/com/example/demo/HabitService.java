package com.example.demo;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// Frontend → Controller → Service → Repository → Datenbank
// Der Service ist das Gehirn zwischen Controller und Repository.

@Service
public class HabitService {

    private final HabitRepository repo;
    private final HabitCompletionRepository completionRepo;
    private final AppUserRepository userRepo; //neu AppUser

    public HabitService(HabitRepository repo, HabitCompletionRepository completionRepo, AppUserRepository userRepo) {
        this.repo = repo;
        this.completionRepo = completionRepo;
        this.userRepo = userRepo; //neu
    }

    //new AppUser
    public Iterable<Habit> getAll(Long userId) {
        return repo.findByUserId(userId);
    }

    //new AppUser
    public Habit addHabit(Habit h, Long userId) {
        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        h.setUser(user);
        h.setCompleted(false);
        h.setStreakCount(0);
        return repo.save(h);
    }

    public void deleteHabit(Long id, Long userId) {
        repo.deleteById(id);
    }

    public Habit updateHabit(Long id, Habit updated, Long userId) {
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

    public Habit checkHabit(Long id, Long userId) {
        Habit habit = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found: " + id));

        habit.setCompleted(true);
        habit.setLastCompletedDate(java.time.LocalDate.now());
        // Streak-Logik kannst du später ergänzen
        return repo.save(habit);
    }

    //new AppUser
    // Optional: simple Filter-Logik (nur nach completed)
    public Iterable<Habit> filterByStatus(Long userId, String status) {
        if ("completed".equalsIgnoreCase(status)) {
            return repo.findByUserIdAndCompletedTrue(userId);
        } else if ("active".equalsIgnoreCase(status)) {
            return repo.findByUserIdAndCompletedFalse(userId);
        }
        return repo.findByUserId(userId);
    }

    // Manuelles Resetting (Wird von Cron Job eigentlich automatisch übernommen)
    public void resetAllHabitsForNewDay() {
        Iterable<Habit> habits = repo.findAll();
        habits.forEach(Habit::resetForNewDay);
        repo.saveAll(habits);
    }

    public Habit completeHabit(Long id, boolean completed, String dateStr, Long userId) {
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
    public List<HabitCompletion> getCompletions(Long habitId, Long userId, int daysBack) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysBack - 1L);

        return completionRepo.findByHabitIdAndDateBetween(habitId, startDate, endDate);
    }
}