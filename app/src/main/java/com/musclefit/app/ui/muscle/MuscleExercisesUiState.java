package com.musclefit.app.ui.muscle;

import java.util.Collections;
import java.util.List;

public class MuscleExercisesUiState {
    public final boolean loading;
    public final String areaName;
    public final String bodyView;
    public final List<MuscleExerciseItem> exercises;
    public final String error;

    private MuscleExercisesUiState(
            boolean loading,
            String areaName,
            String bodyView,
            List<MuscleExerciseItem> exercises,
            String error
    ) {
        this.loading = loading;
        this.areaName = areaName == null ? "" : areaName;
        this.bodyView = bodyView == null ? "front" : bodyView;
        this.exercises = exercises == null ? Collections.emptyList() : exercises;
        this.error = error;
    }

    public static MuscleExercisesUiState loading() {
        return new MuscleExercisesUiState(true, "", "front", Collections.emptyList(), null);
    }

    public static MuscleExercisesUiState success(String areaName, String bodyView, List<MuscleExerciseItem> exercises) {
        return new MuscleExercisesUiState(false, areaName, bodyView, exercises, null);
    }

    public static MuscleExercisesUiState error(String errorMessage) {
        return new MuscleExercisesUiState(false, "", "front", Collections.emptyList(), errorMessage);
    }
}
