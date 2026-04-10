package com.musclefit.app.ui.forum;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.musclefit.app.R;
import com.musclefit.app.data.model.ForumCommentItem;
import com.musclefit.app.data.model.ForumPostCard;
import com.musclefit.app.repo.ForumActionResult;
import com.musclefit.app.repo.ForumRepository;

import java.util.ArrayList;
import java.util.List;

public class ForumViewModel extends AndroidViewModel {
    private final ForumRepository repository;
    private final MutableLiveData<String> accountScope = new MutableLiveData<>("guest");
    private final LiveData<List<ForumPostCard>> posts;

    public ForumViewModel(@NonNull Application application) {
        super(application);
        repository = ForumRepository.getInstance(application);
        posts = Transformations.switchMap(accountScope, repository::observePosts);
    }

    public LiveData<List<ForumPostCard>> getPosts() {
        return posts;
    }

    public LiveData<ForumPostCard> observePost(long postId) {
        return Transformations.switchMap(accountScope, accountId -> repository.observePost(postId, accountId));
    }

    public LiveData<List<ForumCommentItem>> observeComments(long postId) {
        return repository.observeComments(postId);
    }

    public void setAccountScope(String accountId) {
        String next = accountId == null || accountId.trim().isEmpty() ? "guest" : accountId.trim();
        String current = accountScope.getValue();
        if (next.equals(current)) {
            return;
        }
        accountScope.setValue(next);
    }

    public String currentScopeAccountId() {
        String current = accountScope.getValue();
        return current == null ? "guest" : current;
    }

    public void createPost(String title, String content, ForumRepository.ActionCallback callback) {
        repository.createPost(title, content, callback);
    }

    public void createPost(String title, String content, List<String> imageUris, ForumRepository.ActionCallback callback) {
        List<String> safeUris = imageUris == null ? new ArrayList<>() : imageUris;
        repository.createPost(title, content, safeUris, callback);
    }

    public void updatePost(long postId, String title, String content, ForumRepository.ActionCallback callback) {
        repository.updatePost(postId, title, content, callback);
    }

    public void deletePost(long postId, ForumRepository.ActionCallback callback) {
        repository.deletePost(postId, callback);
    }

    public void toggleLike(long postId, ForumRepository.ActionCallback callback) {
        repository.toggleLike(postId, callback);
    }

    public void toggleFavorite(long postId, ForumRepository.ActionCallback callback) {
        repository.toggleFavorite(postId, callback);
    }

    public void addComment(long postId, String content, ForumRepository.ActionCallback callback) {
        repository.addComment(postId, content, callback);
    }

    public void deleteComment(long commentId, ForumRepository.ActionCallback callback) {
        repository.deleteComment(commentId, callback);
    }

    public void reportPost(long postId, String reason, ForumRepository.ActionCallback callback) {
        repository.reportPost(postId, reason, callback);
    }

    public static int errorMessageRes(ForumActionResult result) {
        if (result == ForumActionResult.LOGIN_REQUIRED) {
            return R.string.login_required_action;
        }
        if (result == ForumActionResult.EMPTY_INPUT) {
            return R.string.forum_empty_input;
        }
        if (result == ForumActionResult.TOO_FAST) {
            return R.string.action_too_fast;
        }
        if (result == ForumActionResult.BUSY) {
            return R.string.action_processing;
        }
        if (result == ForumActionResult.NOT_FOUND) {
            return R.string.forum_post_not_found;
        }
        if (result == ForumActionResult.PERMISSION_DENIED) {
            return R.string.forum_permission_denied;
        }
        return R.string.action_failed;
    }
}
