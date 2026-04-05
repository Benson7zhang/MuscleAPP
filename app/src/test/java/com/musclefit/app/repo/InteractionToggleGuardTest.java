package com.musclefit.app.repo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InteractionToggleGuardTest {
    @Test
    public void rapidClicksOnlyAllowFirst() {
        InteractionToggleGuard guard = new InteractionToggleGuard(600L);
        String key = "LIKE:1";

        ToggleResult first = guard.tryAcquire(key, 1000L);
        guard.release(key, 1000L);

        ToggleResult second = guard.tryAcquire(key, 1100L);
        ToggleResult third = guard.tryAcquire(key, 1200L);

        assertEquals(ToggleResult.SUCCESS, first);
        assertEquals(ToggleResult.TOO_FAST, second);
        assertEquals(ToggleResult.TOO_FAST, third);
    }

    @Test
    public void inFlightBlocksConcurrentToggle() {
        InteractionToggleGuard guard = new InteractionToggleGuard(600L);
        String key = "FAVORITE:2";

        ToggleResult first = guard.tryAcquire(key, 1000L);
        ToggleResult second = guard.tryAcquire(key, 1001L);

        assertEquals(ToggleResult.SUCCESS, first);
        assertEquals(ToggleResult.BUSY, second);
    }
}
