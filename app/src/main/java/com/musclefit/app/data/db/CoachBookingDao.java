package com.musclefit.app.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CoachBookingDao {
    @Query("SELECT * FROM coach_booking WHERE user_name = :userName ORDER BY updated_at DESC")
    LiveData<List<CoachBookingEntity>> observeByUser(String userName);

    @Query("SELECT * FROM coach_booking WHERE status = :status ORDER BY created_at ASC")
    LiveData<List<CoachBookingEntity>> observeByStatus(String status);

    @Query("SELECT * FROM coach_booking WHERE status = :status AND coach_name = :coachName ORDER BY created_at ASC")
    LiveData<List<CoachBookingEntity>> observeByStatusAndCoach(String status, String coachName);

    @Query("SELECT * FROM coach_booking WHERE status IN (:firstStatus, :secondStatus) ORDER BY updated_at DESC")
    LiveData<List<CoachBookingEntity>> observeByStatuses(String firstStatus, String secondStatus);

    @Query("SELECT * FROM coach_booking WHERE status IN (:firstStatus, :secondStatus) AND coach_name = :coachName ORDER BY updated_at DESC")
    LiveData<List<CoachBookingEntity>> observeByStatusesAndCoach(String firstStatus, String secondStatus, String coachName);

    @Query("SELECT * FROM coach_booking WHERE id = :bookingId LIMIT 1")
    CoachBookingEntity getByIdSync(long bookingId);

    @Query("UPDATE coach_booking SET status = :status, updated_at = :updatedAt WHERE id = :bookingId")
    int updateStatus(long bookingId, String status, long updatedAt);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(CoachBookingEntity entity);
}
