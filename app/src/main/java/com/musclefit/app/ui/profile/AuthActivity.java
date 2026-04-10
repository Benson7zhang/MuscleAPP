package com.musclefit.app.ui.profile;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.databinding.ActivityAuthBinding;

public class AuthActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_LOGIN = 0;
    public static final int MODE_REGISTER = 1;

    private ActivityAuthBinding binding;
    private AuthManager authManager;
    private int currentMode = MODE_LOGIN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance(this);
        currentMode = getIntent().getIntExtra(EXTRA_MODE, MODE_LOGIN);

        binding.toolbarAuth.setNavigationOnClickListener(v -> finish());
        binding.btnModeLogin.setOnClickListener(v -> updateMode(MODE_LOGIN));
        binding.btnModeRegister.setOnClickListener(v -> updateMode(MODE_REGISTER));
        binding.btnAuthSubmit.setOnClickListener(v -> submit());

        updateMode(currentMode);
    }

    private void updateMode(int mode) {
        updateMode(mode, true);
    }

    private void updateMode(int mode, boolean clearInputs) {
        currentMode = mode == MODE_REGISTER ? MODE_REGISTER : MODE_LOGIN;
        boolean isLogin = currentMode == MODE_LOGIN;

        binding.toolbarAuth.setTitle(isLogin ? R.string.login_title : R.string.register_title);
        binding.btnModeLogin.setChecked(isLogin);
        binding.btnModeRegister.setChecked(!isLogin);

        binding.tilAuthIdentifier.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        binding.tilAuthNickname.setVisibility(isLogin ? View.GONE : View.VISIBLE);
        binding.tilAuthPassword.setHint(getString(isLogin ? R.string.login_password_hint : R.string.register_password_hint));
        binding.etAuthIdentifier.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.btnAuthSubmit.setText(isLogin ? R.string.login_button : R.string.register_button);
        binding.tvAuthHint.setText(isLogin ? R.string.auth_login_hint : R.string.auth_register_hint);

        if (clearInputs) {
            binding.etAuthIdentifier.setText("");
            binding.etAuthNickname.setText("");
            binding.etAuthPassword.setText("");
        }
    }

    private void submit() {
        if (currentMode == MODE_LOGIN) {
            AuthManager.AuthActionResult result = authManager.login(textOf(binding.etAuthIdentifier), textOf(binding.etAuthPassword));
            if (result.success) {
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toast(result.message);
            return;
        }

        AuthManager.AuthActionResult result = authManager.register(textOf(binding.etAuthNickname), textOf(binding.etAuthPassword));
        if (result.success) {
            Toast.makeText(this, getString(R.string.register_success_with_id, result.accountId), Toast.LENGTH_LONG).show();
            updateMode(MODE_LOGIN, false);
            binding.etAuthIdentifier.setText(result.accountId);
            binding.etAuthNickname.setText("");
            binding.etAuthPassword.setText("");
            binding.etAuthPassword.requestFocus();
            return;
        }
        toast(result.message);
    }

    private String textOf(android.widget.EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void toast(String message) {
        if (message == null || message.trim().isEmpty()) {
            Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
