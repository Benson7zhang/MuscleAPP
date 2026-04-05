package com.musclefit.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthRole;
import com.musclefit.app.auth.AuthState;
import com.musclefit.app.auth.RolePolicy;
import com.musclefit.app.databinding.FragmentProfileBinding;
import com.musclefit.app.theme.ThemeManager;

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
        setupFavoritesEntry();

        authManager.observe().observe(getViewLifecycleOwner(), this::renderAuthState);
        renderAuthState(authManager.getCurrent());
    }

    private void setupFavoritesEntry() {
        binding.cardFavoritesEntry.setOnClickListener(v -> {
            if (!requireLogin()) {
                return;
            }
            startActivity(new Intent(requireContext(), FavoritesActivity.class));
        });
    }

    private void setupDrawerActions() {
        binding.btnOpenProfileDrawer.setOnClickListener(v -> binding.drawerProfile.openDrawer(GravityCompat.END));

        binding.rgLoginRole.check(R.id.rb_role_user);

        binding.btnLogin.setOnClickListener(v -> {
            AuthRole role = selectedRole();
            String name = "";
            if (binding.etLoginName.getText() != null) {
                name = binding.etLoginName.getText().toString().trim();
            }
            authManager.login(name, role);
            Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
            binding.drawerProfile.closeDrawer(GravityCompat.END);
        });

        binding.btnLogout.setOnClickListener(v -> {
            authManager.logout();
            Toast.makeText(requireContext(), R.string.logout_success, Toast.LENGTH_SHORT).show();
            binding.drawerProfile.closeDrawer(GravityCompat.END);
        });

        binding.switchTheme.setChecked(ThemeManager.isDarkMode(requireContext()));
        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) ->
                ThemeManager.setDarkMode(requireContext(), isChecked)
        );
    }

    private AuthRole selectedRole() {
        int checkedId = binding.rgLoginRole.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_role_admin) {
            return AuthRole.ADMIN;
        }
        if (checkedId == R.id.rb_role_coach) {
            return AuthRole.COACH;
        }
        return AuthRole.USER;
    }

    private void renderAuthState(AuthState state) {
        String statusText = state.loggedIn
                ? getString(R.string.auth_status_logged_in)
                : getString(R.string.auth_status_logged_out);
        binding.tvAuthStatus.setText(getString(R.string.auth_status, statusText));

        String roleText = RolePolicy.roleLabel(requireContext(), state.role);
        binding.tvAuthRole.setText(getString(R.string.auth_role, roleText));
        binding.tvAuthDuty.setText(getString(R.string.auth_duty, RolePolicy.roleDuty(requireContext(), state.role)));

        if (state.loggedIn) {
            binding.tvAuthUser.setText(getString(R.string.auth_user, state.username));
            binding.tvProfileGuestTip.setVisibility(View.GONE);
            binding.etLoginName.setText(state.username);
            binding.btnLogout.setEnabled(true);
            binding.cardFavoritesEntry.setEnabled(true);
            binding.cardFavoritesEntry.setAlpha(1f);
            binding.tvFavoritesEntrySubtitle.setText(R.string.favorites_entry_subtitle_logged_in);

            if (state.role == AuthRole.ADMIN) {
                binding.rgLoginRole.check(R.id.rb_role_admin);
            } else if (state.role == AuthRole.COACH) {
                binding.rgLoginRole.check(R.id.rb_role_coach);
            } else {
                binding.rgLoginRole.check(R.id.rb_role_user);
            }
            return;
        }

        binding.tvAuthUser.setText(getString(R.string.auth_user, getString(R.string.role_guest)));
        binding.tvProfileGuestTip.setVisibility(View.VISIBLE);
        binding.btnLogout.setEnabled(false);
        binding.cardFavoritesEntry.setEnabled(false);
        binding.cardFavoritesEntry.setAlpha(0.6f);
        binding.tvFavoritesEntrySubtitle.setText(R.string.favorites_entry_subtitle_logged_out);
    }

    private boolean requireLogin() {
        if (authManager.isLoggedIn()) {
            return true;
        }
        Toast.makeText(requireContext(), R.string.login_required_action, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
