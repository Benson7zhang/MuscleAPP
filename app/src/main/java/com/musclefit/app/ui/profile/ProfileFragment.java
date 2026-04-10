package com.musclefit.app.ui.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthState;
import com.musclefit.app.auth.RolePolicy;
import com.musclefit.app.databinding.DialogProfileEditBinding;
import com.musclefit.app.databinding.FragmentProfileBinding;
import com.musclefit.app.theme.ThemeManager;

import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authManager = AuthManager.getInstance(requireContext());

        setupDrawerActions();
        setupMainActions();

        authManager.observe().observe(getViewLifecycleOwner(), this::renderAuthState);
        renderAuthState(authManager.getCurrent());
    }

    private void setupMainActions() {
        binding.cardFavoritesEntry.setOnClickListener(v -> {
            if (!requireLogin()) {
                return;
            }
            startActivity(new Intent(requireContext(), FavoritesActivity.class));
        });

        binding.cardProfileEditEntry.setOnClickListener(v -> {
            if (!requireLogin()) {
                return;
            }
            showEditProfileDialog();
        });
    }

    private void setupDrawerActions() {
        binding.btnOpenProfileDrawer.setOnClickListener(v -> binding.drawerProfile.openDrawer(GravityCompat.END));

        binding.btnLogin.setOnClickListener(v -> openAuthPage(AuthActivity.MODE_LOGIN));
        binding.btnRegister.setOnClickListener(v -> openAuthPage(AuthActivity.MODE_REGISTER));

        binding.btnChangePassword.setOnClickListener(v -> {
            AuthManager.AuthActionResult result = authManager.changePassword(
                    textOf(binding.etSecurityOldPassword),
                    textOf(binding.etSecurityNewPassword),
                    textOf(binding.etSecurityConfirmPassword)
            );
            if (result.success) {
                Toast.makeText(requireContext(), R.string.account_security_change_password_success, Toast.LENGTH_SHORT).show();
                binding.etSecurityOldPassword.setText("");
                binding.etSecurityNewPassword.setText("");
                binding.etSecurityConfirmPassword.setText("");
                binding.drawerProfile.closeDrawer(GravityCompat.END);
                return;
            }
            toast(result.message);
        });

        binding.btnLogout.setOnClickListener(v -> performLogout());

        binding.switchTheme.setChecked(ThemeManager.isDarkMode(requireContext()));
        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) ->
                ThemeManager.setDarkMode(requireContext(), isChecked)
        );
    }

    private void openAuthPage(int mode) {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.putExtra(AuthActivity.EXTRA_MODE, mode);
        startActivity(intent);
        binding.drawerProfile.closeDrawer(GravityCompat.END);
    }

    private void performLogout() {
        authManager.logout();
        Toast.makeText(requireContext(), R.string.logout_success, Toast.LENGTH_SHORT).show();
        binding.drawerProfile.closeDrawer(GravityCompat.END);
    }

    private void showEditProfileDialog() {
        AuthState state = authManager.getCurrent();
        if (!state.loggedIn) {
            Toast.makeText(requireContext(), R.string.login_required_action, Toast.LENGTH_SHORT).show();
            return;
        }

        DialogProfileEditBinding dialogBinding = DialogProfileEditBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.etDialogProfileNickname.setText(state.nickname);
        dialogBinding.etDialogProfileGender.setText(state.gender);
        dialogBinding.etDialogProfilePhone.setText(state.phone);
        dialogBinding.etDialogProfileWeight.setText(state.weightKg);
        dialogBinding.etDialogProfileHeight.setText(state.heightCm);
        dialogBinding.etDialogProfileBirth.setText(state.birthDate);

        dialogBinding.etDialogProfileGender.setOnClickListener(v -> showGenderPicker(dialogBinding.etDialogProfileGender));
        dialogBinding.etDialogProfileBirth.setOnClickListener(v -> showBirthDatePicker(dialogBinding.etDialogProfileBirth));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_edit_title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.forum_cancel, null)
                .setPositiveButton(R.string.update_profile_button, (dialog, which) -> {
                    AuthManager.AuthActionResult result = authManager.updateProfile(
                            textOf(dialogBinding.etDialogProfileNickname),
                            textOf(dialogBinding.etDialogProfileGender),
                            textOf(dialogBinding.etDialogProfilePhone),
                            textOf(dialogBinding.etDialogProfileWeight),
                            textOf(dialogBinding.etDialogProfileHeight),
                            textOf(dialogBinding.etDialogProfileBirth)
                    );
                    if (result.success) {
                        Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    toast(result.message);
                })
                .show();
    }

    private void showGenderPicker(TextInputEditText target) {
        String[] options = new String[]{
                getString(R.string.gender_male),
                getString(R.string.gender_female),
                getString(R.string.gender_other)
        };

        NumberPicker picker = new NumberPicker(requireContext());
        picker.setMinValue(0);
        picker.setMaxValue(options.length - 1);
        picker.setDisplayedValues(options);
        picker.setWrapSelectorWheel(false);
        picker.setValue(findGenderIndex(textOf(target), options));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_gender_picker_title)
                .setView(picker)
                .setNegativeButton(R.string.forum_cancel, null)
                .setPositiveButton(R.string.forum_save, (dialog, which) -> target.setText(options[picker.getValue()]))
                .show();
    }

    private int findGenderIndex(String currentValue, String[] options) {
        if (currentValue == null) {
            return 0;
        }
        String value = currentValue.trim();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
                return i;
            }
        }
        if (value.contains(getString(R.string.gender_female))) {
            return 1;
        }
        if (value.contains(getString(R.string.gender_other))) {
            return 2;
        }
        return 0;
    }

    private void showBirthDatePicker(TextInputEditText target) {
        Calendar calendar = Calendar.getInstance();
        int[] parts = parseDate(textOf(target));
        if (parts != null) {
            calendar.set(parts[0], parts[1] - 1, parts[2]);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> target.setText(String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                )),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private int[] parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String[] values = text.split("-");
        if (values.length != 3) {
            return null;
        }
        try {
            int year = Integer.parseInt(values[0]);
            int month = Integer.parseInt(values[1]);
            int day = Integer.parseInt(values[2]);
            if (year < 1900 || month < 1 || month > 12 || day < 1 || day > 31) {
                return null;
            }
            return new int[]{year, month, day};
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void renderAuthState(AuthState state) {
        binding.tvProfileNavSubtitle.setText(
                state.loggedIn
                        ? R.string.profile_nav_subtitle_logged_in
                        : R.string.profile_nav_subtitle_guest
        );

        String statusText = state.loggedIn
                ? getString(R.string.auth_status_logged_in)
                : getString(R.string.auth_status_logged_out);
        binding.tvAuthStatus.setText(getString(R.string.auth_status, statusText));

        String roleText = RolePolicy.roleLabel(requireContext(), state.role);
        binding.tvAuthRole.setText(getString(R.string.auth_role, roleText));
        binding.tvAuthDuty.setText(getString(R.string.auth_duty, RolePolicy.roleDuty(requireContext(), state.role)));

        if (state.loggedIn) {
            binding.tvAuthUser.setText(getString(R.string.auth_user, state.accountId));
            binding.tvAuthNickname.setText(getString(R.string.auth_nickname, state.nickname));
            binding.tvAuthGender.setText(getString(R.string.auth_gender, profileValue(state.gender)));
            binding.tvAuthPhone.setText(getString(R.string.auth_phone, profileValue(state.phone)));
            binding.tvAuthWeight.setText(getString(R.string.auth_weight, profileValue(state.weightKg)));
            binding.tvAuthHeight.setText(getString(R.string.auth_height, profileValue(state.heightCm)));
            binding.tvAuthBirth.setText(getString(R.string.auth_birth, profileValue(state.birthDate)));
            binding.tvProfileGuestTip.setVisibility(View.GONE);

            binding.btnLogout.setEnabled(true);
            binding.btnChangePassword.setEnabled(true);
            binding.tvAccountSecurityLoginHint.setVisibility(View.GONE);
            binding.tilSecurityOldPassword.setVisibility(View.VISIBLE);
            binding.tilSecurityNewPassword.setVisibility(View.VISIBLE);
            binding.tilSecurityConfirmPassword.setVisibility(View.VISIBLE);
            binding.btnChangePassword.setVisibility(View.VISIBLE);

            binding.cardProfileEditEntry.setEnabled(true);
            binding.cardProfileEditEntry.setAlpha(1f);
            binding.tvProfileEditEntrySubtitle.setText(R.string.profile_edit_entry_subtitle_logged_in);

            binding.cardFavoritesEntry.setEnabled(true);
            binding.cardFavoritesEntry.setAlpha(1f);
            binding.tvFavoritesEntrySubtitle.setText(
                    getString(R.string.favorites_scope_hint_with_user, state.accountId)
            );
            return;
        }

        binding.tvAuthUser.setText(getString(R.string.auth_user, getString(R.string.role_guest)));
        binding.tvAuthNickname.setText(getString(R.string.auth_nickname, "-"));
        binding.tvAuthGender.setText(getString(R.string.auth_gender, "-"));
        binding.tvAuthPhone.setText(getString(R.string.auth_phone, "-"));
        binding.tvAuthWeight.setText(getString(R.string.auth_weight, "-"));
        binding.tvAuthHeight.setText(getString(R.string.auth_height, "-"));
        binding.tvAuthBirth.setText(getString(R.string.auth_birth, "-"));
        binding.tvProfileGuestTip.setVisibility(View.VISIBLE);

        binding.btnLogout.setEnabled(false);
        binding.btnChangePassword.setEnabled(false);
        binding.tvAccountSecurityLoginHint.setVisibility(View.VISIBLE);
        binding.tilSecurityOldPassword.setVisibility(View.GONE);
        binding.tilSecurityNewPassword.setVisibility(View.GONE);
        binding.tilSecurityConfirmPassword.setVisibility(View.GONE);
        binding.btnChangePassword.setVisibility(View.GONE);

        binding.etSecurityOldPassword.setText("");
        binding.etSecurityNewPassword.setText("");
        binding.etSecurityConfirmPassword.setText("");

        binding.cardProfileEditEntry.setEnabled(false);
        binding.cardProfileEditEntry.setAlpha(0.65f);
        binding.tvProfileEditEntrySubtitle.setText(R.string.profile_edit_entry_subtitle_logged_out);

        binding.cardFavoritesEntry.setEnabled(false);
        binding.cardFavoritesEntry.setAlpha(0.65f);
        binding.tvFavoritesEntrySubtitle.setText(R.string.favorites_entry_subtitle_logged_out);
    }

    private boolean requireLogin() {
        if (authManager.isLoggedIn()) {
            return true;
        }
        Toast.makeText(requireContext(), R.string.login_required_action, Toast.LENGTH_SHORT).show();
        return false;
    }

    private String textOf(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void toast(String message) {
        if (message == null || message.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.action_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String profileValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
