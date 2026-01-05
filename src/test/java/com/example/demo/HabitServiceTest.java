package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
 public class HabitServiceTest {
    @Autowired HabitService service;
    @Autowired
    UserService userService;

    @Test
    void test01_registerUserCreatesAccount() {
        AppUser user = userService.register("test1", "123");
        assertNotNull(user.getId());
        assertEquals("test1", user.getUsername());
    }

    @Test
    void test02_addHabitSavesCorrectly() {
        AppUser user = userService.register("habituser", "123");
        Habit habit = service.addHabit(new Habit("Joggen"), user.getId());
        assertEquals("Joggen", habit.getName());
        assertNotNull(habit.getId());
    }

    @Test
    void test03_completeHabitSetsStreakOne() {
        AppUser user = userService.register("streak1", "123");
        Habit habit = service.addHabit(new Habit("Test"), user.getId());
        Habit completed = service.completeHabit(habit.getId(), true, "2026-01-05", user.getId());
        assertEquals(1, completed.getStreakCount());
    }

    @Test
    void test04_streakIncreasesOnConsecutiveDays() {
        AppUser user = userService.register("streak2", "123");
        Habit habit = service.addHabit(new Habit("Test"), user.getId());
        service.completeHabit(habit.getId(), true, "2026-01-05", user.getId());  // Tag 1
        Habit day2 = service.completeHabit(habit.getId(), true, "2026-01-06", user.getId());  // Tag 2
        assertEquals(2, day2.getStreakCount());
    }

    @Test
    void test05_streakResetsAfterMissedDay() {
        AppUser user = userService.register("streak3", "123");
        Habit habit = service.addHabit(new Habit("Test"), user.getId());
        service.completeHabit(habit.getId(), true, "2026-01-05", user.getId());
        service.completeHabit(habit.getId(), true, "2026-01-07", user.getId());  // Skip 06!
        Habit reset = service.getHabit(habit.getId());  // Nach Reset-Job
        assertEquals(1, reset.getStreakCount());  // Reset!
    }
}

