package com.musclefit.app.repo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExerciseRepositoryMathTest {
    @Test
    public void toggleOnIncrementsByOne() {
        int updated = ExerciseRepository.adjustCount(10, true);
        assertEquals(11, updated);
    }

    @Test
    public void toggleOffDecrementsByOne() {
        int updated = ExerciseRepository.adjustCount(10, false);
        assertEquals(9, updated);
    }

    @Test
    public void toggleOffNeverNegative() {
        int updated = ExerciseRepository.adjustCount(0, false);
        assertEquals(0, updated);
    }
}
