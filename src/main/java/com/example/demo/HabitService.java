package com.example.demo;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// Frontend → Controller → Service → Repository → Datenbank
// Der Service ist das Gehirn zwischen Controller und Repository.

/**
 * Service-Klasse für die Habit-Logik.
 * Hier wird gesteuert, wie Gewohnheiten erstellt, aktualisiert und mit Streaks verrechnet werden.
 */
@Service
public class HabitService {

    private final HabitRepository repo;
    private final HabitCompletionRepository completionRepo;
    private final AppUserRepository userRepo; //neu AppUser

    // Konstruktor: Injiziert die benötigten Repositories
    public HabitService(HabitRepository repo, HabitCompletionRepository completionRepo, AppUserRepository userRepo) {
        this.repo = repo;
        this.completionRepo = completionRepo;
        this.userRepo = userRepo; //neu
    }

    /**
     * Holt alle Gewohnheiten eines bestimmten Benutzers.
     * @param userId Die ID des Benutzers, dessen Habits geladen werden sollen.
     * @return Eine Liste aller Habits dieses Users.
     */
    public Iterable<Habit> getAll(Long userId) {
        return repo.findByUserId(userId);
    }

    /**
     * Erstellt eine neue Gewohnheit für einen Benutzer.
     * @param h Das Habit-Objekt mit den Grunddaten (Name, etc.).
     * @param userId Die ID des Besitzers.
     */
    public Habit addHabit(Habit h, Long userId) {
        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        h.setUser(user);
        h.setCompleted(false);
        h.setStreakCount(0);
        return repo.save(h);
    }

    /**
     * Löscht eine Gewohnheit permanent.
     * @param id Die ID des zu löschenden Habits.
     * @param userId (Optional/Sicherheit) Die ID des Users zur Validierung.
     */
    public void deleteHabit(Long id, Long userId) {
        repo.deleteById(id);
    }

    /**
     * Aktualisiert die Daten eines Habits und berechnet den Streak neu.
     * @param id Die ID des existierenden Habits.
     * @param updated Das Objekt mit den neuen Werten aus dem Frontend.
     * @param userId Die ID des Users.
     */
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

    /**
     * Markiert ein Habit als heute erledigt (vereinfachte Version).
     * @param id Die ID des Habits.
     */
    public Habit checkHabit(Long id, Long userId) {
        Habit habit = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found: " + id));

        habit.setCompleted(true);
        habit.setLastCompletedDate(java.time.LocalDate.now());
        // Streak-Logik kannst du später ergänzen
        return repo.save(habit);
    }

    /**
     * Filtert Habits nach ihrem Status.
     * @param userId Der Besitzer der Habits.
     * @param status Entweder "completed", "active" oder "all".
     */
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
                .orElseThrow(() -> new IllegalArgumentException("Habit nicht gefunden: " + id));

        LocalDate abschlussDatum = parseDateOrToday(dateStr);
        LocalDate heute = LocalDate.now();
        LocalDate gestern = heute.minusDays(1);

        // Den Status für den spezifischen Tag in der HabitCompletion-Tabelle speichern
        HabitCompletion completion = completionRepo
                .findByHabitIdAndDate(id, abschlussDatum)
                .orElseGet(() -> {
                    HabitCompletion c = new HabitCompletion();
                    c.setHabit(habit);
                    c.setDate(abschlussDatum);
                    return c;
                });

        boolean warBereitsErledigt = completion.isCompleted();
        completion.setCompleted(completed);
        completionRepo.save(completion);

        // Logik für den Streak-Zähler
        if (!warBereitsErledigt && completed) {
            // STREAK-BERECHNUNG:
            if (habit.getLastCompletedDate() != null && habit.getLastCompletedDate().equals(gestern)) {
                // Wenn gestern erledigt wurde -> Streak um 1 erhöhen
                habit.setStreakCount(habit.getStreakCount() + 1);
            } else {
                // Wenn gestern nichts gemacht wurde oder es das erste Mal ist -> Streak startet bei 1
                habit.setStreakCount(1);
            }
            habit.setLastCompletedDate(abschlussDatum);
            habit.setCompleted(true);
        }
        else if (warBereitsErledigt && !completed) {
            // Wenn der Haken entfernt wird (Undo) -> Streak um 1 verringern
            habit.setStreakCount(Math.max(0, habit.getStreakCount() - 1));
            habit.setCompleted(false);
        }

        return repo.save(habit);
    }

    /**
     * Wandelt einen Datums-String in ein LocalDate um.
     * @param dateStr Datum als Text.
     * @return Das LocalDate Objekt (oder heute, falls Text leer ist).
     */
    private LocalDate parseDateOrToday(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        return LocalDate.parse(dateStr);
    }

    /**
     * Lädt die Erledigungen für die Heatmap (Kalender-Ansicht).
     * @param habitId ID des Habits.
     * @param daysBack Anzahl der Tage, die zurückgeschaut werden soll (z.B. 90 Tage).
     */
    public List<HabitCompletion> getCompletions(Long habitId, Long userId, int daysBack) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysBack - 1L);

        return completionRepo.findByHabitIdAndDateBetween(habitId, startDate, endDate);
    }
}