package com.example.demo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

// Frontend → Controller → Service → Repository → Datenbank
// Repository für Kommunikation mit DB
@Repository
public interface HabitRepository extends CrudRepository<Habit, Long> {

    Iterable<Habit> findByCompletedTrue();
    Iterable<Habit> findByCompletedFalse();

}
