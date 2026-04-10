package com.musclefit.app.ui.forum;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musclefit.app.R;
import com.musclefit.app.databinding.ItemForumComposeImageBinding;

import java.util.ArrayList;
import java.util.List;

public class ForumComposeImageAdapter extends RecyclerView.Adapter<ForumComposeImageAdapter.ImageHolder> {
    public interface Listener {
        void onRemoveClick(String uri);

        void onImageClick(String uri);
    }

    private final Listener listener;
    private final List<String> items = new ArrayList<>();

    public ForumComposeImageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<String> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemForumComposeImageBinding binding = ItemForumComposeImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ImageHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ImageHolder extends RecyclerView.ViewHolder {
        private final ItemForumComposeImageBinding binding;
        private final Listener listener;

        ImageHolder(ItemForumComposeImageBinding binding, Listener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(String uri) {
            ForumImageLoader.load(binding.ivForumComposeImage, uri, R.drawable.bg_forum_cover_1);
            binding.ivForumComposeImage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(uri);
                }
            });
            binding.btnForumComposeImageRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(uri);
                }
            });
        }
    }
}
