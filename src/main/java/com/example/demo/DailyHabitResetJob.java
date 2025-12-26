package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Führt täglich um 00:00 automatisch {@link HabitService#resetAllHabitsForNewDay()}
 * aus. Setzt alle Habits auf {@code completed = false}, behält Streaks bei.
 *
 * @author Nam Phan
 */
@Component
public class DailyHabitResetJob {

    private final HabitRepository habitRepo;

    public DailyHabitResetJob(HabitRepository habitRepo) {
        this.habitRepo = habitRepo;
    }

    // Dieser Job läuft jeden Tag um Punkt 00:00:00 Uhr
    @Scheduled(cron = "0 0 0 * * *")
    public void resetStreaksUndStatus() {
        LocalDate gestern = LocalDate.now().minusDays(1);
        Iterable<Habit> alleHabits = habitRepo.findAll();

        for (Habit habit : alleHabits) {

            if (habit.getLastCompletedDate() == null || !habit.getLastCompletedDate().equals(gestern)) {
                habit.setStreakCount(0);
            }
            habit.setCompleted(false);

            habitRepo.save(habit);
        }
        System.out.println("System: Alle Streaks wurden geprüft und die Status für den neuen Tag zurückgesetzt.");
    }
}

