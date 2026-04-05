package com.musclefit.app.repo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InteractionToggleGuard {
    private final long minIntervalMs;
    private final Map<String, Long> lastActionAt = new HashMap<>();
    private final Set<String> inFlight = new HashSet<>();

    public InteractionToggleGuard(long minIntervalMs) {
        this.minIntervalMs = minIntervalMs;
    }

    public synchronized ToggleResult tryAcquire(String key, long nowMs) {
        Long lastTime = lastActionAt.get(key);
        if (lastTime != null && nowMs - lastTime < minIntervalMs) {
            return ToggleResult.TOO_FAST;
        }
        if (inFlight.contains(key)) {
            return ToggleResult.BUSY;
        }
        inFlight.add(key);
        return ToggleResult.SUCCESS;
    }

    public synchronized void release(String key, long nowMs) {
        inFlight.remove(key);
        lastActionAt.put(key, nowMs);
    }
}
