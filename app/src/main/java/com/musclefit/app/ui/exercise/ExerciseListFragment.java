package com.musclefit.app.ui.exercise;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.databinding.FragmentExerciseListBinding;
import com.musclefit.app.repo.ToggleResult;
import com.musclefit.app.util.CategoryUtils;
import com.musclefit.app.util.SimpleTextWatcher;

import java.util.HashMap;
import java.util.Map;

public class ExerciseListFragment extends Fragment {
    private FragmentExerciseListBinding binding;
    private ExerciseListViewModel viewModel;
    private ExerciseAdapter adapter;
    private AuthManager authManager;
    private final Map<String, Integer> categoryChipIds = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ExerciseListViewModel.class);
        authManager = AuthManager.getInstance(requireContext());

        setupRecycler();
        setupFilters();
        observeData();
        listenFilterFromHome();
        authManager.observe().observe(getViewLifecycleOwner(), state -> viewModel.refreshForAccountScope());
    }

    private void setupRecycler() {
        adapter = new ExerciseAdapter(new ExerciseAdapter.Listener() {
            @Override
            public void onCardClick(long exerciseId) {
                Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
                intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exerciseId);
                startActivity(intent);
            }

            @Override
            public void onLikeClick(long exerciseId) {
                if (!requireLoginForInteraction()) {
                    return;
                }
                viewModel.toggleLike(exerciseId, result -> showErrorIfNeeded(result));
            }

            @Override
            public void onFavoriteClick(long exerciseId) {
                if (!requireLoginForInteraction()) {
                    return;
                }
                viewModel.toggleFavorite(exerciseId, result -> showErrorIfNeeded(result));
            }

            @Override
            public void onImageClick(long exerciseId) {
                Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
                intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exerciseId);
                startActivity(intent);
            }
        });
        binding.rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExercises.setAdapter(adapter);
    }

    private void setupFilters() {
        for (String category : CategoryUtils.allCategories()) {
            Chip chip = new Chip(requireContext());
            int chipId = View.generateViewId();
            chip.setId(chipId);
            chip.setTag(category);
            chip.setText(CategoryUtils.displayCategory(requireContext(), category));
            chip.setCheckable(true);
            chip.setClickable(true);
            categoryChipIds.put(category, chipId);
            binding.chipGroupCategory.addView(chip);
        }
        Integer allId = categoryChipIds.get(CategoryUtils.ALL);
        if (allId != null) {
            binding.chipGroupCategory.check(allId);
        }

        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
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

        binding.etSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(android.text.Editable s) {
                viewModel.setKeyword(s == null ? "" : s.toString());
            }
        });
    }

    private void observeData() {
        viewModel.getExercises().observe(getViewLifecycleOwner(), cards -> adapter.submitList(cards));
    }

    private void listenFilterFromHome() {
        getParentFragmentManager().setFragmentResultListener("exercise_filter", getViewLifecycleOwner(), (requestKey, bundle) -> {
            String keyword = bundle.getString("keyword", "");
            String category = bundle.getString("category", CategoryUtils.ALL);
            String muscleGroup = bundle.getString("muscleGroup", "");
            String trainingCategory = bundle.getString("trainingCategory", "");

            if ((keyword == null || keyword.trim().isEmpty()) && muscleGroup != null && !muscleGroup.trim().isEmpty()) {
                keyword = keywordFromMuscleGroup(muscleGroup.trim());
            }
            if ((category == null || category.trim().isEmpty() || CategoryUtils.ALL.equals(category))
                    && trainingCategory != null
                    && !trainingCategory.trim().isEmpty()) {
                category = categoryCodeFromLabel(trainingCategory.trim());
            }

            binding.etSearch.setText(keyword);
            Integer chipId = categoryChipIds.get(category);
            if (chipId != null) {
                binding.chipGroupCategory.check(chipId);
            }
            viewModel.applyFilter(keyword, category);
        });
    }

    private String keywordFromMuscleGroup(String muscleGroup) {
        if ("chest".equals(muscleGroup)) {
            return "胸";
        }
        if ("shoulder".equals(muscleGroup)) {
            return "肩";
        }
        if ("back".equals(muscleGroup)) {
            return "背";
        }
        if ("abs".equals(muscleGroup)) {
            return "腹";
        }
        if ("thigh".equals(muscleGroup)) {
            return "腿";
        }
        if ("calf".equals(muscleGroup)) {
            return "小腿";
        }
        return "";
    }

    private String categoryCodeFromLabel(String label) {
        if (getString(R.string.category_all).equals(label)) {
            return CategoryUtils.ALL;
        }
        if (getString(R.string.category_bodyweight).equals(label)) {
            return CategoryUtils.BODYWEIGHT;
        }
        if (getString(R.string.category_dumbbell).equals(label)) {
            return CategoryUtils.DUMBBELL;
        }
        if (getString(R.string.category_barbell).equals(label)) {
            return CategoryUtils.BARBELL;
        }
        if (getString(R.string.category_machine).equals(label)) {
            return CategoryUtils.MACHINE;
        }
        return CategoryUtils.ALL;
    }

    private void showErrorIfNeeded(ToggleResult result) {
        if (result == ToggleResult.SUCCESS) {
            return;
        }
        Toast.makeText(requireContext(), ExerciseListViewModel.errorMessage(requireContext(), result), Toast.LENGTH_SHORT).show();
    }

    private boolean requireLoginForInteraction() {
        if (authManager.isLoggedIn()) {
            return true;
        }
        Toast.makeText(requireContext(), R.string.login_required_action, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
