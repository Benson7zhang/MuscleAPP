package com.musclefit.app.repo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.musclefit.app.data.db.AppDatabase;
import com.musclefit.app.data.db.CoachBookingDao;
import com.musclefit.app.data.db.CoachBookingEntity;
import com.musclefit.app.ui.coach.CoachBookingStatus;
import com.musclefit.app.ui.coach.CoachItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoachRepository {
    private static volatile CoachRepository INSTANCE;

    private final CoachBookingDao bookingDao;
    private final ExecutorService ioExecutor;
    private final Handler mainHandler;
    private final InteractionToggleGuard actionGuard;

    private CoachRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        bookingDao = db.coachBookingDao();
        ioExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        actionGuard = new InteractionToggleGuard(600L);
    }

    public static CoachRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CoachRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CoachRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<CoachBookingEntity>> observeUserBookings(String username) {
        return bookingDao.observeByUser(normalizeUser(username));
    }

    public LiveData<List<CoachBookingEntity>> observeCoachPendingBookings(String coachName) {
        return bookingDao.observeByStatusAndCoach(CoachBookingStatus.PENDING_COACH, normalizeCoachName(coachName));
    }

    public LiveData<List<CoachBookingEntity>> observeCoachScheduleBookings(String coachName) {
        return bookingDao.observeByStatusesAndCoach(
                CoachBookingStatus.COACH_ACCEPTED,
                CoachBookingStatus.ADMIN_APPROVED,
                normalizeCoachName(coachName)
        );
    }

    public LiveData<List<CoachBookingEntity>> observeAdminReviewBookings() {
        return bookingDao.observeByStatus(CoachBookingStatus.COACH_ACCEPTED);
    }

    public void createBooking(String username, CoachItem coachItem, ExerciseRepository.ToggleCallback callback) {
        if (coachItem == null) {
            dispatch(callback, ToggleResult.ERROR);
            return;
        }

        String user = normalizeUser(username);
        String key = "BOOK:" + user + ":" + coachItem.id;
        long now = System.currentTimeMillis();
        ToggleResult acquired = actionGuard.tryAcquire(key, now);
        if (acquired != ToggleResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ToggleResult result = ToggleResult.SUCCESS;
            try {
                CoachBookingEntity entity = new CoachBookingEntity();
                entity.userName = user;
                entity.coachId = coachItem.id;
                entity.coachName = coachItem.name;
                entity.coachSpecialty = coachItem.specialty;
                entity.courseType = coachItem.courseType;
                entity.status = CoachBookingStatus.PENDING_COACH;
                entity.createdAt = now;
                entity.updatedAt = now;
                bookingDao.insert(entity);
            } catch (Exception e) {
                result = ToggleResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    public void coachAccept(long bookingId, String coachName, ExerciseRepository.ToggleCallback callback) {
        transitionStatus(bookingId, CoachBookingStatus.COACH_ACCEPTED, coachName, callback);
    }

    public void coachReject(long bookingId, String coachName, ExerciseRepository.ToggleCallback callback) {
        transitionStatus(bookingId, CoachBookingStatus.COACH_REJECTED, coachName, callback);
    }

    public void adminApprove(long bookingId, ExerciseRepository.ToggleCallback callback) {
        transitionStatus(bookingId, CoachBookingStatus.ADMIN_APPROVED, null, callback);
    }

    public void adminReject(long bookingId, ExerciseRepository.ToggleCallback callback) {
        transitionStatus(bookingId, CoachBookingStatus.ADMIN_REJECTED, null, callback);
    }

    private void transitionStatus(long bookingId, String targetStatus, String coachName, ExerciseRepository.ToggleCallback callback) {
        String key = "BOOK_STATUS:" + bookingId;
        long now = System.currentTimeMillis();
        ToggleResult acquired = actionGuard.tryAcquire(key, now);
        if (acquired != ToggleResult.SUCCESS) {
            dispatch(callback, acquired);
            return;
        }

        ioExecutor.execute(() -> {
            ToggleResult result = ToggleResult.SUCCESS;
            try {
                CoachBookingEntity current = bookingDao.getByIdSync(bookingId);
                if (current == null) {
                    result = ToggleResult.NOT_FOUND;
                } else if ((CoachBookingStatus.COACH_ACCEPTED.equals(targetStatus)
                        || CoachBookingStatus.COACH_REJECTED.equals(targetStatus))
                        && !canCoachOperate(current, coachName)) {
                    result = ToggleResult.NOT_FOUND;
                } else if (!isTransitionAllowed(current.status, targetStatus)) {
                    result = ToggleResult.ERROR;
                } else {
                    int affected = bookingDao.updateStatus(bookingId, targetStatus, System.currentTimeMillis());
                    if (affected <= 0) {
                        result = ToggleResult.NOT_FOUND;
                    }
                }
            } catch (Exception e) {
                result = ToggleResult.ERROR;
            } finally {
                actionGuard.release(key, System.currentTimeMillis());
            }
            dispatch(callback, result);
        });
    }

    private boolean canCoachOperate(CoachBookingEntity current, String coachName) {
        String normalizedCoach = normalizeCoachName(coachName);
        if (normalizedCoach.isEmpty()) {
            return false;
        }
        return normalizedCoach.equals(normalizeCoachName(current.coachName));
    }

    static boolean isTransitionAllowed(String currentStatus, String targetStatus) {
        if (CoachBookingStatus.COACH_ACCEPTED.equals(targetStatus)
                || CoachBookingStatus.COACH_REJECTED.equals(targetStatus)) {
            return CoachBookingStatus.canCoachHandle(currentStatus);
        }
        if (CoachBookingStatus.ADMIN_APPROVED.equals(targetStatus)
                || CoachBookingStatus.ADMIN_REJECTED.equals(targetStatus)) {
            return CoachBookingStatus.canAdminHandle(currentStatus);
        }
        return false;
    }

    private String normalizeUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "用户";
        }
        return username.trim();
    }

    private String normalizeCoachName(String coachName) {
        if (coachName == null) {
            return "";
        }
        return coachName.trim();
    }

    private void dispatch(ExerciseRepository.ToggleCallback callback, ToggleResult result) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onResult(result));
    }
}
