package com.musclefit.app.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthManager {
    private static final String PREF = "musclefit_auth";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";

    private static volatile AuthManager INSTANCE;

    private final SharedPreferences preferences;
    private final MutableLiveData<AuthState> state = new MutableLiveData<>(AuthState.guest());

    private AuthManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        state.setValue(readFromPrefs());
    }

    public static AuthManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AuthManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuthManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<AuthState> observe() {
        return state;
    }

    public AuthState getCurrent() {
        AuthState current = state.getValue();
        return current == null ? AuthState.guest() : current;
    }

    public boolean isLoggedIn() {
        return getCurrent().loggedIn;
    }

    public void login(String username, AuthRole role) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty()) {
            name = "用户";
        }
        AuthState next = new AuthState(true, name, role == null ? AuthRole.USER : role);
        preferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USERNAME, next.username)
                .putString(KEY_ROLE, next.role.name())
                .apply();
        state.setValue(next);
    }

    public void logout() {
        preferences.edit().clear().apply();
        state.setValue(AuthState.guest());
    }

    private AuthState readFromPrefs() {
        boolean loggedIn = preferences.getBoolean(KEY_LOGGED_IN, false);
        if (!loggedIn) {
            return AuthState.guest();
        }
        String username = preferences.getString(KEY_USERNAME, "用户");
        AuthRole role = AuthRole.fromName(preferences.getString(KEY_ROLE, AuthRole.USER.name()));
        return new AuthState(true, username == null ? "用户" : username, role);
    }
}
