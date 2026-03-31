package com.musclefit.app.ui.exercise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.data.model.ExerciseCard;
import com.musclefit.app.data.model.ExerciseIntensityNote;
import com.musclefit.app.databinding.ActivityExerciseDetailBinding;
import com.musclefit.app.repo.ToggleResult;
import com.musclefit.app.util.IntensityUiUtils;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDetailActivity extends AppCompatActivity {
    public static final String EXTRA_EXERCISE_ID = "exercise_id";

    private ActivityExerciseDetailBinding binding;
    private ExerciseDetailViewModel viewModel;
    private AuthManager authManager;
    private long exerciseId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExerciseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        exerciseId = getIntent().getLongExtra(EXTRA_EXERCISE_ID, -1L);
        if (exerciseId <= 0L) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ExerciseDetailViewModel.class);
        viewModel.setExerciseId(exerciseId);
        authManager = AuthManager.getInstance(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnDetailLike.setOnClickListener(v -> {
            if (!authManager.isLoggedIn()) {
                Toast.makeText(this, R.string.login_required_action, Toast.LENGTH_SHORT).show();
                return;
            }
            lockShort(v);
            viewModel.toggleLike(exerciseId, this::handleToggleResult);
        });
        binding.btnDetailFavorite.setOnClickListener(v -> {
            if (!authManager.isLoggedIn()) {
                Toast.makeText(this, R.string.login_required_action, Toast.LENGTH_SHORT).show();
                return;
            }
            lockShort(v);
            viewModel.toggleFavorite(exerciseId, this::handleToggleResult);
        });

        observeUi();
    }

    private void observeUi() {
        viewModel.getExercise().observe(this, this::renderExercise);
        viewModel.getIntensityNotes().observe(this, this::renderIntensityNotes);
    }

    private void renderExercise(ExerciseCard card) {
        if (card == null) {
            return;
        }
        binding.tvDetailName.setText(card.name);
        binding.btnDetailLike.setText(getString(
                card.liked ? R.string.btn_like_on : R.string.btn_like_off,
                card.likeCount
        ));
        binding.btnDetailFavorite.setText(getString(
                card.favorited ? R.string.btn_favorite_on : R.string.btn_favorite_off,
                card.favoriteCount
        ));

        int active = ContextCompat.getColor(this, R.color.mf_primary);
        int inactive = ContextCompat.getColor(this, R.color.mf_muted);
        binding.btnDetailLike.setTextColor(card.liked ? active : inactive);
        binding.btnDetailFavorite.setTextColor(card.favorited ? active : inactive);

        binding.tvDetailDescription.setText(getString(R.string.detail_description_format, card.description));
        int detailImageRes = ExerciseImageResolver.resolveDetailImage(card);
        binding.ivDetailTutorial.setImageResource(detailImageRes);
        binding.layoutDetailImageArea.setOnClickListener(v -> openImagePreview(detailImageRes));

        StringBuilder conditions = new StringBuilder(getString(R.string.detail_conditions_title));
        if (card.gripType != null && !card.gripType.trim().isEmpty()) {
            conditions.append(getString(R.string.detail_conditions_grip, card.gripType));
        }
        if (card.categoryHint != null && !card.categoryHint.trim().isEmpty()) {
            conditions.append(getString(R.string.detail_conditions_category, card.categoryHint));
        }
        if (conditions.toString().equals(getString(R.string.detail_conditions_title))) {
            conditions.append(getString(R.string.detail_conditions_none));
        }
        binding.tvDetailConditions.setText(conditions.toString());
        binding.tvDetailCaution.setText(getString(R.string.detail_caution_format, card.cautionNotes));
    }

    private void renderIntensityNotes(List<ExerciseIntensityNote> notes) {
        if (notes == null || notes.isEmpty()) {
            binding.tvDetailIntensityNotes.setText(getString(R.string.detail_intensity_empty));
            return;
        }

        List<String> rows = new ArrayList<>();
        for (ExerciseIntensityNote note : notes) {
            String line = getString(
                    R.string.detail_intensity_line,
                    note.muscleName,
                    IntensityUiUtils.levelText(note.intensityLevel),
                    note.role
            );
            rows.add(line);
        }
        binding.tvDetailIntensityNotes.setText(getString(
                R.string.detail_intensity_with_rows,
                IntensityUiUtils.formatIntensityNotes(rows)
        ));
    }

    private void lockShort(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 600L);
    }

    private void handleToggleResult(ToggleResult result) {
        if (result == ToggleResult.SUCCESS) {
            return;
        }
        Toast.makeText(this, ExerciseListViewModel.errorMessage(this, result), Toast.LENGTH_SHORT).show();
    }

    private void openImagePreview(int imageRes) {
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_RES, imageRes);
        startActivity(intent);
    }
}
