package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;  // ← Oben!

import java.time.LocalDate;

/**
 * Cron-Job: Läuft täglich um 00:00 Uhr.
 * Setzt alle Habits auf 'completed=false', behält aber gültige Streaks (gestern erledigt).
 */
@Component
public class DailyHabitResetJob {

    private static final Logger log = LoggerFactory.getLogger(DailyHabitResetJob.class);  // ← Oben nach Imports!

    private final HabitRepository habitRepo;

    public DailyHabitResetJob(HabitRepository habitRepo) {
        this.habitRepo = habitRepo;
    }

    @Scheduled(cron = "0 * * * * *", zone = "Europe/Berlin")  // ← TEST: Jede Minute!
    public void resetStreaksUndStatus() {
        log.info("🔄 Reset-Job läuft: {}", LocalDate.now());  // ← Zuerst!

        LocalDate gestern = LocalDate.now().minusDays(1);
        Iterable<Habit> alleHabits = habitRepo.findAll();

        for (Habit habit : alleHabits) {
            if (habit.getLastCompletedDate() == null || !habit.getLastCompletedDate().equals(gestern)) {
                habit.setStreakCount(0);
            }
            habit.setCompleted(false);
            habitRepo.save(habit);
        }

        log.info("✅ Reset fertig – {} Habits", alleHabits.spliterator().getExactSizeIfKnown());
    }
}