package com.musclefit.app.ui.exercise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.musclefit.app.R;
import com.musclefit.app.data.model.ExerciseCard;
import com.musclefit.app.databinding.ItemExerciseCardBinding;
import com.musclefit.app.util.CategoryUtils;
import com.musclefit.app.util.IntensityUiUtils;

public class ExerciseAdapter extends ListAdapter<ExerciseCard, ExerciseAdapter.ExerciseViewHolder> {
    public interface Listener {
        void onCardClick(long exerciseId);

        void onLikeClick(long exerciseId);

        void onFavoriteClick(long exerciseId);

        void onImageClick(long exerciseId);
    }

    private final Listener listener;

    public ExerciseAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ExerciseCard> DIFF = new DiffUtil.ItemCallback<ExerciseCard>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExerciseCard oldItem, @NonNull ExerciseCard newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExerciseCard oldItem, @NonNull ExerciseCard newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseCardBinding binding = ItemExerciseCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ExerciseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final ItemExerciseCardBinding binding;

        ExerciseViewHolder(ItemExerciseCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ExerciseCard card) {
            binding.tvName.setText(card.name);
            binding.chipCategory.setText(CategoryUtils.displayCategory(itemView.getContext(), card.trainingCategory));
            binding.chipMovement.setText(CategoryUtils.displayMovement(itemView.getContext(), card.movementType));
            binding.tvIntensity.setText(IntensityUiUtils.levelText(card.maxIntensityLevel));
            int imageRes = ExerciseImageResolver.resolveCardImage(card);
            binding.ivCardImage.setImageResource(imageRes);
            binding.viewIntensityOverlay.setVisibility(View.GONE);

            binding.btnLike.setEnabled(true);
            binding.btnFavorite.setEnabled(true);

            binding.btnLike.setText(itemView.getContext().getString(
                    card.liked ? R.string.btn_like_on : R.string.btn_like_off,
                    card.likeCount
            ));
            binding.btnFavorite.setText(itemView.getContext().getString(
                    card.favorited ? R.string.btn_favorite_on : R.string.btn_favorite_off,
                    card.favoriteCount
            ));

            int active = ContextCompat.getColor(itemView.getContext(), R.color.mf_primary);
            int inactive = ContextCompat.getColor(itemView.getContext(), R.color.mf_muted);
            binding.btnLike.setTextColor(card.liked ? active : inactive);
            binding.btnFavorite.setTextColor(card.favorited ? active : inactive);

            binding.cardRoot.setOnClickListener(v -> listener.onCardClick(card.id));
            binding.layoutImageClickArea.setOnClickListener(v -> listener.onImageClick(card.id));
            binding.btnLike.setOnClickListener(v -> {
                if (!v.isEnabled()) {
                    return;
                }
                lockShort(v);
                listener.onLikeClick(card.id);
            });
            binding.btnFavorite.setOnClickListener(v -> {
                if (!v.isEnabled()) {
                    return;
                }
                lockShort(v);
                listener.onFavoriteClick(card.id);
            });
        }

        private void lockShort(View view) {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 600L);
        }
    }
}
