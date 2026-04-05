package com.musclefit.app.ui.exercise;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.musclefit.app.repo.ExerciseRepository;
import com.musclefit.app.repo.ToggleResult;
import com.musclefit.app.data.model.ExerciseCard;
import com.musclefit.app.R;
import com.musclefit.app.util.CategoryUtils;

import java.util.List;

public class ExerciseListViewModel extends AndroidViewModel {
    private final ExerciseRepository repository;

    private final MutableLiveData<FilterState> filter = new MutableLiveData<>(new FilterState("", CategoryUtils.ALL));

    private final LiveData<List<ExerciseCard>> exercises;

    public ExerciseListViewModel(@NonNull Application application) {
        super(application);
        repository = ExerciseRepository.getInstance(application);
        exercises = Transformations.switchMap(filter, state -> repository.observeExercises(state.keyword, state.category));
    }

    public LiveData<List<ExerciseCard>> getExercises() {
        return exercises;
    }

    public void setKeyword(String keyword) {
        FilterState current = filter.getValue();
        if (current == null) {
            current = new FilterState("", CategoryUtils.ALL);
        }
        if (keyword == null) {
            keyword = "";
        }
        filter.setValue(new FilterState(keyword, current.category));
    }

    public void setCategory(String category) {
        FilterState current = filter.getValue();
        if (current == null) {
            current = new FilterState("", CategoryUtils.ALL);
        }
        filter.setValue(new FilterState(current.keyword, category));
    }

    public void applyFilter(String keyword, String category) {
        filter.setValue(new FilterState(keyword == null ? "" : keyword, category == null ? CategoryUtils.ALL : category));
    }

    public void toggleLike(long exerciseId, ExerciseRepository.ToggleCallback callback) {
        repository.toggleLike(exerciseId, callback);
    }

    public void toggleFavorite(long exerciseId, ExerciseRepository.ToggleCallback callback) {
        repository.toggleFavorite(exerciseId, callback);
    }

    public void refreshForAccountScope() {
        FilterState current = filter.getValue();
        if (current == null) {
            current = new FilterState("", CategoryUtils.ALL);
        }
        filter.setValue(new FilterState(current.keyword, current.category));
    }

    public static String errorMessage(Context context, ToggleResult result) {
        if (result == ToggleResult.TOO_FAST) {
            return context.getString(R.string.action_too_fast);
        }
        if (result == ToggleResult.BUSY) {
            return context.getString(R.string.action_processing);
        }
        if (result == ToggleResult.ERROR) {
            return context.getString(R.string.action_failed);
        }
        return context.getString(R.string.exercise_not_found);
    }

    private static final class FilterState {
        private final String keyword;
        private final String category;

        private FilterState(String keyword, String category) {
            this.keyword = keyword;
            this.category = category;
        }
    }
}
