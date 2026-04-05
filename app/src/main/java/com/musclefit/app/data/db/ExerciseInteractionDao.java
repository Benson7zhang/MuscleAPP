package com.musclefit.app.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ExerciseInteractionDao {
    @Query("SELECT * FROM exercise_interaction WHERE exercise_id = :exerciseId AND account_id = :accountId LIMIT 1")
    ExerciseInteractionEntity getByExerciseIdSync(long exerciseId, String accountId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ExerciseInteractionEntity entity);
}
