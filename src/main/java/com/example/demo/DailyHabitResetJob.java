package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Führt täglich um 00:00 automatisch {@link HabitService#resetAllHabitsForNewDay()}
 * aus. Setzt alle Habits auf {@code completed = false}, behält Streaks bei.
 *
 * @author Nam Phan
 */
@Component
public class DailyHabitResetJob {
    private final HabitService habitService;

    public DailyHabitResetJob(HabitService habitService) {
        this.habitService = habitService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetAllHabitsForNewDay() {
        habitService.resetAllHabitsForNewDay();
        System.out.println("✅ AUTO Daily reset executed");
    }
}

