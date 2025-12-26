package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class HabitCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Habit habit;
    private LocalDate date;
    private boolean completed;
    public HabitCompletion() {}

    // Konstruktor
    public HabitCompletion(Habit habit, LocalDate date, boolean completed) {
        this.habit = habit;
        this.date = date;
        this.completed = completed;
    }

    public Long getId() { return id; }

    public Habit getHabit() { return habit; }
    public void setHabit(Habit habit) { this.habit = habit; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
