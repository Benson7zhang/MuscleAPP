package com.musclefit.app.ui.forum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthState;
import com.musclefit.app.data.model.ForumPostCard;
import com.musclefit.app.databinding.DialogForumComposeSheetBinding;
import com.musclefit.app.databinding.FragmentForumBinding;
import com.musclefit.app.repo.ForumActionResult;
import com.musclefit.app.ui.exercise.ImagePreviewActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class ForumFragment extends Fragment {
    private static final int FILTER_ALL = 0;
    private static final int FILTER_MINE = 1;
    private static final int FILTER_FAVORITE = 2;
    private static final int MAX_COMPOSE_IMAGES = 3;

    private static final int SORT_LATEST = 0;
    private static final int SORT_LIKE_DESC = 1;
    private static final int SORT_FAVORITE_DESC = 2;

    private FragmentForumBinding binding;
    private ForumViewModel viewModel;
    private AuthManager authManager;
    private ForumPostAdapter adapter;
    private AuthState currentAuthState;
    private int filterMode = FILTER_ALL;
    private int sortMode = SORT_LATEST;
    private List<ForumPostCard> latestPosts = new ArrayList<>();

    private BottomSheetDialog composeDialog;
    private DialogForumComposeSheetBinding composeBinding;
    private ForumComposeImageAdapter composeImageAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImagesLauncher;

    private String draftTitle = "";
    private String draftContent = "";
    private final List<String> draftImageUris = new ArrayList<>();
    private boolean publishingPost = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ForumViewModel.class);
        authManager = AuthManager.getInstance(requireContext());

        setupImagePicker();
        setupRecycler();
        setupComposerTrigger();
        setupFilter();
        setupSort();
        observeData();

        authManager.observe().observe(getViewLifecycleOwner(), this::renderAuthState);
        renderAuthState(authManager.getCurrent());
    }

    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(MAX_COMPOSE_IMAGES),
                this::onImagesPicked
        );
    }

    private void setupRecycler() {
        adapter = new ForumPostAdapter(new ForumPostAdapter.Listener() {
            @Override
            public void onPostClick(ForumPostCard post) {
                openPostDetail(post.id);
            }

            @Override
            public void onLikeClick(ForumPostCard post) {
                viewModel.toggleLike(post.id, ForumFragment.this::onActionResult);
            }

            @Override
            public void onFavoriteClick(ForumPostCard post) {
                viewModel.toggleFavorite(post.id, ForumFragment.this::onActionResult);
            }

            @Override
            public void onCommentClick(ForumPostCard post) {
                openPostDetail(post.id);
            }
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvForumPosts.setLayoutManager(layoutManager);
        binding.rvForumPosts.setAdapter(adapter);
    }

    private void setupComposerTrigger() {
        binding.fabForumCompose.setOnClickListener(v -> {
            if (currentAuthState == null || !currentAuthState.loggedIn) {
                Toast.makeText(requireContext(), R.string.login_required_action, Toast.LENGTH_SHORT).show();
                return;
            }
            showComposeSheet();
        });
    }

    private void setupFilter() {
        binding.chipGroupForumFilter.check(R.id.chip_forum_all);
        binding.chipGroupForumFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.chip_forum_mine) {
                filterMode = FILTER_MINE;
            } else if (checkedId == R.id.chip_forum_favorite) {
                filterMode = FILTER_FAVORITE;
            } else {
                filterMode = FILTER_ALL;
            }
            renderPostList();
        });
    }

    private void setupSort() {
        binding.chipGroupForumSort.check(R.id.chip_forum_sort_latest);
        binding.chipGroupForumSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.chip_forum_sort_like) {
                sortMode = SORT_LIKE_DESC;
            } else if (checkedId == R.id.chip_forum_sort_favorite) {
                sortMode = SORT_FAVORITE_DESC;
            } else {
                sortMode = SORT_LATEST;
            }
            renderPostList();
        });
    }

    private void observeData() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            latestPosts = posts == null ? new ArrayList<>() : posts;
            renderPostList();
        });
    }

    private void renderAuthState(AuthState state) {
        currentAuthState = state;
        viewModel.setAccountScope(state.loggedIn ? state.accountId : "guest");

        boolean canWrite = state.loggedIn;
        binding.tvForumLoginTip.setVisibility(canWrite ? View.GONE : View.VISIBLE);
        binding.fabForumCompose.setAlpha(canWrite ? 1f : 0.64f);

        binding.chipForumMine.setEnabled(canWrite);
        binding.chipForumFavorite.setEnabled(canWrite);
        if (!canWrite && filterMode != FILTER_ALL) {
            filterMode = FILTER_ALL;
            binding.chipGroupForumFilter.check(R.id.chip_forum_all);
        }

        if (!canWrite && composeDialog != null && composeDialog.isShowing()) {
            composeDialog.dismiss();
        }

        updateComposePublishState();
        renderPostList();
    }

    private void renderPostList() {
        if (binding == null || adapter == null) {
            return;
        }
        List<ForumPostCard> source = latestPosts == null ? new ArrayList<>() : latestPosts;
        List<ForumPostCard> visiblePosts = buildVisiblePosts(source);
        adapter.submitList(visiblePosts);
        binding.tvForumEmpty.setVisibility(visiblePosts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<ForumPostCard> buildVisiblePosts(List<ForumPostCard> source) {
        List<ForumPostCard> filtered = new ArrayList<>();
        boolean loggedIn = currentAuthState != null && currentAuthState.loggedIn;
        String myId = (loggedIn && currentAuthState.accountId != null) ? currentAuthState.accountId : "";

        for (ForumPostCard post : source) {
            if (post == null) {
                continue;
            }
            if (!loggedIn || filterMode == FILTER_ALL) {
                filtered.add(post);
                continue;
            }
            if (filterMode == FILTER_MINE) {
                if (post.authorAccountId != null && myId.equals(post.authorAccountId)) {
                    filtered.add(post);
                }
                continue;
            }
            if (filterMode == FILTER_FAVORITE && post.favoritedByMe) {
                filtered.add(post);
            }
        }

        sortPosts(filtered);
        return filtered;
    }

    private void sortPosts(List<ForumPostCard> posts) {
        if (posts.isEmpty()) {
            return;
        }
        if (sortMode == SORT_LIKE_DESC) {
            Collections.sort(posts, (left, right) -> {
                int metric = Integer.compare(right.likeCount, left.likeCount);
                if (metric != 0) {
                    return metric;
                }
                return compareByTime(left, right);
            });
            return;
        }
        if (sortMode == SORT_FAVORITE_DESC) {
            Collections.sort(posts, (left, right) -> {
                int metric = Integer.compare(right.favoriteCount, left.favoriteCount);
                if (metric != 0) {
                    return metric;
                }
                return compareByTime(left, right);
            });
            return;
        }
        Collections.sort(posts, this::compareByTime);
    }

    private int compareByTime(ForumPostCard left, ForumPostCard right) {
        int byUpdated = Long.compare(right.updatedAt, left.updatedAt);
        if (byUpdated != 0) {
            return byUpdated;
        }
        return Long.compare(right.id, left.id);
    }

    private void showComposeSheet() {
        if (!isAdded()) {
            return;
        }
        if (composeDialog == null) {
            initComposeSheet();
        }
        syncDraftToComposeSheet();
        refreshComposeImagesUi();
        updateComposePublishState();
        composeDialog.show();
    }

    private void initComposeSheet() {
        composeBinding = DialogForumComposeSheetBinding.inflate(getLayoutInflater());
        composeDialog = new BottomSheetDialog(requireContext());
        composeDialog.setContentView(composeBinding.getRoot());
        composeDialog.setOnDismissListener(dialog -> syncDraftFromComposeSheet());

        RecyclerView imagesRecycler = composeBinding.rvForumComposeImages;
        imagesRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        composeImageAdapter = new ForumComposeImageAdapter(new ForumComposeImageAdapter.Listener() {
            @Override
            public void onRemoveClick(String uri) {
                draftImageUris.remove(uri);
                refreshComposeImagesUi();
            }

            @Override
            public void onImageClick(String uri) {
                openImagePreview(uri);
            }
        });
        imagesRecycler.setAdapter(composeImageAdapter);

        composeBinding.btnForumSheetClose.setOnClickListener(v -> composeDialog.dismiss());
        composeBinding.btnForumComposeAddImage.setOnClickListener(v -> {
            if (pickImagesLauncher == null) {
                return;
            }
            pickImagesLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        composeBinding.btnForumSheetPublish.setOnClickListener(v -> publishPostFromSheet());

        composeBinding.etForumSheetTitle.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                draftTitle = editable == null ? "" : editable.toString().trim();
            }
        });
        composeBinding.etForumSheetContent.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                draftContent = editable == null ? "" : editable.toString().trim();
                updateComposePublishState();
            }
        });
    }

    private void publishPostFromSheet() {
        if (publishingPost) {
            return;
        }
        if (currentAuthState == null || !currentAuthState.loggedIn) {
            Toast.makeText(requireContext(), R.string.login_required_action, Toast.LENGTH_SHORT).show();
            return;
        }

        syncDraftFromComposeSheet();
        if (draftContent.isEmpty()) {
            Toast.makeText(requireContext(), R.string.forum_empty_input, Toast.LENGTH_SHORT).show();
            return;
        }

        publishingPost = true;
        updateComposePublishState();

        List<String> imageUris = new ArrayList<>(draftImageUris);
        viewModel.createPost(draftTitle, draftContent, imageUris, result -> {
            publishingPost = false;
            updateComposePublishState();
            if (!isAdded()) {
                return;
            }

            if (result == ForumActionResult.SUCCESS) {
                clearComposeDraft();
                if (composeBinding != null) {
                    composeBinding.etForumSheetTitle.setText("");
                    composeBinding.etForumSheetContent.setText("");
                    refreshComposeImagesUi();
                }
                if (composeDialog != null && composeDialog.isShowing()) {
                    composeDialog.dismiss();
                }
                if (filterMode == FILTER_FAVORITE) {
                    filterMode = FILTER_ALL;
                    binding.chipGroupForumFilter.check(R.id.chip_forum_all);
                }
                binding.rvForumPosts.smoothScrollToPosition(0);
                Toast.makeText(requireContext(), R.string.forum_post_published, Toast.LENGTH_SHORT).show();
                return;
            }
            onActionResult(result);
        });
    }

    private void clearComposeDraft() {
        draftTitle = "";
        draftContent = "";
        draftImageUris.clear();
    }

    private void syncDraftToComposeSheet() {
        if (composeBinding == null) {
            return;
        }
        if (!draftTitle.equals(textOf(composeBinding.etForumSheetTitle))) {
            composeBinding.etForumSheetTitle.setText(draftTitle);
            composeBinding.etForumSheetTitle.setSelection(composeBinding.etForumSheetTitle.length());
        }
        if (!draftContent.equals(textOf(composeBinding.etForumSheetContent))) {
            composeBinding.etForumSheetContent.setText(draftContent);
            composeBinding.etForumSheetContent.setSelection(composeBinding.etForumSheetContent.length());
        }
    }

    private void syncDraftFromComposeSheet() {
        if (composeBinding == null) {
            return;
        }
        draftTitle = textOf(composeBinding.etForumSheetTitle);
        draftContent = textOf(composeBinding.etForumSheetContent);
    }

    private void onImagesPicked(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            return;
        }
        LinkedHashSet<String> merged = new LinkedHashSet<>(draftImageUris);
        for (Uri uri : uris) {
            if (uri != null) {
                merged.add(uri.toString());
            }
        }

        List<String> limited = new ArrayList<>(MAX_COMPOSE_IMAGES);
        for (String item : merged) {
            limited.add(item);
            if (limited.size() >= MAX_COMPOSE_IMAGES) {
                break;
            }
        }

        boolean truncated = merged.size() > limited.size();
        draftImageUris.clear();
        draftImageUris.addAll(limited);
        refreshComposeImagesUi();

        if (truncated && isAdded()) {
            Toast.makeText(requireContext(), R.string.forum_image_limit_reached, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshComposeImagesUi() {
        if (composeBinding == null || composeImageAdapter == null) {
            return;
        }
        composeImageAdapter.submitList(new ArrayList<>(draftImageUris));
        composeBinding.tvForumComposeImageCount.setText(getString(
                R.string.forum_image_count_format,
                draftImageUris.size(),
                MAX_COMPOSE_IMAGES
        ));
        composeBinding.rvForumComposeImages.setVisibility(draftImageUris.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateComposePublishState() {
        if (composeBinding == null) {
            return;
        }
        boolean canPublish = !publishingPost
                && currentAuthState != null
                && currentAuthState.loggedIn
                && !draftContent.trim().isEmpty();
        composeBinding.btnForumSheetPublish.setEnabled(canPublish);
        composeBinding.btnForumSheetPublish.setAlpha(canPublish ? 1f : 0.65f);
        composeBinding.btnForumComposeAddImage.setEnabled(!publishingPost);
    }

    private void onActionResult(ForumActionResult result) {
        if (!isAdded()) {
            return;
        }
        if (result == ForumActionResult.SUCCESS) {
            return;
        }
        int messageRes = ForumViewModel.errorMessageRes(result);
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show();
    }

    private void openPostDetail(long postId) {
        Intent intent = new Intent(requireContext(), ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRA_POST_ID, postId);
        startActivity(intent);
    }

    private void openImagePreview(String imageUri) {
        Intent intent = new Intent(requireContext(), ImagePreviewActivity.class);
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_URI, imageUri);
        startActivity(intent);
    }

    private String textOf(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (composeDialog != null && composeDialog.isShowing()) {
            composeDialog.dismiss();
        }
        composeDialog = null;
        composeBinding = null;
        composeImageAdapter = null;
        binding = null;
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
