package com.musclefit.app.ui.exercise;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.musclefit.app.R;
import com.musclefit.app.databinding.ActivityImagePreviewBinding;

import coil.Coil;
import coil.request.ImageRequest;

public class ImagePreviewActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_RES = "image_res";
    public static final String EXTRA_IMAGE_URI = "image_uri";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityImagePreviewBinding binding = ActivityImagePreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int imageRes = getIntent().getIntExtra(EXTRA_IMAGE_RES, 0);
        String imageUri = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (imageRes == 0 && TextUtils.isEmpty(imageUri)) {
            finish();
            return;
        }

        if (imageRes != 0) {
            binding.photoPreview.setImageResource(imageRes);
        } else {
            ImageRequest request = new ImageRequest.Builder(this)
                    .data(imageUri)
                    .target(binding.photoPreview)
                    .crossfade(true)
                    .build();
            Coil.imageLoader(this).enqueue(request);
        }
        binding.btnClosePreview.setOnClickListener(v -> finish());
    }
}
