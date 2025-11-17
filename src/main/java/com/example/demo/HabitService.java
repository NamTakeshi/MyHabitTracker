package com.example.demo;

import org.springframework.stereotype.Service;

// Frontend → Controller → Service → Repository → Datenbank
// Der Service ist das Gehirn zwischen Controller und Repository.
@Service
public class HabitService {

    private final HabitRepository repo;

    public HabitService(HabitRepository repo) {
        this.repo = repo;
    }

    public Iterable<Habit> getAll() {
        return repo.findAll();
    }

    public Habit addHabit(Habit h) {
        return repo.save(h);
    }
}

