package com.musclefit.app.repo;

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;

import androidx.lifecycle.LiveData;

import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthRole;
import com.musclefit.app.auth.AuthState;
import com.musclefit.app.data.db.AppDatabase;
import com.musclefit.app.data.db.ForumCommentEntity;
import com.musclefit.app.data.db.ForumDao;
import com.musclefit.app.data.db.ForumPostFavoriteEntity;
import com.musclefit.app.data.db.ForumPostEntity;
import com.musclefit.app.data.db.ForumPostLikeEntity;
import com.musclefit.app.data.db.ForumReportEntity;
import com.musclefit.app.data.model.ForumCommentItem;
import com.musclefit.app.data.model.ForumPostCard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForumRepository {
    public interface ActionCallback {
        void onResult(ForumActionResult result);
    }

    private static volatile ForumRepository INSTANCE;
    private static final int MAX_POST_IMAGES = 3;
    private static final String FORUM_MEDIA_DIR = "forum_media";

    private final Context appContext;
    private final AppDatabase db;
    private final ForumDao forumDao;
    private final AuthManager authManager;
    private final ExecutorService ioExecutor;
    private final Handler mainHandler;
    private final InteractionToggleGuard actionGuard;

    private ForumRepository(Context context) {
        appContext = context.getApplicationContext();
        db = AppDatabase.getInstance(appContext);
        forumDao = db.forumDao();
        authManager = AuthManager.getInstance(appContext);
        ioExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        actionGuard = new InteractionToggleGuard(600L);
    }

    public static ForumRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ForumRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ForumRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<ForumPostCard>> observePosts(String accountId) {
        return forumDao.observePosts(normalizeScopeAccountId(accountId));
    }

    public LiveData<ForumPostCard> observePost(long postId, String accountId) {
        return forumDao.observePost(postId, normalizeScopeAccountId(accountId));
    }

    public LiveData<List<ForumCommentItem>> observeComments(long postId) {
        return forumDao.observeComments(postId);
    }

    public void createPost(String title, String content, ActionCallback callback) {
        createPost(title, content, null, callback);
    }

    public void createPost(String title, String content, List<String> selectedImageUris, ActionCallback callback) {
        if (!authManager.isLoggedIn()) {
            dispatch(callback, ForumActionResult.LOGIN_REQUIRED);
            return;
        }

        String safeContent = safeText(content);
        if (safeContent.isEmpty()) {
            dispatch(callback, ForumActionResult.EMPTY_INPUT);
            return;
        }
        String safeTitle = normalizePostTitle(title, safeContent);
        List<String> normalizedImageUris = normalizeImageUriList(selectedImageUris);

        String accountId = currentAccountId();
        String key = "POST_CREATE:" + accountId;
        ForumActionResult acquired = acquireGuard(key);
        if (acquired != ForumActionResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ForumActionResult result = ForumActionResult.SUCCESS;
            List<String> persistedImageUris = new ArrayList<>();
            try {
                persistedImageUris = persistPostImages(normalizedImageUris);
                if (!normalizedImageUris.isEmpty() && persistedImageUris.isEmpty()) {
                    throw new IllegalStateException("image_copy_failed");
                }

                ForumPostEntity post = new ForumPostEntity();
                post.authorAccountId = accountId;
                post.authorNickname = currentNicknameOrFallback();
                post.title = safeTitle;
                post.content = safeContent;
                post.imageUris = ForumImageCodec.encode(persistedImageUris);
                post.likeCount = 0;
                post.commentCount = 0;
                post.favoriteCount = 0;
                long now = System.currentTimeMillis();
                post.createdAt = now;
                post.updatedAt = now;
                forumDao.insertPost(post);
            } catch (Exception e) {
                deletePostImages(persistedImageUris);
                result = ForumActionResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public void updatePost(long postId, String title, String content, ActionCallback callback) {
        if (!authManager.isLoggedIn()) {
            dispatch(callback, ForumActionResult.LOGIN_REQUIRED);
            return;
        }

        String safeContent = safeText(content);
        if (safeContent.isEmpty()) {
            dispatch(callback, ForumActionResult.EMPTY_INPUT);
            return;
        }
        String safeTitle = normalizePostTitle(title, safeContent);

        String accountId = currentAccountId();
        String key = "POST_EDIT:" + accountId + ":" + postId;
        ForumActionResult acquired = acquireGuard(key);
        if (acquired != ForumActionResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ForumActionResult result = ForumActionResult.SUCCESS;
            try {
                db.runInTransaction(() -> {
                    ForumPostEntity post = forumDao.getPostByIdSync(postId);
                    if (post == null) {
                        throw new IllegalStateException("post_not_found");
                    }
                    if (!canManagePost(post, accountId, authManager.getCurrent())) {
                        throw new SecurityException("permission_denied");
                    }
                    int updated = forumDao.updatePostContent(postId, safeTitle, safeContent, System.currentTimeMillis());
                    if (updated <= 0) {
                        throw new IllegalStateException("post_not_found");
                    }
                });
            } catch (SecurityException denied) {
                result = ForumActionResult.PERMISSION_DENIED;
            } catch (IllegalStateException notFound) {
                result = ForumActionResult.NOT_FOUND;
            } catch (Exception e) {
                result = ForumActionResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public void deletePost(long postId, ActionCallback callback) {
        if (!authManager.isLoggedIn()) {
            dispatch(callback, ForumActionResult.LOGIN_REQUIRED);
            return;
        }

        String accountId = currentAccountId();
        String key = "POST_DELETE:" + accountId + ":" + postId;
        ForumActionResult acquired = acquireGuard(key);
        if (acquired != ForumActionResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ForumActionResult result = ForumActionResult.SUCCESS;
            final String[] removedImageUris = new String[1];
            try {
                db.runInTransaction(() -> {
                    ForumPostEntity post = forumDao.getPostByIdSync(postId);
                    if (post == null) {
                        throw new IllegalStateException("post_not_found");
                    }
                    if (!canManagePost(post, accountId, authManager.getCurrent())) {
                        throw new SecurityException("permission_denied");
                    }
                    removedImageUris[0] = post.imageUris;
                    int deleted = forumDao.deletePostById(postId);
                    if (deleted <= 0) {
                        throw new IllegalStateException("post_not_found");
                    }
                });
                deletePostImages(removedImageUris[0]);
            } catch (SecurityException denied) {
                result = ForumActionResult.PERMISSION_DENIED;
            } catch (IllegalStateException notFound) {
                result = ForumActionResult.NOT_FOUND;
            } catch (Exception e) {
                result = ForumActionResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public void toggleLike(long postId, ActionCallback callback) {
        if (!authManager.isLoggedIn()) {
            dispatch(callback, ForumActionResult.LOGIN_REQUIRED);
            return;
        }

        String accountId = currentAccountId();
        String key = "POST_LIKE:" + accountId + ":" + postId;
        ForumActionResult acquired = acquireGuard(key);
        if (acquired != ForumActionResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ForumActionResult result = ForumActionResult.SUCCESS;
            try {
                db.runInTransaction(() -> {
                    ForumPostEntity post = forumDao.getPostByIdSync(postId);
                    if (post == null) {
                        throw new IllegalStateException("post_not_found");
                    }

                    ForumPostLikeEntity like = forumDao.getLikeSync(postId, accountId);
                    long now = System.currentTimeMillis();
                    if (like == null) {
                        ForumPostLikeEntity next = new ForumPostLikeEntity();
                        next.postId = postId;
                        next.accountId = accountId;
                        next.createdAt = now;
                        forumDao.insertLike(next);
                        post.likeCount = post.likeCount + 1;
                    } else {
                        forumDao.deleteLike(postId, accountId);
                        post.likeCount = Math.max(0, post.likeCount - 1);
                    }
                    forumDao.updatePostLikeCount(postId, post.likeCount);
                });
            } catch (IllegalStateException notFound) {
                result = ForumActionResult.NOT_FOUND;
            } catch (Exception e) {
                result = ForumActionResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public void toggleFavorite(long postId, ActionCallback callback) {
        if (!authManager.isLoggedIn()) {
            dispatch(callback, ForumActionResult.LOGIN_REQUIRED);
            return;
        }

        String accountId = currentAccountId();
        String key = "POST_FAVORITE:" + accountId + ":" + postId;
        ForumActionResult acquired = acquireGuard(key);
        if (acquired != ForumActionResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ForumActionResult result = ForumActionResult.SUCCESS;
            try {
                db.runInTransaction(() -> {
                    ForumPostEntity post = forumDao.getPostByIdSync(postId);
                    if (post == null) {
                        throw new IllegalStateException("post_not_found");
                    }

                    ForumPostFavoriteEntity favorite = forumDao.getFavoriteSync(postId, accountId);
                    long now = System.currentTimeMillis();
                    if (favorite == null) {
                        ForumPostFavoriteEntity next = new ForumPostFavoriteEntity();
                        next.postId = postId;
                        next.accountId = accountId;
                        next.createdAt = now;
                        forumDao.insertFavorite(next);
                        post.favoriteCount = post.favoriteCount + 1;
                    } else {
                        forumDao.deleteFavorite(postId, accountId);
                        post.favoriteCount = Math.max(0, post.favoriteCount - 1);
                    }
                    forumDao.updatePostFavoriteCount(postId, post.favoriteCount);
                });
            } catch (IllegalStateException notFound) {
                result = ForumActionResult.NOT_FOUND;
            } catch (Exception e) {
                result = ForumActionResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public void addComment(long postId, String content, ActionCallback callback) {
        if (!authManager.isLoggedIn()) {
            dispatch(callback, ForumActionResult.LOGIN_REQUIRED);
            return;
        }

        String safeContent = safeText(content);
        if (safeContent.isEmpty()) {
            dispatch(callback, ForumActionResult.EMPTY_INPUT);
            return;
        }

        String accountId = currentAccountId();
        String key = "POST_COMMENT:" + accountId + ":" + postId;
        ForumActionResult acquired = acquireGuard(key);
        if (acquired != ForumActionResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ForumActionResult result = ForumActionResult.SUCCESS;
            try {
                db.runInTransaction(() -> {
                    ForumPostEntity post = forumDao.getPostByIdSync(postId);
                    if (post == null) {
                        throw new IllegalStateException("post_not_found");
                    }

                    ForumCommentEntity comment = new ForumCommentEntity();
                    comment.postId = postId;
                    comment.authorAccountId = accountId;
                    comment.authorNickname = currentNicknameOrFallback();
                    comment.content = safeContent;
                    comment.createdAt = System.currentTimeMillis();
                    forumDao.insertComment(comment);

                    post.commentCount = post.commentCount + 1;
                    forumDao.updatePostCommentCount(postId, post.commentCount, System.currentTimeMillis());
                });
            } catch (IllegalStateException notFound) {
                result = ForumActionResult.NOT_FOUND;
            } catch (Exception e) {
                result = ForumActionResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public void deleteComment(long commentId, ActionCallback callback) {
        if (!authManager.isLoggedIn()) {
            dispatch(callback, ForumActionResult.LOGIN_REQUIRED);
            return;
        }

        String accountId = currentAccountId();
        String key = "COMMENT_DELETE:" + accountId + ":" + commentId;
        ForumActionResult acquired = acquireGuard(key);
        if (acquired != ForumActionResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ForumActionResult result = ForumActionResult.SUCCESS;
            try {
                db.runInTransaction(() -> {
                    ForumCommentEntity comment = forumDao.getCommentByIdSync(commentId);
                    if (comment == null) {
                        throw new IllegalStateException("comment_not_found");
                    }
                    ForumPostEntity post = forumDao.getPostByIdSync(comment.postId);
                    if (post == null) {
                        throw new IllegalStateException("post_not_found");
                    }
                    if (!canDeleteComment(comment, post, accountId, authManager.getCurrent())) {
                        throw new SecurityException("permission_denied");
                    }
                    int deleted = forumDao.deleteCommentById(commentId);
                    if (deleted <= 0) {
                        throw new IllegalStateException("comment_not_found");
                    }
                    int count = forumDao.countCommentsByPostId(post.id);
                    forumDao.updatePostCommentCount(post.id, count, System.currentTimeMillis());
                });
            } catch (SecurityException denied) {
                result = ForumActionResult.PERMISSION_DENIED;
            } catch (IllegalStateException notFound) {
                result = ForumActionResult.NOT_FOUND;
            } catch (Exception e) {
                result = ForumActionResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public void reportPost(long postId, String reason, ActionCallback callback) {
        if (!authManager.isLoggedIn()) {
            dispatch(callback, ForumActionResult.LOGIN_REQUIRED);
            return;
        }

        String safeReason = safeText(reason);
        if (safeReason.isEmpty()) {
            safeReason = "其他";
        }

        String accountId = currentAccountId();
        String key = "POST_REPORT:" + accountId + ":" + postId;
        ForumActionResult acquired = acquireGuard(key);
        if (acquired != ForumActionResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        final String finalReason = safeReason;
        ioExecutor.execute(() -> {
            ForumActionResult result = ForumActionResult.SUCCESS;
            try {
                db.runInTransaction(() -> {
                    ForumPostEntity post = forumDao.getPostByIdSync(postId);
                    if (post == null) {
                        throw new IllegalStateException("post_not_found");
                    }
                    if (accountId.equals(post.authorAccountId) && !isAdmin(authManager.getCurrent())) {
                        throw new SecurityException("permission_denied");
                    }
                    ForumReportEntity report = new ForumReportEntity();
                    report.postId = postId;
                    report.reporterAccountId = accountId;
                    report.reason = finalReason;
                    report.status = "OPEN";
                    report.createdAt = System.currentTimeMillis();
                    forumDao.insertReport(report);
                });
            } catch (SecurityException denied) {
                result = ForumActionResult.PERMISSION_DENIED;
            } catch (IllegalStateException notFound) {
                result = ForumActionResult.NOT_FOUND;
            } catch (Exception e) {
                result = ForumActionResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public String currentScopeAccountId() {
        return currentAccountId();
    }

    private List<String> persistPostImages(List<String> imageUris) throws Exception {
        List<String> normalized = normalizeImageUriList(imageUris);
        if (normalized.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> persisted = new ArrayList<>(normalized.size());
        for (String uriString : normalized) {
            Uri sourceUri = Uri.parse(uriString);
            File copied = copyUriToPrivateFile(sourceUri);
            if (copied != null) {
                persisted.add(Uri.fromFile(copied).toString());
            }
        }
        return persisted;
    }

    private File copyUriToPrivateFile(Uri sourceUri) throws Exception {
        ContentResolver resolver = appContext.getContentResolver();
        String mimeType = resolver.getType(sourceUri);
        File mediaDir = ensureForumMediaDir();
        String extension = inferExtension(sourceUri, mimeType);
        String name = "forum_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().replace("-", "") + "." + extension;
        File outputFile = new File(mediaDir, name);

        try (InputStream inputStream = resolver.openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            if (inputStream == null) {
                throw new IllegalStateException("image_stream_null");
            }
            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return outputFile;
    }

    private File ensureForumMediaDir() {
        File dir = new File(appContext.getFilesDir(), FORUM_MEDIA_DIR);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    private void deletePostImages(String imageUrisJson) {
        deletePostImages(ForumImageCodec.decode(imageUrisJson));
    }

    private void deletePostImages(List<String> imageUris) {
        if (imageUris == null || imageUris.isEmpty()) {
            return;
        }
        for (String imageUri : imageUris) {
            if (imageUri == null || imageUri.trim().isEmpty()) {
                continue;
            }
            Uri uri = Uri.parse(imageUri);
            if (uri == null || uri.getPath() == null) {
                continue;
            }
            if (!"file".equalsIgnoreCase(uri.getScheme())) {
                continue;
            }
            File file = new File(uri.getPath());
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    private static List<String> normalizeImageUriList(List<String> imageUris) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        if (imageUris != null) {
            for (String item : imageUris) {
                if (item == null) {
                    continue;
                }
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    unique.add(trimmed);
                }
            }
        }
        List<String> normalized = new ArrayList<>(Math.min(unique.size(), MAX_POST_IMAGES));
        for (String item : unique) {
            normalized.add(item);
            if (normalized.size() >= MAX_POST_IMAGES) {
                break;
            }
        }
        return normalized;
    }

    private static String inferExtension(Uri uri, String mimeType) {
        String ext = "";
        if (mimeType != null) {
            ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }
        if (ext == null || ext.trim().isEmpty()) {
            String lastPath = uri == null ? null : uri.getLastPathSegment();
            if (lastPath != null) {
                int dot = lastPath.lastIndexOf('.');
                if (dot >= 0 && dot < lastPath.length() - 1) {
                    ext = lastPath.substring(dot + 1);
                }
            }
        }
        if (ext == null || ext.trim().isEmpty()) {
            return "jpg";
        }
        String normalized = ext.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized.isEmpty() ? "jpg" : normalized;
    }

    private ForumActionResult acquireGuard(String key) {
        ToggleResult acquired = actionGuard.tryAcquire(key, System.currentTimeMillis());
        if (acquired == ToggleResult.SUCCESS) {
            return ForumActionResult.SUCCESS;
        }
        if (acquired == ToggleResult.TOO_FAST) {
            return ForumActionResult.TOO_FAST;
        }
        if (acquired == ToggleResult.BUSY) {
            return ForumActionResult.BUSY;
        }
        return ForumActionResult.ERROR;
    }

    private void dispatch(ActionCallback callback, ForumActionResult result) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onResult(result));
    }

    private String currentAccountId() {
        AuthState state = authManager.getCurrent();
        if (state == null || !state.loggedIn) {
            return "guest";
        }
        if (state.accountId == null || state.accountId.trim().isEmpty()) {
            return "guest";
        }
        return state.accountId.trim().toLowerCase(Locale.ROOT);
    }

    private String currentNicknameOrFallback() {
        AuthState state = authManager.getCurrent();
        if (state == null) {
            return "";
        }
        if (state.nickname != null && !state.nickname.trim().isEmpty()) {
            return state.nickname.trim();
        }
        if (state.accountId != null) {
            return state.accountId;
        }
        return "";
    }

    private static boolean canManagePost(ForumPostEntity post, String accountId, AuthState state) {
        if (post == null || accountId == null) {
            return false;
        }
        return accountId.equals(post.authorAccountId) || isAdmin(state);
    }

    private static boolean canDeleteComment(ForumCommentEntity comment, ForumPostEntity post, String accountId, AuthState state) {
        if (comment == null || post == null || accountId == null) {
            return false;
        }
        if (isAdmin(state)) {
            return true;
        }
        if (accountId.equals(comment.authorAccountId)) {
            return true;
        }
        return accountId.equals(post.authorAccountId);
    }

    private static boolean isAdmin(AuthState state) {
        return state != null && state.role == AuthRole.ADMIN;
    }

    private static String safeText(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }

    private static String normalizePostTitle(String rawTitle, String safeContent) {
        String title = safeText(rawTitle);
        if (!title.isEmpty()) {
            return clipTitle(title);
        }
        if (safeContent.isEmpty()) {
            return "";
        }

        String compact = safeContent.replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        if (compact.isEmpty()) {
            return "";
        }
        return clipTitle(compact);
    }

    private static String clipTitle(String raw) {
        final int maxChars = 18;
        if (raw.length() <= maxChars) {
            return raw;
        }
        return raw.substring(0, maxChars) + "…";
    }

    private static String normalizeScopeAccountId(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return "guest";
        }
        return accountId.trim().toLowerCase(Locale.ROOT);
    }
}
