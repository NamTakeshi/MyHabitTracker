package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    Optional<HabitCompletion> findByHabitIdAndDate(Long habitId, LocalDate date);

    List<HabitCompletion> findByHabitIdAndDateBetween(Long habitId, LocalDate from, LocalDate to);
}
