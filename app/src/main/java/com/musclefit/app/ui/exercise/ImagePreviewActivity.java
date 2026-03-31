package com.musclefit.app.ui.exercise;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.musclefit.app.R;
import com.musclefit.app.databinding.ActivityImagePreviewBinding;

public class ImagePreviewActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_RES = "image_res";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityImagePreviewBinding binding = ActivityImagePreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int imageRes = getIntent().getIntExtra(EXTRA_IMAGE_RES, 0);
        if (imageRes == 0) {
            finish();
            return;
        }

        binding.photoPreview.setImageResource(imageRes);
        binding.btnClosePreview.setOnClickListener(v -> finish());
    }
}
