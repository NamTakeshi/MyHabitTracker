package com.example.demo;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Die Haupt-Entity für ein Habit.
 * Hier werden Kerninformationen einer Gewohnheit und der aktuelle Fortschritt gespeichert.
 */
@Entity
@Table(name = "habits")
public class Habit {

    // Verknüpfung zu den einzelnen Erledigungen (Historie)
    // cascade = ALL: Wenn ein Habit gelöscht wird, werden auch alle Erledigungen gelöscht.
    // orphanRemoval = true: Verwaiste Einträge werden automatisch entfernt.
    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HabitCompletion> completions = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean completed;
    private int streakCount;
    private LocalDate lastCompletedDate;

    private String category;
    private Integer targetAmount;
    private String targetUnit;

    private String frequency;

    private String notes;
    private String color;
    private String icon;

    /**
     * Verknüpfung zum Besitzer der Gewohnheit.
     * @ManyToOne: Viele Habits gehören zu einem User.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "app_user_id", nullable = false) // "app_user_id" ist eindeutiger
    private AppUser user;


    // Konstruktor
    public Habit() {}
    public Habit(Long id, String name) {
        this.id = id;
        this.name = name;
        this.completed = false;
        this.streakCount = 0;
        this.lastCompletedDate = null;
    }

    //Getter und Setter
    public LocalDate getLastCompletedDate() {return lastCompletedDate;}
    public void setLastCompletedDate(LocalDate lastCompletedDate) {this.lastCompletedDate = lastCompletedDate;}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public boolean isCompleted() {return completed;}
    public void setCompleted(boolean completed) {this.completed = completed;}

    public int getStreakCount() {return streakCount;}
    public void setStreakCount(int streakCount) {this.streakCount = streakCount;}

    public String getCategory() {return category;}
    public void setCategory(String category) {this.category = category;}

    public String getTargetUnit() {return targetUnit;}
    public void setTargetUnit(String targetUnit) {this.targetUnit = targetUnit;}

    public Integer getTargetAmount() {return targetAmount;}
    public void setTargetAmount(Integer targetAmount) {this.targetAmount = targetAmount;}

    public String getFrequency() {return frequency;}
    public void setFrequency(String frequency) {this.frequency = frequency;}

    public String getNotes() {return notes;}
    public void setNotes(String notes) {this.notes = notes;}

    public String getColor() {return color;}
    public void setColor(String color) {this.color = color;}

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    //New App User
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    // Tagesreset
    public void resetForNewDay() {
        this.completed = false;
    }
}