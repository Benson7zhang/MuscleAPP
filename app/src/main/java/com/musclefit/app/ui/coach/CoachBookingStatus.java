package com.musclefit.app.ui.coach;

import android.content.Context;

import com.musclefit.app.R;

public final class CoachBookingStatus {
    public static final String PENDING_COACH = "PENDING_COACH";
    public static final String COACH_ACCEPTED = "COACH_ACCEPTED";
    public static final String COACH_REJECTED = "COACH_REJECTED";
    public static final String ADMIN_APPROVED = "ADMIN_APPROVED";
    public static final String ADMIN_REJECTED = "ADMIN_REJECTED";

    private CoachBookingStatus() {
    }

    public static int labelRes(String status) {
        if (COACH_ACCEPTED.equals(status)) {
            return R.string.booking_status_coach_accepted;
        }
        if (COACH_REJECTED.equals(status)) {
            return R.string.booking_status_coach_rejected;
        }
        if (ADMIN_APPROVED.equals(status)) {
            return R.string.booking_status_admin_approved;
        }
        if (ADMIN_REJECTED.equals(status)) {
            return R.string.booking_status_admin_rejected;
        }
        return R.string.booking_status_pending_coach;
    }

    public static String label(Context context, String status) {
        return context.getString(labelRes(status));
    }

    public static boolean canCoachHandle(String status) {
        return PENDING_COACH.equals(status);
    }

    public static boolean canAdminHandle(String status) {
        return COACH_ACCEPTED.equals(status);
    }
}
