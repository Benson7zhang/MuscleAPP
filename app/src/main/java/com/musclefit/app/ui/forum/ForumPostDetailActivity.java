package com.musclefit.app.ui.forum;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthRole;
import com.musclefit.app.auth.AuthState;
import com.musclefit.app.data.model.ForumCommentItem;
import com.musclefit.app.data.model.ForumPostCard;
import com.musclefit.app.databinding.ActivityForumPostDetailBinding;
import com.musclefit.app.databinding.DialogForumEditPostBinding;
import com.musclefit.app.repo.ForumActionResult;
import com.musclefit.app.repo.ForumImageCodec;
import com.musclefit.app.ui.exercise.ImagePreviewActivity;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForumPostDetailActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "extra_post_id";

    private ActivityForumPostDetailBinding binding;
    private ForumViewModel viewModel;
    private AuthManager authManager;
    private ForumCommentAdapter commentAdapter;
    private ForumPostImageAdapter imageAdapter;
    private long postId;

    private AuthState currentAuthState;
    private ForumPostCard currentPost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForumPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postId = getIntent().getLongExtra(EXTRA_POST_ID, -1L);
        if (postId <= 0L) {
            Toast.makeText(this, R.string.forum_post_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ForumViewModel.class);
        authManager = AuthManager.getInstance(this);

        setupViews();
        observeData();

        authManager.observe().observe(this, this::renderAuthState);
        renderAuthState(authManager.getCurrent());
    }

    private void setupViews() {
        binding.btnForumDetailBack.setOnClickListener(v -> finish());

        commentAdapter = new ForumCommentAdapter(new ForumCommentAdapter.Listener() {
            @Override
            public boolean canDelete(ForumCommentItem comment) {
                return canDeleteComment(comment);
            }

            @Override
            public void onDeleteClick(ForumCommentItem comment) {
                confirmDeleteComment(comment);
            }
        });
        binding.rvForumComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvForumComments.setAdapter(commentAdapter);

        imageAdapter = new ForumPostImageAdapter(this::openImagePreview);
        binding.rvForumDetailImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvForumDetailImages.setAdapter(imageAdapter);

        binding.btnForumDetailLike.setOnClickListener(v -> viewModel.toggleLike(postId, this::onActionResult));
        binding.btnForumDetailFavorite.setOnClickListener(v -> viewModel.toggleFavorite(postId, this::onActionResult));
        binding.btnForumDetailEdit.setOnClickListener(v -> showEditPostDialog());
        binding.btnForumDetailDelete.setOnClickListener(v -> confirmDeletePost());
        binding.btnForumDetailReport.setOnClickListener(v -> showReportDialog());

        binding.btnForumSendComment.setOnClickListener(v -> {
            String content = textOf(binding.etForumComment);
            viewModel.addComment(postId, content, result -> {
                if (result == ForumActionResult.SUCCESS) {
                    binding.etForumComment.setText("");
                    Toast.makeText(this, R.string.forum_comment_sent, Toast.LENGTH_SHORT).show();
                    return;
                }
                onActionResult(result);
            });
        });
    }

    private void observeData() {
        viewModel.observePost(postId).observe(this, this::renderPost);
        viewModel.observeComments(postId).observe(this, comments -> {
            commentAdapter.submitList(comments);
            binding.tvForumDetailCommentCount.setText(getString(R.string.forum_comment_count, comments == null ? 0 : comments.size()));
        });
    }

    private void renderPost(ForumPostCard post) {
        if (post == null) {
            Toast.makeText(this, R.string.forum_post_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentPost = post;

        binding.tvForumDetailPostTitle.setText(post.title);
        binding.tvForumDetailPostContent.setText(post.content);
        binding.tvForumDetailPostMeta.setText(getString(
                R.string.forum_post_meta,
                post.authorAccountId,
                formatTime(post.updatedAt)
        ));
        List<String> imageUris = ForumImageCodec.decode(post.imageUris);
        if (imageUris == null) {
            imageUris = new ArrayList<>();
        }
        imageAdapter.submitList(imageUris);
        binding.rvForumDetailImages.setVisibility(imageUris.isEmpty() ? View.GONE : View.VISIBLE);

        int likeLabel = post.likedByMe ? R.string.forum_liked_count : R.string.forum_like_count;
        binding.btnForumDetailLike.setText(getString(likeLabel, post.likeCount));
        int favoriteLabel = post.favoritedByMe ? R.string.forum_favorited_count : R.string.forum_favorite_count;
        binding.btnForumDetailFavorite.setText(getString(favoriteLabel, post.favoriteCount));
        int activeColor = getColor(R.color.mf_primary);
        int normalColor = getColor(R.color.mf_muted);
        binding.btnForumDetailLike.setTextColor(post.likedByMe ? activeColor : normalColor);
        binding.btnForumDetailFavorite.setTextColor(post.favoritedByMe ? activeColor : normalColor);

        updatePostActionButtons();
    }

    private void renderAuthState(AuthState state) {
        currentAuthState = state;
        viewModel.setAccountScope(state.loggedIn ? state.accountId : "guest");

        boolean canInteract = state.loggedIn;
        binding.tvForumDetailLoginTip.setVisibility(canInteract ? View.GONE : View.VISIBLE);
        binding.etForumComment.setEnabled(canInteract);
        binding.btnForumSendComment.setEnabled(canInteract);
        binding.btnForumSendComment.setAlpha(canInteract ? 1f : 0.65f);
        binding.btnForumDetailLike.setEnabled(canInteract);
        binding.btnForumDetailLike.setAlpha(canInteract ? 1f : 0.75f);
        binding.btnForumDetailFavorite.setEnabled(canInteract);
        binding.btnForumDetailFavorite.setAlpha(canInteract ? 1f : 0.75f);

        updatePostActionButtons();
        commentAdapter.notifyDataSetChanged();
    }

    private void updatePostActionButtons() {
        if (currentPost == null || currentAuthState == null || !currentAuthState.loggedIn) {
            binding.btnForumDetailEdit.setVisibility(View.GONE);
            binding.btnForumDetailDelete.setVisibility(View.GONE);
            binding.btnForumDetailReport.setVisibility(View.GONE);
            return;
        }

        boolean canManage = canManageCurrentPost();
        boolean canReport = canReportCurrentPost();
        binding.btnForumDetailEdit.setVisibility(canManage ? View.VISIBLE : View.GONE);
        binding.btnForumDetailDelete.setVisibility(canManage ? View.VISIBLE : View.GONE);
        binding.btnForumDetailReport.setVisibility(canReport ? View.VISIBLE : View.GONE);
    }

    private boolean canManageCurrentPost() {
        if (currentPost == null || currentAuthState == null || !currentAuthState.loggedIn) {
            return false;
        }
        if (isAdmin(currentAuthState)) {
            return true;
        }
        return currentAuthState.accountId != null && currentAuthState.accountId.equals(currentPost.authorAccountId);
    }

    private boolean canReportCurrentPost() {
        if (currentPost == null || currentAuthState == null || !currentAuthState.loggedIn) {
            return false;
        }
        if (isAdmin(currentAuthState)) {
            return false;
        }
        return currentAuthState.accountId != null && !currentAuthState.accountId.equals(currentPost.authorAccountId);
    }

    private boolean canDeleteComment(ForumCommentItem comment) {
        if (comment == null || currentAuthState == null || currentPost == null || !currentAuthState.loggedIn) {
            return false;
        }
        if (isAdmin(currentAuthState)) {
            return true;
        }
        if (currentAuthState.accountId == null) {
            return false;
        }
        if (currentAuthState.accountId.equals(comment.authorAccountId)) {
            return true;
        }
        return currentAuthState.accountId.equals(currentPost.authorAccountId);
    }

    private void showEditPostDialog() {
        if (!canManageCurrentPost()) {
            Toast.makeText(this, R.string.forum_permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        DialogForumEditPostBinding dialogBinding = DialogForumEditPostBinding.inflate(LayoutInflater.from(this));
        dialogBinding.etDialogForumTitle.setText(currentPost.title);
        dialogBinding.etDialogForumContent.setText(currentPost.content);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.forum_edit)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.forum_cancel, null)
                .setPositiveButton(R.string.forum_save, (dialog, which) -> {
                    String title = textOf(dialogBinding.etDialogForumTitle);
                    String content = textOf(dialogBinding.etDialogForumContent);
                    viewModel.updatePost(postId, title, content, result -> {
                        if (result == ForumActionResult.SUCCESS) {
                            Toast.makeText(this, R.string.forum_post_updated, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        onActionResult(result);
                    });
                })
                .show();
    }

    private void confirmDeletePost() {
        if (!canManageCurrentPost()) {
            Toast.makeText(this, R.string.forum_permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.forum_delete_post_confirm)
                .setNegativeButton(R.string.forum_cancel, null)
                .setPositiveButton(R.string.forum_delete, (dialog, which) -> viewModel.deletePost(postId, result -> {
                    if (result == ForumActionResult.SUCCESS) {
                        Toast.makeText(this, R.string.forum_post_deleted, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    onActionResult(result);
                }))
                .show();
    }

    private void confirmDeleteComment(ForumCommentItem comment) {
        if (comment == null) {
            return;
        }
        if (!canDeleteComment(comment)) {
            Toast.makeText(this, R.string.forum_permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.forum_delete_comment_confirm)
                .setNegativeButton(R.string.forum_cancel, null)
                .setPositiveButton(R.string.forum_delete, (dialog, which) -> viewModel.deleteComment(comment.id, result -> {
                    if (result == ForumActionResult.SUCCESS) {
                        Toast.makeText(this, R.string.forum_comment_deleted, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    onActionResult(result);
                }))
                .show();
    }

    private void showReportDialog() {
        if (!canReportCurrentPost()) {
            Toast.makeText(this, R.string.forum_permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] reasons = new String[]{
                getString(R.string.forum_report_reason_spam),
                getString(R.string.forum_report_reason_offensive),
                getString(R.string.forum_report_reason_wrong),
                getString(R.string.forum_report_reason_other)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.forum_report)
                .setItems(reasons, (dialog, which) -> {
                    String reason = reasons[which];
                    viewModel.reportPost(postId, reason, result -> {
                        if (result == ForumActionResult.SUCCESS) {
                            Toast.makeText(this, R.string.forum_report_sent, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        onActionResult(result);
                    });
                })
                .setNegativeButton(R.string.forum_cancel, null)
                .show();
    }

    private void onActionResult(ForumActionResult result) {
        if (result == ForumActionResult.SUCCESS) {
            return;
        }
        int messageRes = ForumViewModel.errorMessageRes(result);
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
    }

    private void openImagePreview(String imageUri) {
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_URI, imageUri);
        startActivity(intent);
    }

    private String textOf(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private static boolean isAdmin(AuthState state) {
        return state != null && state.role == AuthRole.ADMIN;
    }

    private static String formatTime(long epochMs) {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date(epochMs));
    }
}
