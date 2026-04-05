package com.musclefit.app.auth;

public class AuthState {
    public final boolean loggedIn;
    public final String username;
    public final AuthRole role;

    public AuthState(boolean loggedIn, String username, AuthRole role) {
        this.loggedIn = loggedIn;
        this.username = username;
        this.role = role;
    }

    public static AuthState guest() {
        return new AuthState(false, "", AuthRole.GUEST);
    }
}
