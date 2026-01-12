package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository repo;

    @Mock
    private HabitCompletionRepository completionRepo;

    @Mock
    private AppUserRepository userRepo;

    @InjectMocks  // ← Service mit Mocks!
    private HabitService service;

    /**
     * Testet ob addHabit() ein neues Habit korrekt mit Streak=0 anlegt.
     *
     * Mocks:
     * - userRepo.findById(1L) → Fake-User ID 1
     * - repo.save(any()) → Fake-Habit (ID=10, name="Joggen", streakCount=0)
     *
     * Prüft:
     * - result.getStreakCount() == 0 (neues Habit hat Streak 0)
     * - result.getName() == "Joggen" (Name bleibt erhalten)
     */
    @Test
    @DisplayName("should add habit with streak count 0")
    void testAddHabitSetsStreakZero() {
        // 1. Mock User
        AppUser mockUser = new AppUser();
        mockUser.setId(1L);
        doReturn(Optional.of(mockUser)).when(userRepo).findById(1L);

        // 2. Mock Save → WICHTIG: Name setzen!
        Habit mockSaved = new Habit();
        mockSaved.setId(10L);
        mockSaved.setStreakCount(0);
        mockSaved.setName("Joggen");
        doReturn(mockSaved).when(repo).save(any(Habit.class));

        // 3. Input
        Habit input = new Habit();
        input.setName("Joggen");

        // 4. Test
        Habit result = service.addHabit(input, 1L);

        // 5. Asserts
        assertEquals(0, result.getStreakCount());
        assertEquals("Joggen", result.getName());
    }

    /**
     * Testet ob getAll(userId) die Habits eines Users korrekt zurückgibt.
     *
     * Mocks:
     * - repo.findByUserId(1L) → List mit 1 Fake-Habit
     *
     * Prüft:
     * - result.size() == 1 (genau 1 Habit für User 1)
     */
    @Test
    @DisplayName("getAll should return user habits")
    void testGetAllReturnsUserHabits() {
        Habit habit1 = new Habit();
        habit1.setName("Habit1");
        doReturn(List.of(habit1)).when(repo).findByUserId(1L);

        Iterable<Habit> result = service.getAll(1L);
        assertEquals(1, ((List<?>) result).size());
    }

    /**
     * Testet ob completeHabit() bei erster Erledigung Streak auf 1 setzt.
     *
     * Mocks:
     * - repo.findById(1L) → Leeres Habit
     * - completionRepo.findByHabitIdAndDate(1L, date) → Bestehende Completion
     * - repo.save() → Habit mit streakCount=1
     *
     * Prüft:
     * - result.getStreakCount() == 1 (erste Completion → Streak 1)
     */
    @Test
    @DisplayName("completeHabit first completion sets streak 1")
    void testCompleteHabitSetsStreakOne() {
        Habit mockHabit = new Habit();
        mockHabit.setId(1L);
        doReturn(Optional.of(mockHabit)).when(repo).findById(1L);

        HabitCompletion mockComp = new HabitCompletion();
        doReturn(Optional.of(mockComp)).when(completionRepo)
                .findByHabitIdAndDate(eq(1L), any(LocalDate.class));  // ← FIX!

        doReturn(mockComp).when(completionRepo).save(any());

        Habit mockSaved = new Habit();
        mockSaved.setStreakCount(1);
        doReturn(mockSaved).when(repo).save(any());

        Habit result = service.completeHabit(1L, true, "2026-01-05", 1L);
        assertEquals(1, result.getStreakCount());
    }


    /**
     * Testet ob completeHabit() bei aufeinanderfolgenden Tagen Streak erhöht.
     *
     * Mocks:
     * - repo.findById(1L) → Habit mit lastCompletedDate=2026-01-05, streakCount=1
     * - completionRepo.findByHabitIdAndDate(1L, 2026-01-06) → Keine Completion
     * - repo.save() → Habit mit streakCount=2
     *
     * Prüft:
     * - result.getStreakCount() == 2 (Tag 1→2 → Streak +1)
     */
    @Test
    @DisplayName("completeHabit consecutive days increases streak")
    void testStreakIncreasesConsecutive() {
        Habit mockHabit = new Habit();
        mockHabit.setId(1L);
        mockHabit.setLastCompletedDate(LocalDate.parse("2026-01-05"));
        mockHabit.setStreakCount(1);
        doReturn(Optional.of(mockHabit)).when(repo).findById(1L);

        HabitCompletion mockComp = new HabitCompletion();
        doReturn(Optional.empty()).when(completionRepo).findByHabitIdAndDate(1L, LocalDate.parse("2026-01-06"));
        doReturn(mockComp).when(completionRepo).save(any());

        Habit mockSaved = new Habit();
        mockSaved.setStreakCount(2);
        doReturn(mockSaved).when(repo).save(any());

        Habit result = service.completeHabit(1L, true, "2026-01-06", 1L);
        assertEquals(2, result.getStreakCount());
    }

    /**
     * Testet ob filterByStatus("active") nur aktive Habits zurückgibt.
     *
     * Mocks:
     * - repo.findByUserIdAndCompletedFalse(1L) → List mit 1 aktiven Habit
     *
     * Prüft:
     * - result.size() == 1 (nur aktive Habits für User 1)
     */
    @Test
    @DisplayName("filterByStatus active returns only active habits")
    void testFilterByStatusActive() {
        Habit active = new Habit();
        active.setCompleted(false);
        doReturn(List.of(active)).when(repo).findByUserIdAndCompletedFalse(1L);

        Iterable<Habit> result = service.filterByStatus(1L, "active");
        assertEquals(1, ((List<?>) result).size());
    }

    /**
     * Testet ob deleteHabit() ohne Fehler ausgeführt wird.
     *
     * Mocks:
     * - repo.deleteById(5L) → kein Verhalten (void)
     *
     * Prüft:
     * - es wird keine Exception geworfen
     */
    @Test
    @DisplayName("deleteHabit should delete habit by id")
    void testDeleteHabitCallsRepository() {
        assertDoesNotThrow(() -> service.deleteHabit(5L, 1L));
    }

    /**
     * Testet ob filterByStatus("completed") nur erledigte Habits zurückgibt.
     *
     * Mocks:
     * - repo.findByUserIdAndCompletedTrue(1L) → List mit 1 erledigtem Habit
     *
     * Prüft:
     * - result.size() == 1
     */
    @Test
    @DisplayName("filterByStatus completed returns only completed habits")
    void testFilterByStatusCompleted() {
        Habit completed = new Habit();
        completed.setCompleted(true);

        doReturn(List.of(completed))
                .when(repo).findByUserIdAndCompletedTrue(1L);

        Iterable<Habit> result = service.filterByStatus(1L, "completed");
        assertEquals(1, ((List<?>) result).size());
    }

    /**
     * Testet ob resetAllHabitsForNewDay() alle Habits zurücksetzt.
     *
     * Mocks:
     * - repo.findAll() → List mit 1 erledigtem Habit
     * - repo.saveAll(...) → gespeicherte Habits
     *
     * Prüft:
     * - completed == false nach dem Reset
     */

    @Test
    @DisplayName("resetAllHabitsForNewDay resets completed flag")
    void testResetAllHabitsForNewDay() {
        Habit h = new Habit();
        h.setCompleted(true);

        doReturn(List.of(h)).when(repo).findAll();
        doReturn(List.of(h)).when(repo).saveAll(any());

        service.resetAllHabitsForNewDay();

        assertFalse(h.isCompleted());
    }

    /**
     * Testet ob addHabit() fehlschlägt, wenn der User nicht existiert.
     *
     * Mocks:
     * - userRepo.findById(99L) → Optional.empty()
     *
     * Prüft:
     * - IllegalArgumentException wird geworfen
     */

    @Test
    @DisplayName("addHabit fails if user does not exist")
    void addHabitFailsWhenUserNotFound() {
        doReturn(Optional.empty())
                .when(userRepo).findById(99L);

        Habit input = new Habit();
        input.setName("Joggen");

        assertThrows(IllegalArgumentException.class, () ->
                service.addHabit(input, 99L)
        );
    }

    /**
     * Testet ob ein Undo (completed=false) keinen negativen Streak erzeugt.
     *
     * Mocks:
     * - repo.findById(1L) → Habit mit streakCount = 0
     * - completionRepo.findByHabitIdAndDate(...) → bestehende Completion
     *
     * Prüft:
     * - streakCount bleibt 0
     */

    @Test
    @DisplayName("undo completion does not set negative streak")
    void undoDoesNotCreateNegativeStreak() {
        Habit habit = new Habit();
        habit.setId(1L);
        habit.setStreakCount(0);

        doReturn(Optional.of(habit)).when(repo).findById(1L);

        HabitCompletion comp = new HabitCompletion();
        comp.setCompleted(true);

        doReturn(Optional.of(comp))
                .when(completionRepo)
                .findByHabitIdAndDate(eq(1L), any());

        doReturn(comp).when(completionRepo).save(any());
        doReturn(habit).when(repo).save(any());

        Habit result = service.completeHabit(1L, false, "2026-01-06", 1L);

        assertEquals(0, result.getStreakCount());
    }

    /**
     * Testet ob completeHabit() fehlschlägt, wenn das Habit nicht existiert.
     *
     * Mocks:
     * - repo.findById(42L) → Optional.empty()
     *
     * Prüft:
     * - IllegalArgumentException wird geworfen
     */
    @Test
    @DisplayName("completeHabit fails if habit not found")
    void completeHabitFailsWhenHabitMissing() {
        doReturn(Optional.empty())
                .when(repo).findById(42L);

        assertThrows(IllegalArgumentException.class, () ->
                service.completeHabit(42L, true, "2026-01-06", 1L)
        );
    }

    /**
     * Testet ob filterByStatus(null) alle Habits zurückgibt.
     *
     * Mocks:
     * - repo.findByUserId(1L) → List mit 2 Habits
     *
     * Prüft:
     * - result.size() == 2
     */
    @Test
    @DisplayName("filterByStatus with null returns all habits")
    void filterByStatusNullReturnsAll() {
        Habit h1 = new Habit();
        Habit h2 = new Habit();

        doReturn(List.of(h1, h2))
                .when(repo).findByUserId(1L);

        Iterable<Habit> result = service.filterByStatus(1L, null);

        assertEquals(2, ((List<?>) result).size());
    }

    /**
     * Testet ob updateHabit() fehlschlägt, wenn das Habit nicht existiert.
     *
     * Mocks:
     * - repo.findById(99L) → Optional.empty()
     *
     * Prüft:
     * - IllegalArgumentException wird geworfen
     */

    @Test
    @DisplayName("updateHabit fails if habit does not exist")
    void updateHabitFailsWhenHabitMissing() {
        doReturn(Optional.empty())
                .when(repo).findById(99L);

        Habit updated = new Habit();
        updated.setName("Neu");

        assertThrows(IllegalArgumentException.class, () ->
                service.updateHabit(99L, updated, 1L)
        );
    }
}
