package com.musclefit.app.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.musclefit.app.data.model.ExerciseCard;
import com.musclefit.app.repo.ExerciseRepository;
import com.musclefit.app.util.CategoryUtils;

import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {
    private final ExerciseRepository repository;

    private final MutableLiveData<FilterState> filter = new MutableLiveData<>(new FilterState("", CategoryUtils.ALL));

    private final LiveData<List<ExerciseCard>> favorites;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        repository = ExerciseRepository.getInstance(application);
        favorites = Transformations.switchMap(filter, state -> repository.observeFavoriteExercises(state.keyword, state.category));
    }

    public LiveData<List<ExerciseCard>> getFavorites() {
        return favorites;
    }

    public void setKeyword(String keyword) {
        FilterState current = filter.getValue();
        if (current == null) {
            current = new FilterState("", CategoryUtils.ALL);
        }
        filter.setValue(new FilterState(keyword == null ? "" : keyword, current.category));
    }

    public void setCategory(String category) {
        FilterState current = filter.getValue();
        if (current == null) {
            current = new FilterState("", CategoryUtils.ALL);
        }
        filter.setValue(new FilterState(current.keyword, category));
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

    private static final class FilterState {
        private final String keyword;
        private final String category;

        private FilterState(String keyword, String category) {
            this.keyword = keyword;
            this.category = category;
        }
    }
}
