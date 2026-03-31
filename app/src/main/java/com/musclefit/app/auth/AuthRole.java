package com.musclefit.app.auth;

public enum AuthRole {
    GUEST,
    USER,
    COACH,
    ADMIN;

    public static AuthRole fromName(String raw) {
        if (raw == null) {
            return GUEST;
        }
        for (AuthRole role : values()) {
            if (role.name().equals(raw)) {
                return role;
            }
        }
        return GUEST;
    }
}
