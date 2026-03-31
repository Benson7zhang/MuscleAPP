package com.musclefit.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthState;
import com.musclefit.app.databinding.ActivityFavoritesBinding;
import com.musclefit.app.repo.ToggleResult;
import com.musclefit.app.ui.exercise.ExerciseAdapter;
import com.musclefit.app.ui.exercise.ExerciseDetailActivity;
import com.musclefit.app.ui.exercise.ExerciseListViewModel;
import com.musclefit.app.util.CategoryUtils;
import com.musclefit.app.util.SimpleTextWatcher;

import java.util.HashMap;
import java.util.Map;

public class FavoritesActivity extends AppCompatActivity {
    private ActivityFavoritesBinding binding;
    private FavoritesViewModel viewModel;
    private ExerciseAdapter adapter;
    private AuthManager authManager;
    private final Map<String, Integer> categoryChipIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
        authManager = AuthManager.getInstance(this);

        setupToolbar();
        setupRecycler();
        setupFilters();
        observeData();

        authManager.observe().observe(this, this::renderAuthState);
        renderAuthState(authManager.getCurrent());
    }

    private void setupToolbar() {
        binding.toolbarFavorites.setTitle(R.string.my_favorites_title);
        binding.toolbarFavorites.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        adapter = new ExerciseAdapter(new ExerciseAdapter.Listener() {
            @Override
            public void onCardClick(long exerciseId) {
                Intent intent = new Intent(FavoritesActivity.this, ExerciseDetailActivity.class);
                intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exerciseId);
                startActivity(intent);
            }

            @Override
            public void onLikeClick(long exerciseId) {
                if (!requireLogin()) {
                    return;
                }
                viewModel.toggleLike(exerciseId, FavoritesActivity.this::showErrorIfNeeded);
            }

            @Override
            public void onFavoriteClick(long exerciseId) {
                if (!requireLogin()) {
                    return;
                }
                viewModel.toggleFavorite(exerciseId, FavoritesActivity.this::showErrorIfNeeded);
            }

            @Override
            public void onImageClick(long exerciseId) {
                Intent intent = new Intent(FavoritesActivity.this, ExerciseDetailActivity.class);
                intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exerciseId);
                startActivity(intent);
            }
        });
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavorites.setAdapter(adapter);
    }

    private void setupFilters() {
        for (String category : CategoryUtils.allCategories()) {
            Chip chip = new Chip(this);
            int chipId = View.generateViewId();
            chip.setId(chipId);
            chip.setTag(category);
            chip.setText(CategoryUtils.displayCategory(this, category));
            chip.setCheckable(true);
            chip.setClickable(true);
            categoryChipIds.put(category, chipId);
            binding.chipGroupFavoriteCategory.addView(chip);
        }
        Integer allId = categoryChipIds.get(CategoryUtils.ALL);
        if (allId != null) {
            binding.chipGroupFavoriteCategory.check(allId);
        }

        binding.chipGroupFavoriteCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == View.NO_ID) {
                viewModel.setCategory(CategoryUtils.ALL);
                return;
            }
            View checked = group.findViewById(checkedId);
            if (checked != null && checked.getTag() instanceof String) {
                viewModel.setCategory((String) checked.getTag());
            }
        });

        binding.etFavoriteSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(android.text.Editable s) {
                viewModel.setKeyword(s == null ? "" : s.toString());
            }
        });
    }

    private void observeData() {
        viewModel.getFavorites().observe(this, cards -> adapter.submitList(cards));
    }

    private void renderAuthState(@NonNull AuthState state) {
        if (!state.loggedIn) {
            Toast.makeText(this, R.string.login_required_action, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvFavoritesScopeHint.setText(getString(R.string.favorites_scope_hint_with_user, state.username));
        viewModel.refreshForAccountScope();
    }

    private boolean requireLogin() {
        if (authManager.isLoggedIn()) {
            return true;
        }
        Toast.makeText(this, R.string.login_required_action, Toast.LENGTH_SHORT).show();
        finish();
        return false;
    }

    private void showErrorIfNeeded(ToggleResult result) {
        if (result == ToggleResult.SUCCESS) {
            return;
        }
        Toast.makeText(this, ExerciseListViewModel.errorMessage(this, result), Toast.LENGTH_SHORT).show();
    }
}
