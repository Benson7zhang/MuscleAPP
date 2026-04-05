package com.musclefit.app.ui.coach;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.musclefit.app.R;
import com.musclefit.app.databinding.ItemCoachBinding;

import java.util.List;

public class CoachAdapter extends ListAdapter<CoachItem, CoachAdapter.CoachHolder> {
    public interface Listener {
        void onBook(CoachItem item);
    }

    private final Listener listener;
    private static final DiffUtil.ItemCallback<CoachItem> DIFF = new DiffUtil.ItemCallback<CoachItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CoachItem oldItem, @NonNull CoachItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CoachItem oldItem, @NonNull CoachItem newItem) {
            return oldItem.name.equals(newItem.name)
                    && oldItem.specialty.equals(newItem.specialty)
                    && oldItem.courseType.equals(newItem.courseType);
        }
    };

    public CoachAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    public void submitList(List<CoachItem> next) {
        super.submitList(next);
    }

    @NonNull
    @Override
    public CoachHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCoachBinding binding = ItemCoachBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CoachHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CoachHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class CoachHolder extends RecyclerView.ViewHolder {
        private final ItemCoachBinding binding;

        CoachHolder(ItemCoachBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CoachItem item) {
            binding.tvCoachName.setText(item.name);
            binding.tvCoachSpecialty.setText(itemView.getContext().getString(R.string.coach_specialty, item.specialty));
            binding.tvCoachCourseType.setText(itemView.getContext().getString(R.string.coach_course_type, item.courseType));
            binding.btnBookCoach.setEnabled(true);
            binding.btnBookCoach.setOnClickListener(v -> {
                if (!v.isEnabled()) {
                    return;
                }
                lockShort(v);
                listener.onBook(item);
            });
        }

        private void lockShort(View view) {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 600L);
        }
    }
}
