package com.musclefit.app.ui.muscle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MuscleExercisesViewModel extends AndroidViewModel {
    private final MuscleExercisesRepository repository;
    private final ExecutorService executor;
    private final MutableLiveData<MuscleExercisesUiState> state = new MutableLiveData<>(MuscleExercisesUiState.loading());

    public MuscleExercisesViewModel(@NonNull Application application) {
        super(application);
        repository = new MuscleExercisesRepository(application);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<MuscleExercisesUiState> getState() {
        return state;
    }

    public void load(String muscleKey, String fallbackSide) {
        state.setValue(MuscleExercisesUiState.loading());
        executor.execute(() -> {
            try {
                MuscleExercisesRepository.MuscleAreaData data = repository.loadArea(muscleKey, fallbackSide);
                state.postValue(MuscleExercisesUiState.success(data.areaName, data.view, data.exercises));
            } catch (Exception e) {
                state.postValue(MuscleExercisesUiState.error("load_failed"));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}
