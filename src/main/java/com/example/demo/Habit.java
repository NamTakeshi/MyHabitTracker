package com.example.demo;


import java.time.LocalDate;

public class Habit {

    private Long id;
    private String name;
    private boolean completed;
    private int streakCount;
    private LocalDate lastCompletedDate;

    // constructor
    public Habit(Long id, String name) {
        this.id = id;
        this.name = name;
        this.completed = false;
        this.streakCount = 0;
        this.lastCompletedDate = null;
    }

    // getter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public LocalDate getLastCompletedDate() {
        return lastCompletedDate;
    }

    // setter
    public void setLastCompletedDate(LocalDate lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    // methods
    // Tagesreset
    public void resetForNewDay() {
        this.completed = false;
    }
}
