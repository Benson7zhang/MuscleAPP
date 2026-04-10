package com.musclefit.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.musclefit.app.R;
import com.musclefit.app.databinding.ActivityMainBinding;
import com.musclefit.app.ui.assistant.AiTrainingFragment;
import com.musclefit.app.ui.exercise.ExerciseListFragment;
import com.musclefit.app.ui.forum.ForumFragment;
import com.musclefit.app.ui.home.HomeFragment;
import com.musclefit.app.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_HOME = "tab_home";
    private static final String TAG_EXERCISE = "tab_exercise";
    private static final String TAG_AI = "tab_ai";
    private static final String TAG_FORUM = "tab_forum";
    private static final String TAG_PROFILE = "tab_profile";

    private ActivityMainBinding binding;
    private int currentTabId = View.NO_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            switchTab(item.getItemId());
            return true;
        });
        binding.bottomNav.setOnItemReselectedListener(item -> {
            // no-op: avoid duplicate fragment transactions on reselection
        });

        if (savedInstanceState == null) {
            switchTab(R.id.nav_home);
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
            return;
        }

        currentTabId = detectCurrentTabId();
        if (currentTabId == View.NO_ID) {
            switchTab(R.id.nav_home);
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
            return;
        }
        binding.bottomNav.setSelectedItemId(currentTabId);
    }

    public void selectTab(@IdRes int menuId) {
        if (binding.bottomNav.getSelectedItemId() == menuId) {
            switchTab(menuId);
            return;
        }
        binding.bottomNav.setSelectedItemId(menuId);
    }

    public void openExerciseWithFilter(@Nullable Bundle filter) {
        if (filter != null) {
            getSupportFragmentManager().setFragmentResult("exercise_filter", filter);
        }
        selectTab(R.id.nav_exercise);
    }

    private void switchTab(@IdRes int itemId) {
        if (itemId == currentTabId) {
            return;
        }

        FragmentManager manager = getSupportFragmentManager();
        Fragment target = findOrCreate(itemId, manager);
        if (target == null) {
            return;
        }

        Fragment current = currentTabId == View.NO_ID
                ? manager.findFragmentById(R.id.fragment_container)
                : manager.findFragmentByTag(tagOf(currentTabId));
        if (current == null) {
            current = manager.findFragmentById(R.id.fragment_container);
        }

        FragmentTransaction transaction = manager.beginTransaction().setReorderingAllowed(true);
        if (current != null && current != target && current.isAdded()) {
            transaction.hide(current);
        }
        if (target.isAdded()) {
            transaction.show(target);
        } else {
            transaction.add(R.id.fragment_container, target, tagOf(itemId));
        }

        if (manager.isStateSaved()) {
            transaction.commitAllowingStateLoss();
        } else {
            transaction.commit();
        }
        currentTabId = itemId;
    }

    @Nullable
    private Fragment findOrCreate(@IdRes int itemId, FragmentManager manager) {
        Fragment existing = manager.findFragmentByTag(tagOf(itemId));
        if (existing != null) {
            return existing;
        }

        if (itemId == R.id.nav_exercise) {
            return new ExerciseListFragment();
        }
        if (itemId == R.id.nav_ai) {
            return new AiTrainingFragment();
        }
        if (itemId == R.id.nav_forum) {
            return new ForumFragment();
        }
        if (itemId == R.id.nav_profile) {
            return new ProfileFragment();
        }
        if (itemId == R.id.nav_home) {
            return new HomeFragment();
        }
        return null;
    }

    private int detectCurrentTabId() {
        Fragment container = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (container instanceof HomeFragment) {
            return R.id.nav_home;
        }
        if (container instanceof ExerciseListFragment) {
            return R.id.nav_exercise;
        }
        if (container instanceof AiTrainingFragment) {
            return R.id.nav_ai;
        }
        if (container instanceof ForumFragment) {
            return R.id.nav_forum;
        }
        if (container instanceof ProfileFragment) {
            return R.id.nav_profile;
        }

        if (isVisible(TAG_HOME)) {
            return R.id.nav_home;
        }
        if (isVisible(TAG_EXERCISE)) {
            return R.id.nav_exercise;
        }
        if (isVisible(TAG_AI)) {
            return R.id.nav_ai;
        }
        if (isVisible(TAG_FORUM)) {
            return R.id.nav_forum;
        }
        if (isVisible(TAG_PROFILE)) {
            return R.id.nav_profile;
        }
        return View.NO_ID;
    }

    private boolean isVisible(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return fragment != null && fragment.isVisible();
    }

    private String tagOf(@IdRes int itemId) {
        if (itemId == R.id.nav_exercise) {
            return TAG_EXERCISE;
        }
        if (itemId == R.id.nav_ai) {
            return TAG_AI;
        }
        if (itemId == R.id.nav_forum) {
            return TAG_FORUM;
        }
        if (itemId == R.id.nav_profile) {
            return TAG_PROFILE;
        }
        return TAG_HOME;
    }
}
