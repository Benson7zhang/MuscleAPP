package com.musclefit.app.ui.forum;

import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import coil.Coil;
import coil.request.ImageRequest;

public final class ForumImageLoader {
    private ForumImageLoader() {
    }

    public static void load(ImageView target, String data, @DrawableRes int fallbackRes) {
        if (target == null) {
            return;
        }
        ImageRequest request = new ImageRequest.Builder(target.getContext())
                .data(data)
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .crossfade(true)
                .target(target)
                .build();
        Coil.imageLoader(target.getContext()).enqueue(request);
    }
}
