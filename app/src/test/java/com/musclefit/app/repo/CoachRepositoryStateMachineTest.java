package com.musclefit.app.repo;

import com.musclefit.app.ui.coach.CoachBookingStatus;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoachRepositoryStateMachineTest {
    @Test
    public void coachCanHandlePendingOnly() {
        assertTrue(CoachRepository.isTransitionAllowed(
                CoachBookingStatus.PENDING_COACH,
                CoachBookingStatus.COACH_ACCEPTED
        ));
        assertTrue(CoachRepository.isTransitionAllowed(
                CoachBookingStatus.PENDING_COACH,
                CoachBookingStatus.COACH_REJECTED
        ));
        assertFalse(CoachRepository.isTransitionAllowed(
                CoachBookingStatus.COACH_ACCEPTED,
                CoachBookingStatus.COACH_REJECTED
        ));
    }

    @Test
    public void adminCanHandleCoachAcceptedOnly() {
        assertTrue(CoachRepository.isTransitionAllowed(
                CoachBookingStatus.COACH_ACCEPTED,
                CoachBookingStatus.ADMIN_APPROVED
        ));
        assertTrue(CoachRepository.isTransitionAllowed(
                CoachBookingStatus.COACH_ACCEPTED,
                CoachBookingStatus.ADMIN_REJECTED
        ));
        assertFalse(CoachRepository.isTransitionAllowed(
                CoachBookingStatus.PENDING_COACH,
                CoachBookingStatus.ADMIN_APPROVED
        ));
    }
}
