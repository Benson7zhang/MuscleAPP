package com.musclefit.app.auth;

public enum AuthRole {
    GUEST,
    USER,
    ADMIN;

    public static AuthRole fromName(String raw) {
        if (raw == null) {
            return GUEST;
        }
        if ("COACH".equals(raw)) {
            return USER;
        }
        for (AuthRole role : values()) {
            if (role.name().equals(raw)) {
                return role;
            }
        }
        return GUEST;
    }
}
