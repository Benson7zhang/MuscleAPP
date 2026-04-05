package com.musclefit.app.ui.coach;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.musclefit.app.data.db.CoachBookingEntity;
import com.musclefit.app.repo.CoachRepository;
import com.musclefit.app.repo.ExerciseRepository;

import java.util.List;

public class CoachViewModel extends AndroidViewModel {
    private final CoachRepository repository;
    private final MutableLiveData<String> currentUser = new MutableLiveData<>("");

    private final LiveData<List<CoachBookingEntity>> userBookings;
    private final LiveData<List<CoachBookingEntity>> coachPendingBookings;
    private final LiveData<List<CoachBookingEntity>> coachScheduleBookings;
    private final LiveData<List<CoachBookingEntity>> adminReviewBookings;

    public CoachViewModel(@NonNull Application application) {
        super(application);
        repository = CoachRepository.getInstance(application);
        userBookings = Transformations.switchMap(currentUser, repository::observeUserBookings);
        coachPendingBookings = Transformations.switchMap(currentUser, repository::observeCoachPendingBookings);
        coachScheduleBookings = Transformations.switchMap(currentUser, repository::observeCoachScheduleBookings);
        adminReviewBookings = repository.observeAdminReviewBookings();
    }

    public LiveData<List<CoachBookingEntity>> getUserBookings() {
        return userBookings;
    }

    public LiveData<List<CoachBookingEntity>> getCoachPendingBookings() {
        return coachPendingBookings;
    }

    public LiveData<List<CoachBookingEntity>> getCoachScheduleBookings() {
        return coachScheduleBookings;
    }

    public LiveData<List<CoachBookingEntity>> getAdminReviewBookings() {
        return adminReviewBookings;
    }

    public void setCurrentUser(String username) {
        currentUser.setValue(username == null ? "" : username.trim());
    }

    public void createBooking(String username, CoachItem item, ExerciseRepository.ToggleCallback callback) {
        repository.createBooking(username, item, callback);
    }

    public void coachAccept(long bookingId, String coachName, ExerciseRepository.ToggleCallback callback) {
        repository.coachAccept(bookingId, coachName, callback);
    }

    public void coachReject(long bookingId, String coachName, ExerciseRepository.ToggleCallback callback) {
        repository.coachReject(bookingId, coachName, callback);
    }

    public void adminApprove(long bookingId, ExerciseRepository.ToggleCallback callback) {
        repository.adminApprove(bookingId, callback);
    }

    public void adminReject(long bookingId, ExerciseRepository.ToggleCallback callback) {
        repository.adminReject(bookingId, callback);
    }
}
