package com.musclefit.app.ui.coach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthRole;
import com.musclefit.app.auth.AuthState;
import com.musclefit.app.auth.RolePolicy;
import com.musclefit.app.data.db.CoachBookingEntity;
import com.musclefit.app.databinding.FragmentCoachBinding;
import com.musclefit.app.repo.ToggleResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoachFragment extends Fragment {
    private FragmentCoachBinding binding;
    private CoachViewModel viewModel;
    private AuthManager authManager;

    private CoachAdapter coachAdapter;
    private CoachBookingAdapter userBookingAdapter;
    private CoachBookingAdapter coachPendingAdapter;
    private CoachBookingAdapter coachScheduleAdapter;
    private CoachBookingAdapter adminReviewAdapter;

    private List<CoachBookingEntity> userBookings = Collections.emptyList();
    private List<CoachBookingEntity> coachPendingBookings = Collections.emptyList();
    private List<CoachBookingEntity> coachScheduleBookings = Collections.emptyList();
    private List<CoachBookingEntity> adminReviewBookings = Collections.emptyList();

    private AuthState currentAuthState = AuthState.guest();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoachBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authManager = AuthManager.getInstance(requireContext());
        viewModel = new ViewModelProvider(this).get(CoachViewModel.class);

        setupAdapters();
        observeData();

        authManager.observe().observe(getViewLifecycleOwner(), this::renderAuth);
        renderAuth(authManager.getCurrent());
    }

    private void setupAdapters() {
        coachAdapter = new CoachAdapter(this::handleBook);
        binding.rvCoaches.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCoaches.setAdapter(coachAdapter);
        coachAdapter.submitList(mockCoaches());

        userBookingAdapter = new CoachBookingAdapter(CoachBookingAdapter.Mode.READ_ONLY, null);

        coachPendingAdapter = new CoachBookingAdapter(CoachBookingAdapter.Mode.COACH_PENDING, new CoachBookingAdapter.ActionListener() {
            @Override
            public void onPrimaryAction(long bookingId) {
                if (currentAuthState.role != AuthRole.COACH) {
                    return;
                }
                viewModel.coachAccept(
                        bookingId,
                        currentAuthState.username,
                        result -> onActionResult(result, R.string.booking_coach_accept_success)
                );
            }

            @Override
            public void onSecondaryAction(long bookingId) {
                if (currentAuthState.role != AuthRole.COACH) {
                    return;
                }
                viewModel.coachReject(
                        bookingId,
                        currentAuthState.username,
                        result -> onActionResult(result, R.string.booking_coach_reject_success)
                );
            }
        });

        coachScheduleAdapter = new CoachBookingAdapter(CoachBookingAdapter.Mode.READ_ONLY, null);

        adminReviewAdapter = new CoachBookingAdapter(CoachBookingAdapter.Mode.ADMIN_REVIEW, new CoachBookingAdapter.ActionListener() {
            @Override
            public void onPrimaryAction(long bookingId) {
                if (currentAuthState.role != AuthRole.ADMIN) {
                    return;
                }
                viewModel.adminApprove(bookingId, result -> onActionResult(result, R.string.booking_admin_approve_success));
            }

            @Override
            public void onSecondaryAction(long bookingId) {
                if (currentAuthState.role != AuthRole.ADMIN) {
                    return;
                }
                viewModel.adminReject(bookingId, result -> onActionResult(result, R.string.booking_admin_reject_success));
            }
        });

        binding.rvCoachPrimary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCoachSecondary.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void observeData() {
        viewModel.getUserBookings().observe(getViewLifecycleOwner(), data -> {
            userBookings = data == null ? Collections.emptyList() : data;
            userBookingAdapter.submitList(new ArrayList<>(userBookings));
            refreshRolePanels();
        });

        viewModel.getCoachPendingBookings().observe(getViewLifecycleOwner(), data -> {
            coachPendingBookings = data == null ? Collections.emptyList() : data;
            coachPendingAdapter.submitList(new ArrayList<>(coachPendingBookings));
            refreshRolePanels();
        });

        viewModel.getCoachScheduleBookings().observe(getViewLifecycleOwner(), data -> {
            coachScheduleBookings = data == null ? Collections.emptyList() : data;
            coachScheduleAdapter.submitList(new ArrayList<>(coachScheduleBookings));
            refreshRolePanels();
        });

        viewModel.getAdminReviewBookings().observe(getViewLifecycleOwner(), data -> {
            adminReviewBookings = data == null ? Collections.emptyList() : data;
            adminReviewAdapter.submitList(new ArrayList<>(adminReviewBookings));
            refreshRolePanels();
        });
    }

    private void renderAuth(AuthState state) {
        currentAuthState = state;
        String roleText = RolePolicy.roleLabel(requireContext(), state.role);
        binding.tvCoachRole.setText(getString(R.string.auth_role, roleText));
        binding.tvCoachDuty.setText(getString(R.string.auth_duty, RolePolicy.roleDuty(requireContext(), state.role)));

        viewModel.setCurrentUser(state.username);
        refreshRolePanels();
    }

    private void refreshRolePanels() {
        if (binding == null) {
            return;
        }

        if (!currentAuthState.loggedIn) {
            binding.tvCoachLoginTip.setVisibility(View.VISIBLE);
            binding.tvCoachLoginTip.setText(R.string.coach_login_required_hint);

            binding.tvCoachUserSectionTitle.setVisibility(View.VISIBLE);
            binding.rvCoaches.setVisibility(View.VISIBLE);

            binding.tvCoachPrimaryTitle.setText(R.string.coach_user_bookings_title);
            binding.rvCoachPrimary.setVisibility(View.GONE);
            binding.tvCoachPrimaryEmpty.setVisibility(View.VISIBLE);
            binding.tvCoachPrimaryEmpty.setText(R.string.coach_guest_bookings_hint);

            binding.tvCoachSecondaryTitle.setVisibility(View.GONE);
            binding.rvCoachSecondary.setVisibility(View.GONE);
            binding.tvCoachSecondaryEmpty.setVisibility(View.GONE);
            return;
        }

        binding.tvCoachLoginTip.setVisibility(View.GONE);

        if (currentAuthState.role == AuthRole.USER) {
            binding.tvCoachUserSectionTitle.setVisibility(View.VISIBLE);
            binding.rvCoaches.setVisibility(View.VISIBLE);

            binding.tvCoachPrimaryTitle.setText(R.string.coach_user_bookings_title);
            binding.rvCoachPrimary.setAdapter(userBookingAdapter);
            binding.rvCoachPrimary.setVisibility(View.VISIBLE);
            setEmptyState(binding.tvCoachPrimaryEmpty, userBookings.isEmpty(), R.string.coach_user_bookings_empty);

            binding.tvCoachSecondaryTitle.setVisibility(View.GONE);
            binding.rvCoachSecondary.setVisibility(View.GONE);
            binding.tvCoachSecondaryEmpty.setVisibility(View.GONE);
            return;
        }

        if (currentAuthState.role == AuthRole.COACH) {
            binding.tvCoachUserSectionTitle.setVisibility(View.GONE);
            binding.rvCoaches.setVisibility(View.GONE);

            binding.tvCoachPrimaryTitle.setText(R.string.coach_pending_title);
            binding.rvCoachPrimary.setAdapter(coachPendingAdapter);
            binding.rvCoachPrimary.setVisibility(View.VISIBLE);
            setEmptyState(binding.tvCoachPrimaryEmpty, coachPendingBookings.isEmpty(), R.string.coach_pending_empty);

            binding.tvCoachSecondaryTitle.setVisibility(View.VISIBLE);
            binding.tvCoachSecondaryTitle.setText(R.string.coach_schedule_title);
            binding.rvCoachSecondary.setAdapter(coachScheduleAdapter);
            binding.rvCoachSecondary.setVisibility(View.VISIBLE);
            setEmptyState(binding.tvCoachSecondaryEmpty, coachScheduleBookings.isEmpty(), R.string.coach_schedule_empty);
            return;
        }

        binding.tvCoachUserSectionTitle.setVisibility(View.GONE);
        binding.rvCoaches.setVisibility(View.GONE);

        binding.tvCoachPrimaryTitle.setText(R.string.admin_review_title);
        binding.rvCoachPrimary.setAdapter(adminReviewAdapter);
        binding.rvCoachPrimary.setVisibility(View.VISIBLE);
        setEmptyState(binding.tvCoachPrimaryEmpty, adminReviewBookings.isEmpty(), R.string.admin_review_empty);

        binding.tvCoachSecondaryTitle.setVisibility(View.GONE);
        binding.rvCoachSecondary.setVisibility(View.GONE);
        binding.tvCoachSecondaryEmpty.setVisibility(View.GONE);
    }

    private void setEmptyState(TextView emptyTextView, boolean empty, int messageRes) {
        emptyTextView.setVisibility(empty ? View.VISIBLE : View.GONE);
        emptyTextView.setText(messageRes);
    }

    private void handleBook(CoachItem item) {
        if (!currentAuthState.loggedIn) {
            Toast.makeText(requireContext(), R.string.login_required_action, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentAuthState.role == AuthRole.COACH) {
            Toast.makeText(requireContext(), R.string.role_duty_coach, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentAuthState.role == AuthRole.ADMIN) {
            Toast.makeText(requireContext(), R.string.role_duty_admin, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.createBooking(currentAuthState.username, item, result -> onActionResult(result, R.string.booking_create_success));
    }

    private void onActionResult(ToggleResult result, int successRes) {
        if (result == ToggleResult.SUCCESS) {
            Toast.makeText(requireContext(), successRes, Toast.LENGTH_SHORT).show();
            return;
        }
        if (result == ToggleResult.TOO_FAST) {
            Toast.makeText(requireContext(), R.string.action_too_fast, Toast.LENGTH_SHORT).show();
            return;
        }
        if (result == ToggleResult.BUSY) {
            Toast.makeText(requireContext(), R.string.action_processing, Toast.LENGTH_SHORT).show();
            return;
        }
        if (result == ToggleResult.NOT_FOUND) {
            Toast.makeText(requireContext(), R.string.booking_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(), R.string.action_failed, Toast.LENGTH_SHORT).show();
    }

    private List<CoachItem> mockCoaches() {
        return Arrays.asList(
                new CoachItem(1L, "李教练", "增肌与力量周期", "线下训练指导"),
                new CoachItem(2L, "王教练", "减脂塑形与体态", "线上动作纠正"),
                new CoachItem(3L, "陈教练", "运动康复与拉伸", "饮食计划定制")
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
