package com.musclefit.app.theme;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    private static final String PREF = "musclefit_theme";
    private static final String KEY_DARK = "dark";

    private ThemeManager() {
    }

    public static void applySavedMode(Context context) {
        boolean dark = isDarkMode(context);
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_DARK, false);
    }

    public static void setDarkMode(Context context, boolean dark) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_DARK, dark).apply();
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
