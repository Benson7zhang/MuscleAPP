package com.musclefit.app;

import android.app.Application;

import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.repo.ExerciseRepository;
import com.musclefit.app.theme.ThemeManager;

public class ZhiLianEngineApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedMode(this);
        AuthManager.getInstance(this);
        ExerciseRepository.getInstance(this);
    }
}
