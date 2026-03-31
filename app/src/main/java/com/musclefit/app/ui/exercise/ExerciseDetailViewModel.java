package com.musclefit.app.ui.exercise;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.musclefit.app.data.model.ExerciseCard;
import com.musclefit.app.data.model.ExerciseIntensityNote;
import com.musclefit.app.repo.ExerciseRepository;

import java.util.List;

public class ExerciseDetailViewModel extends AndroidViewModel {
    private final ExerciseRepository repository;
    private final MutableLiveData<Long> exerciseId = new MutableLiveData<>();

    private final LiveData<ExerciseCard> exercise;
    private final LiveData<List<ExerciseIntensityNote>> notes;

    public ExerciseDetailViewModel(@NonNull Application application) {
        super(application);
        repository = ExerciseRepository.getInstance(application);
        exercise = Transformations.switchMap(exerciseId, repository::observeExerciseById);
        notes = Transformations.switchMap(exerciseId, repository::observeIntensityNotes);
    }

    public void setExerciseId(long id) {
        exerciseId.setValue(id);
    }

    public LiveData<ExerciseCard> getExercise() {
        return exercise;
    }

    public LiveData<List<ExerciseIntensityNote>> getIntensityNotes() {
        return notes;
    }

    public void toggleLike(long id, ExerciseRepository.ToggleCallback callback) {
        repository.toggleLike(id, callback);
    }

    public void toggleFavorite(long id, ExerciseRepository.ToggleCallback callback) {
        repository.toggleFavorite(id, callback);
    }
}
