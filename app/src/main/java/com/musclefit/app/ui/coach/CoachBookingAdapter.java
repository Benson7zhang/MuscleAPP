package com.musclefit.app.ui.coach;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.musclefit.app.R;
import com.musclefit.app.data.db.CoachBookingEntity;
import com.musclefit.app.databinding.ItemBookingCardBinding;

import java.text.DateFormat;
import java.util.Date;

public class CoachBookingAdapter extends ListAdapter<CoachBookingEntity, CoachBookingAdapter.BookingHolder> {
    public enum Mode {
        READ_ONLY,
        COACH_PENDING,
        ADMIN_REVIEW
    }

    public interface ActionListener {
        void onPrimaryAction(long bookingId);

        void onSecondaryAction(long bookingId);
    }

    private static final DiffUtil.ItemCallback<CoachBookingEntity> DIFF = new DiffUtil.ItemCallback<CoachBookingEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull CoachBookingEntity oldItem, @NonNull CoachBookingEntity newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CoachBookingEntity oldItem, @NonNull CoachBookingEntity newItem) {
            return oldItem.id == newItem.id
                    && textEq(oldItem.userName, newItem.userName)
                    && textEq(oldItem.coachName, newItem.coachName)
                    && textEq(oldItem.coachSpecialty, newItem.coachSpecialty)
                    && textEq(oldItem.courseType, newItem.courseType)
                    && textEq(oldItem.status, newItem.status)
                    && oldItem.updatedAt == newItem.updatedAt;
        }

        private boolean textEq(String a, String b) {
            if (a == null) {
                return b == null;
            }
            return a.equals(b);
        }
    };

    private final Mode mode;
    private final ActionListener actionListener;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public CoachBookingAdapter(Mode mode, ActionListener actionListener) {
        super(DIFF);
        this.mode = mode;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public BookingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingCardBinding binding = ItemBookingCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class BookingHolder extends RecyclerView.ViewHolder {
        private final ItemBookingCardBinding binding;

        BookingHolder(ItemBookingCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CoachBookingEntity booking) {
            binding.tvBookingTitle.setText(itemView.getContext().getString(
                    R.string.booking_title,
                    safe(booking.userName),
                    safe(booking.coachName)
            ));
            binding.tvBookingSubtitle.setText(itemView.getContext().getString(
                    R.string.booking_subtitle,
                    safe(booking.coachSpecialty),
                    safe(booking.courseType)
            ));
            binding.tvBookingStatus.setText(itemView.getContext().getString(
                    R.string.booking_status_with_time,
                    CoachBookingStatus.label(itemView.getContext(), booking.status),
                    dateFormat.format(new Date(booking.updatedAt))
            ));

            bindActions(booking);
        }

        private void bindActions(CoachBookingEntity booking) {
            if (mode == Mode.READ_ONLY || actionListener == null) {
                binding.layoutBookingActions.setVisibility(View.GONE);
                return;
            }

            binding.layoutBookingActions.setVisibility(View.VISIBLE);
            binding.btnBookingPrimary.setEnabled(true);
            binding.btnBookingSecondary.setEnabled(true);

            if (mode == Mode.COACH_PENDING) {
                binding.btnBookingPrimary.setText(R.string.booking_action_accept);
                binding.btnBookingSecondary.setText(R.string.booking_action_reject);
            } else {
                binding.btnBookingPrimary.setText(R.string.booking_action_admin_approve);
                binding.btnBookingSecondary.setText(R.string.booking_action_admin_reject);
            }

            binding.btnBookingPrimary.setOnClickListener(v -> {
                if (!v.isEnabled()) {
                    return;
                }
                lockShort(v, binding.btnBookingSecondary);
                actionListener.onPrimaryAction(booking.id);
            });
            binding.btnBookingSecondary.setOnClickListener(v -> {
                if (!v.isEnabled()) {
                    return;
                }
                lockShort(v, binding.btnBookingPrimary);
                actionListener.onSecondaryAction(booking.id);
            });
        }

        private String safe(String text) {
            return text == null ? "" : text;
        }

        private void lockShort(View current, View sibling) {
            current.setEnabled(false);
            sibling.setEnabled(false);
            current.postDelayed(() -> {
                current.setEnabled(true);
                sibling.setEnabled(true);
            }, 600L);
        }
    }
}
