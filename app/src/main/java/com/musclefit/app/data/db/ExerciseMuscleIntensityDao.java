package com.musclefit.app.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.musclefit.app.data.model.ExerciseIntensityNote;

import java.util.List;

@Dao
public interface ExerciseMuscleIntensityDao {
    @Query("SELECT muscle_name AS muscleName, intensity_level AS intensityLevel, role AS role " +
            "FROM exercise_muscle_intensity WHERE exercise_id = :exerciseId " +
            "ORDER BY intensity_level DESC, muscle_name ASC")
    LiveData<List<ExerciseIntensityNote>> observeNotes(long exerciseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExerciseMuscleIntensityEntity> notes);
}
