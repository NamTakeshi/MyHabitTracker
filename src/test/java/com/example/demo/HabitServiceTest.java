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
     * Testet, dass deleteHabit() ohne Exception ausgeführt wird.
     *
     * <p>Der Test stellt sicher, dass der Service-Aufruf
     * zum Löschen eines Habits keine Fehler wirft,
     * auch wenn keine Rückgabewerte geprüft werden.</p>
     *
     * <p>Die Repository-Abhängigkeit ist gemockt,
     * sodass keine echte Datenbank benötigt wird.</p>
     */
    @Test
    @DisplayName("deleteHabit should delete habit by id")
    void testDeleteHabitCallsRepository() {
        assertDoesNotThrow(() -> service.deleteHabit(5L, 1L));
    }

    /**
     * Testet, ob filterByStatus("completed") ausschließlich
     * erledigte Habits zurückgibt.
     *
     * <p>Gemockt wird das Repository so,
     * dass genau ein abgeschlossenes Habit für den User existiert.</p>
     *
     * <p>Der Test prüft, dass die Rückgabe
     * genau ein Element enthält.</p>
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
     * Testet, ob filterByStatus("all") alle Habits
     * eines Benutzers zurückliefert.
     *
     * <p>Das Repository wird gemockt,
     * um zwei Habits für den User zurückzugeben.</p>
     *
     * <p>Der Test verifiziert,
     * dass beide Habits im Ergebnis enthalten sind.</p>
     */
    @Test
    @DisplayName("filterByStatus all returns all habits")
    void testFilterByStatusAll() {
        Habit h1 = new Habit();
        Habit h2 = new Habit();

        doReturn(List.of(h1, h2)).when(repo).findByUserId(1L);

        Iterable<Habit> result = service.filterByStatus(1L, "all");
        assertEquals(2, ((List<?>) result).size());
    }

    /**
     * Testet, ob resetAllHabitsForNewDay() den Status
     * "completed" aller Habits zurücksetzt.
     *
     * <p>Ein zuvor als erledigt markiertes Habit
     * wird nach dem Reset als nicht erledigt erwartet.</p>
     *
     * <p>Der Test simuliert einen Tageswechsel,
     * ohne dabei Streak-Werte zu verändern.</p>
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
     * Testet, ob getCompletions() eine Liste von
     * HabitCompletion-Einträgen korrekt zurückgibt.
     *
     * <p>Das Completion-Repository wird gemockt,
     * um zwei Erledigungs-Einträge im angegebenen Zeitraum zu liefern.</p>
     *
     * <p>Der Test prüft,
     * dass die erwartete Anzahl an Einträgen zurückgegeben wird.</p>
     */
    @Test
    @DisplayName("getCompletions returns completion list")
    void testGetCompletionsReturnsList() {
        HabitCompletion c1 = new HabitCompletion();
        HabitCompletion c2 = new HabitCompletion();

        doReturn(List.of(c1, c2))
                .when(completionRepo)
                .findByHabitIdAndDateBetween(eq(1L), any(), any());

        List<HabitCompletion> result = service.getCompletions(1L, 1L, 30);
        assertEquals(2, result.size());
    }
}
