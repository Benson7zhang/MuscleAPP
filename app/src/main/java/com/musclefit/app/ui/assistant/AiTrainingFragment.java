package com.musclefit.app.ui.assistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.musclefit.app.BuildConfig;
import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.databinding.FragmentAiTrainingBinding;

public class AiTrainingFragment extends Fragment {
    private static final String TYPE_TRAINING = "training";
    private static final String TYPE_NUTRITION = "nutrition";
    private static final String TAG_TRAINING = "ai_training_tab_training";
    private static final String TAG_NUTRITION = "ai_training_tab_nutrition";

    private FragmentAiTrainingBinding binding;
    private AuthManager authManager;
    private String currentType = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAiTrainingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authManager = AuthManager.getInstance(requireContext());

        binding.toggleAiAssistants.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || !authManager.isLoggedIn()) {
                return;
            }
            if (checkedId == R.id.btn_ai_nutrition) {
                switchAssistant(TYPE_NUTRITION);
            } else {
                switchAssistant(TYPE_TRAINING);
            }
        });

        if (savedInstanceState != null) {
            restoreSelectionFromFragments();
        }

        authManager.observe().observe(getViewLifecycleOwner(), state -> renderLoginGate(state.loggedIn));
        renderLoginGate(authManager.isLoggedIn());
    }

    private void renderApiKeyStatus() {
        String apiKey = BuildConfig.LONGCAT_API_KEY == null ? "" : BuildConfig.LONGCAT_API_KEY.trim();
        if (apiKey.isEmpty()) {
            binding.tvAiKeyStatus.setText(R.string.ai_key_missing_hint);
            binding.tvAiKeyStatus.setTextColor(requireContext().getColor(R.color.mf_muted));
            return;
        }
        binding.tvAiKeyStatus.setText(R.string.ai_key_configured_hint);
        binding.tvAiKeyStatus.setTextColor(requireContext().getColor(R.color.mf_primary));
    }

    private void renderLoginGate(boolean loggedIn) {
        if (binding == null) {
            return;
        }
        if (!loggedIn) {
            currentType = "";
            binding.tvAiKeyStatus.setText(R.string.ai_login_required_hint);
            binding.tvAiKeyStatus.setTextColor(requireContext().getColor(R.color.mf_muted));
            binding.tvAiCopyHint.setVisibility(View.GONE);
            binding.toggleAiAssistants.setEnabled(false);
            binding.btnAiTraining.setEnabled(false);
            binding.btnAiNutrition.setEnabled(false);
            binding.toggleAiAssistants.clearChecked();
            binding.aiAssistantContainer.setVisibility(View.GONE);
            return;
        }

        binding.toggleAiAssistants.setEnabled(true);
        binding.btnAiTraining.setEnabled(true);
        binding.btnAiNutrition.setEnabled(true);
        binding.tvAiCopyHint.setVisibility(View.VISIBLE);
        binding.aiAssistantContainer.setVisibility(View.VISIBLE);
        renderApiKeyStatus();

        int checkedId = binding.toggleAiAssistants.getCheckedButtonId();
        if (checkedId == View.NO_ID) {
            binding.toggleAiAssistants.check(R.id.btn_ai_training);
            checkedId = R.id.btn_ai_training;
        }

        if (checkedId == R.id.btn_ai_nutrition) {
            switchAssistant(TYPE_NUTRITION);
        } else {
            switchAssistant(TYPE_TRAINING);
        }
    }

    private void restoreSelectionFromFragments() {
        FragmentManager fm = getChildFragmentManager();
        Fragment training = fm.findFragmentByTag(TAG_TRAINING);
        Fragment nutrition = fm.findFragmentByTag(TAG_NUTRITION);
        if (nutrition != null && nutrition.isVisible()) {
            currentType = TYPE_NUTRITION;
            binding.toggleAiAssistants.check(R.id.btn_ai_nutrition);
            return;
        }
        if (training != null && training.isVisible()) {
            currentType = TYPE_TRAINING;
            binding.toggleAiAssistants.check(R.id.btn_ai_training);
        }
    }

    private void switchAssistant(String nextType) {
        if (binding == null) {
            return;
        }
        if (nextType == null || nextType.equals(currentType)) {
            return;
        }

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction tx = fm.beginTransaction().setReorderingAllowed(true);

        Fragment current = fm.findFragmentByTag(tagOf(currentType));
        if (current != null && current.isAdded()) {
            tx.hide(current);
        }

        Fragment next = fm.findFragmentByTag(tagOf(nextType));
        if (next == null) {
            next = createAssistant(nextType);
            tx.add(R.id.ai_assistant_container, next, tagOf(nextType));
        } else {
            tx.show(next);
        }

        tx.commit();
        currentType = nextType;
    }

    private Fragment createAssistant(String type) {
        if (TYPE_NUTRITION.equals(type)) {
            return new NutritionistAssistantFragment();
        }
        return new TrainingAssistantFragment();
    }

    private String tagOf(String type) {
        if (TYPE_NUTRITION.equals(type)) {
            return TAG_NUTRITION;
        }
        return TAG_TRAINING;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
