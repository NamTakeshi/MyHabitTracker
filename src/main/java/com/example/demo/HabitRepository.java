package com.example.demo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

// Frontend → Controller → Service → Repository → Datenbank
@Repository
public interface HabitRepository extends CrudRepository<Habit, Long> {
    Iterable<Habit> findByUserId(Long userId);
    Iterable<Habit> findByUserIdAndCompletedTrue(Long userId);
    Iterable<Habit> findByUserIdAndCompletedFalse(Long userId);

    // für Account-Löschung
    void deleteByUserId(Long userId);
}
