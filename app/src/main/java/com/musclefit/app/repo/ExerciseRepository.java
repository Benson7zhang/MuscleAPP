package com.musclefit.app.repo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.data.db.AppDatabase;
import com.musclefit.app.data.db.ExerciseDao;
import com.musclefit.app.data.db.ExerciseEntity;
import com.musclefit.app.data.db.ExerciseInteractionDao;
import com.musclefit.app.data.db.ExerciseInteractionEntity;
import com.musclefit.app.data.db.ExerciseMuscleIntensityDao;
import com.musclefit.app.data.model.ExerciseCard;
import com.musclefit.app.data.model.ExerciseIntensityNote;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExerciseRepository {
    public interface ToggleCallback {
        void onResult(ToggleResult result);
    }

    private static volatile ExerciseRepository INSTANCE;

    private final AppDatabase db;
    private final ExerciseDao exerciseDao;
    private final ExerciseInteractionDao interactionDao;
    private final ExerciseMuscleIntensityDao intensityDao;
    private final ExecutorService ioExecutor;
    private final Handler mainHandler;
    private final InteractionToggleGuard toggleGuard;
    private final AuthManager authManager;

    private ExerciseRepository(Context context) {
        db = AppDatabase.getInstance(context);
        exerciseDao = db.exerciseDao();
        interactionDao = db.interactionDao();
        intensityDao = db.intensityDao();
        ioExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        toggleGuard = new InteractionToggleGuard(600L);
        authManager = AuthManager.getInstance(context);
    }

    public static ExerciseRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ExerciseRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExerciseRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<ExerciseCard>> observeExercises(String keyword, String category) {
        return exerciseDao.observeExercises(normalize(keyword), normalizeCategory(category), currentAccountId());
    }

    public LiveData<List<ExerciseCard>> observeFavoriteExercises(String keyword, String category) {
        return exerciseDao.observeFavoriteExercises(normalize(keyword), normalizeCategory(category), currentAccountId());
    }

    public LiveData<ExerciseCard> observeExerciseById(long exerciseId) {
        return exerciseDao.observeExerciseById(exerciseId, currentAccountId());
    }

    public LiveData<List<ExerciseIntensityNote>> observeIntensityNotes(long exerciseId) {
        return intensityDao.observeNotes(exerciseId);
    }

    public void toggleLike(long exerciseId, ToggleCallback callback) {
        toggleInteraction(exerciseId, true, callback);
    }

    public void toggleFavorite(long exerciseId, ToggleCallback callback) {
        toggleInteraction(exerciseId, false, callback);
    }

    private void toggleInteraction(long exerciseId, boolean forLike, ToggleCallback callback) {
        String accountId = currentAccountId();
        String key = (forLike ? "LIKE:" : "FAVORITE:") + accountId + ":" + exerciseId;
        long now = System.currentTimeMillis();
        ToggleResult acquired = toggleGuard.tryAcquire(key, now);
        if (acquired != ToggleResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ToggleResult result = ToggleResult.SUCCESS;
            try {
                db.runInTransaction(() -> {
                    ExerciseEntity exercise = exerciseDao.getExerciseSync(exerciseId);
                    if (exercise == null) {
                        throw new IllegalStateException("exercise_not_found");
                    }

                    ExerciseInteractionEntity interaction = interactionDao.getByExerciseIdSync(exerciseId, accountId);
                    if (interaction == null) {
                        interaction = new ExerciseInteractionEntity();
                        interaction.exerciseId = exerciseId;
                        interaction.accountId = accountId;
                        interaction.liked = false;
                        interaction.favorited = false;
                    }

                    if (forLike) {
                        boolean next = !interaction.liked;
                        exercise.likeCount = adjustCount(exercise.likeCount, next);
                        interaction.liked = next;
                    } else {
                        boolean next = !interaction.favorited;
                        exercise.favoriteCount = adjustCount(exercise.favoriteCount, next);
                        interaction.favorited = next;
                    }

                    interaction.updatedAt = System.currentTimeMillis();
                    exerciseDao.updateCounts(exerciseId, exercise.likeCount, exercise.favoriteCount);
                    interactionDao.upsert(interaction);
                });
            } catch (IllegalStateException missing) {
                result = ToggleResult.NOT_FOUND;
            } catch (Exception e) {
                result = ToggleResult.ERROR;
            } finally {
                toggleGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    static int adjustCount(int currentCount, boolean toggledOn) {
        if (toggledOn) {
            return currentCount + 1;
        }
        return Math.max(0, currentCount - 1);
    }

    private void dispatch(ToggleCallback callback, ToggleResult result) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onResult(result));
    }

    private String normalize(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }

    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "ALL";
        }
        return category;
    }

    private String currentAccountId() {
        if (!authManager.isLoggedIn()) {
            return "guest";
        }
        String username = authManager.getCurrent().username;
        if (username == null) {
            return "guest";
        }
        String trimmed = username.trim();
        if (trimmed.isEmpty()) {
            return "guest";
        }
        return trimmed.toLowerCase();
    }
}
