package com.musclefit.app.ui.forum;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musclefit.app.R;
import com.musclefit.app.databinding.ItemForumPostImageBinding;

import java.util.ArrayList;
import java.util.List;

public class ForumPostImageAdapter extends RecyclerView.Adapter<ForumPostImageAdapter.ImageHolder> {
    public interface Listener {
        void onImageClick(String uri);
    }

    private final Listener listener;
    private final List<String> items = new ArrayList<>();

    public ForumPostImageAdapter(Listener listener) {
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
        ItemForumPostImageBinding binding = ItemForumPostImageBinding.inflate(
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
        private final ItemForumPostImageBinding binding;
        private final Listener listener;

        ImageHolder(ItemForumPostImageBinding binding, Listener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(String uri) {
            ForumImageLoader.load(binding.ivForumPostImage, uri, R.drawable.bg_forum_cover_2);
            binding.ivForumPostImage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(uri);
                }
            });
        }
    }
}
