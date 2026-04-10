package com.musclefit.app.auth;

import android.content.Context;

import com.musclefit.app.R;

public final class RolePolicy {
    private RolePolicy() {
    }

    public static String roleLabel(Context context, AuthRole role) {
        if (role == AuthRole.ADMIN) {
            return context.getString(R.string.role_admin);
        }
        if (role == AuthRole.USER) {
            return context.getString(R.string.role_user);
        }
        return context.getString(R.string.role_guest);
    }

    public static String roleDuty(Context context, AuthRole role) {
        if (role == AuthRole.ADMIN) {
            return context.getString(R.string.role_duty_admin);
        }
        if (role == AuthRole.USER) {
            return context.getString(R.string.role_duty_user);
        }
        return context.getString(R.string.role_duty_guest);
    }
}
