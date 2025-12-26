package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Diese Entity speichert den Status einer Gewohnheit für ein ganz bestimmtes Datum.
 * Sie ermöglicht es, eine Historie anzuzeigen.
 */
@Entity
public class HabitCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mehrere Erledigungen (Completions) gehören zu einem Habit.
    // optional = false bedeutet: Eine Erledigung darf nicht ohne Habit existieren.
    @ManyToOne(optional = false)
    private Habit habit;

    private LocalDate date;
    private boolean completed;
    public HabitCompletion() {}

    /**
     * Konstruktor zum Erstellen eines neuen Eintrags.
     * @param habit Das zugehörige Habit-Objekt.
     * @param date Der Tag der Erledigung.
     * @param completed Status der Erledigung.
     */
    public HabitCompletion(Habit habit, LocalDate date, boolean completed) {
        this.habit = habit;
        this.date = date;
        this.completed = completed;
    }

    // Getter und Setter
    public Long getId() { return id; }

    public Habit getHabit() { return habit; }
    public void setHabit(Habit habit) { this.habit = habit; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
