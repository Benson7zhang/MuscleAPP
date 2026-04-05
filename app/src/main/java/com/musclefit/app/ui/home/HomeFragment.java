package com.musclefit.app.ui.home;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.musclefit.app.R;
import com.musclefit.app.databinding.FragmentHomeBinding;
import com.musclefit.app.ui.MainActivity;
import com.musclefit.app.util.CategoryUtils;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final long MUSCLE_JUMP_DELAY_MS = 120L;

    private FragmentHomeBinding binding;
    private final Map<Integer, String> categoryByRadioId = new HashMap<>();
    private final RectF mappedImageRect = new RectF();

    private String selectedCategory = CategoryUtils.ALL;
    private boolean male = true;
    private int currentSide = BodyMapView.SIDE_FRONT;
    private String selectedMuscleKey;
    private Runnable pendingOpenExercise;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCategoryBottomSheet();
        setupGenderAndBodyMap();
    }

    private void setupCategoryBottomSheet() {
        categoryByRadioId.put(R.id.rb_sheet_home_all, CategoryUtils.ALL);
        categoryByRadioId.put(R.id.rb_sheet_home_bodyweight, CategoryUtils.BODYWEIGHT);
        categoryByRadioId.put(R.id.rb_sheet_home_dumbbell, CategoryUtils.DUMBBELL);
        categoryByRadioId.put(R.id.rb_sheet_home_barbell, CategoryUtils.BARBELL);
        categoryByRadioId.put(R.id.rb_sheet_home_machine, CategoryUtils.MACHINE);

        updateSelectedCategoryText();
        binding.btnOpenHomeDrawer.setOnClickListener(v -> showCategoryBottomSheet());
    }

    private void showCategoryBottomSheet() {
        if (!isAdded()) {
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_home_category_sheet, null, false);
        RadioGroup categoryGroup = sheetView.findViewById(R.id.rg_sheet_home_category);

        int checkedId = categoryRadioIdByCategory(selectedCategory);
        if (checkedId != View.NO_ID) {
            categoryGroup.check(checkedId);
        }

        categoryGroup.setOnCheckedChangeListener((group, checkedRadioId) -> {
            String nextCategory = categoryByRadioId.get(checkedRadioId);
            if (nextCategory == null) {
                return;
            }
            selectedCategory = nextCategory;
            updateSelectedCategoryText();
            dialog.dismiss();
        });

        dialog.setContentView(sheetView);
        dialog.show();
    }

    private int categoryRadioIdByCategory(String category) {
        for (Map.Entry<Integer, String> entry : categoryByRadioId.entrySet()) {
            if (entry.getValue().equals(category)) {
                return entry.getKey();
            }
        }
        return View.NO_ID;
    }

    private void setupGenderAndBodyMap() {
        binding.bodyMapOverlay.setDrawSilhouette(false);
        binding.bodyMapOverlay.setOnMuscleClickListener(this::onMuscleSelected);

        binding.bodyMapPanel.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                syncOverlayToImageBounds()
        );

        binding.btnGenderSwitch.setOnClickListener(v -> {
            male = !male;
            currentSide = BodyMapView.SIDE_FRONT;
            selectedMuscleKey = null;
            applyBodyMapState(false);
        });

        binding.btnToggleBodySide.setOnClickListener(v -> {
            currentSide = currentSide == BodyMapView.SIDE_FRONT
                    ? BodyMapView.SIDE_BACK
                    : BodyMapView.SIDE_FRONT;
            sanitizeSelectedMuscleForSide();
            applyBodyMapState(true);
        });

        applyBodyMapState(false);
    }

    private void applyBodyMapState(boolean animateSideSwitch) {
        sanitizeSelectedMuscleForSide();

        Runnable renderTask = () -> {
            if (binding == null) {
                return;
            }

            String titleGender = male
                    ? getString(R.string.home_body_map_title_male)
                    : getString(R.string.home_body_map_title_female);
            String sideLabel = currentSide == BodyMapView.SIDE_FRONT
                    ? getString(R.string.body_map_side_front)
                    : getString(R.string.body_map_side_back);

            binding.tvHomeBodyMapTitle.setText(getString(R.string.home_body_map_title_with_side, titleGender, sideLabel));
            binding.btnGenderSwitch.setText(getString(R.string.home_gender_switch_format, titleGender));

            int imageRes = resolveBodyMapImage();
            binding.ivBodyMapBackground.setImageResource(imageRes);
            binding.ivBodyMapBackground.setContentDescription(getString(R.string.body_map_content_description, titleGender, sideLabel));

            binding.bodyMapOverlay.setGender(male ? BodyMapView.GENDER_MALE : BodyMapView.GENDER_FEMALE);
            binding.bodyMapOverlay.setSide(currentSide);
            binding.bodyMapOverlay.setSelectedMuscleKey(selectedMuscleKey);
            binding.ivBodyMapBackground.post(this::syncOverlayToImageBounds);

            binding.btnToggleBodySide.setText(
                    currentSide == BodyMapView.SIDE_FRONT
                            ? R.string.body_map_switch_back
                            : R.string.body_map_switch_front
            );
        };

        if (!animateSideSwitch) {
            renderTask.run();
            binding.bodyMapPanel.setAlpha(1f);
            return;
        }

        binding.bodyMapPanel.animate().cancel();
        binding.bodyMapPanel.animate()
                .alpha(0f)
                .setDuration(120L)
                .withEndAction(() -> {
                    renderTask.run();
                    if (binding == null) {
                        return;
                    }
                    binding.bodyMapPanel.animate().alpha(1f).setDuration(180L).start();
                })
                .start();
    }

    private int resolveBodyMapImage() {
        if (male) {
            return currentSide == BodyMapView.SIDE_FRONT
                    ? R.drawable.body_map_front
                    : R.drawable.body_map_back;
        }
        return currentSide == BodyMapView.SIDE_FRONT
                ? R.drawable.body_map_front_female
                : R.drawable.body_map_back_female;
    }

    private void syncOverlayToImageBounds() {
        if (binding == null) {
            return;
        }

        Drawable drawable = binding.ivBodyMapBackground.getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            binding.bodyMapOverlay.setImageContentRect(null);
            return;
        }

        mappedImageRect.set(0f, 0f, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Matrix imageMatrix = binding.ivBodyMapBackground.getImageMatrix();
        imageMatrix.mapRect(mappedImageRect);

        float offsetX = binding.ivBodyMapBackground.getLeft() - binding.bodyMapOverlay.getLeft();
        float offsetY = binding.ivBodyMapBackground.getTop() - binding.bodyMapOverlay.getTop();
        mappedImageRect.offset(offsetX, offsetY);
        binding.bodyMapOverlay.setImageContentRect(mappedImageRect);
    }

    private void sanitizeSelectedMuscleForSide() {
        if (selectedMuscleKey == null) {
            return;
        }
        if (currentSide == BodyMapView.SIDE_FRONT) {
            if ("back".equals(selectedMuscleKey)) {
                selectedMuscleKey = null;
            }
            return;
        }
        if ("chest".equals(selectedMuscleKey) || "abs".equals(selectedMuscleKey)) {
            selectedMuscleKey = null;
        }
    }

    private void onMuscleSelected(String muscleKey) {
        selectedMuscleKey = muscleKey;
        binding.bodyMapOverlay.setSelectedMuscleKey(muscleKey);

        if (pendingOpenExercise != null) {
            binding.bodyMapPanel.removeCallbacks(pendingOpenExercise);
        }

        pendingOpenExercise = () -> {
            pendingOpenExercise = null;
            if (!isAdded() || binding == null) {
                return;
            }
            jumpToExerciseFromMuscle(muscleKey);
        };
        binding.bodyMapPanel.postDelayed(pendingOpenExercise, MUSCLE_JUMP_DELAY_MS);
    }

    private void jumpToExerciseFromMuscle(String muscleKey) {
        Bundle filter = new Bundle();
        filter.putString("muscleGroup", muscleKey);
        filter.putString("trainingCategory", CategoryUtils.displayCategory(requireContext(), selectedCategory));
        filter.putString("keyword", keywordByMuscle(muscleKey));
        filter.putString("category", selectedCategory);

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).openExerciseWithFilter(filter);
            return;
        }
        getParentFragmentManager().setFragmentResult("exercise_filter", filter);
    }

    private String keywordByMuscle(String muscleKey) {
        if ("chest".equals(muscleKey)) {
            return "胸";
        }
        if ("shoulder".equals(muscleKey)) {
            return "肩";
        }
        if ("back".equals(muscleKey)) {
            return "背";
        }
        if ("abs".equals(muscleKey)) {
            return "腹";
        }
        if ("thigh".equals(muscleKey)) {
            return "腿";
        }
        if ("calf".equals(muscleKey)) {
            return "小腿";
        }
        return "";
    }

    private void updateSelectedCategoryText() {
        binding.btnOpenHomeDrawer.setText(getString(
                R.string.home_category_button_format,
                CategoryUtils.displayCategory(requireContext(), selectedCategory)
        ));
    }

    @Override
    public void onDestroyView() {
        if (binding != null && pendingOpenExercise != null) {
            binding.bodyMapPanel.removeCallbacks(pendingOpenExercise);
        }
        pendingOpenExercise = null;
        super.onDestroyView();
        binding = null;
    }
}
