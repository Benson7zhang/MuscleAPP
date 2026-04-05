package com.musclefit.app.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.musclefit.app.data.model.ExerciseCard;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Query("SELECT " +
            "e.id AS id, e.name AS name, e.training_category AS trainingCategory, e.movement_type AS movementType, " +
            "e.description AS description, e.grip_type AS gripType, e.category_hint AS categoryHint, " +
            "e.caution_notes AS cautionNotes, e.primary_muscle AS primaryMuscle, e.like_count AS likeCount, " +
            "e.favorite_count AS favoriteCount, e.max_intensity_level AS maxIntensityLevel, " +
            "COALESCE(i.liked, 0) AS liked, COALESCE(i.favorited, 0) AS favorited " +
            "FROM exercise e " +
            "LEFT JOIN exercise_interaction i ON e.id = i.exercise_id AND i.account_id = :accountId " +
            "WHERE (:keyword = '' OR e.name LIKE '%' || :keyword || '%' OR e.primary_muscle LIKE '%' || :keyword || '%') " +
            "AND (:category = 'ALL' OR e.training_category = :category) " +
            "ORDER BY e.id")
    LiveData<List<ExerciseCard>> observeExercises(String keyword, String category, String accountId);

    @Query("SELECT " +
            "e.id AS id, e.name AS name, e.training_category AS trainingCategory, e.movement_type AS movementType, " +
            "e.description AS description, e.grip_type AS gripType, e.category_hint AS categoryHint, " +
            "e.caution_notes AS cautionNotes, e.primary_muscle AS primaryMuscle, e.like_count AS likeCount, " +
            "e.favorite_count AS favoriteCount, e.max_intensity_level AS maxIntensityLevel, " +
            "COALESCE(i.liked, 0) AS liked, COALESCE(i.favorited, 0) AS favorited " +
            "FROM exercise e " +
            "LEFT JOIN exercise_interaction i ON e.id = i.exercise_id AND i.account_id = :accountId " +
            "WHERE COALESCE(i.favorited, 0) = 1 " +
            "AND (:keyword = '' OR e.name LIKE '%' || :keyword || '%' OR e.primary_muscle LIKE '%' || :keyword || '%') " +
            "AND (:category = 'ALL' OR e.training_category = :category) " +
            "ORDER BY i.updated_at DESC")
    LiveData<List<ExerciseCard>> observeFavoriteExercises(String keyword, String category, String accountId);

    @Query("SELECT " +
            "e.id AS id, e.name AS name, e.training_category AS trainingCategory, e.movement_type AS movementType, " +
            "e.description AS description, e.grip_type AS gripType, e.category_hint AS categoryHint, " +
            "e.caution_notes AS cautionNotes, e.primary_muscle AS primaryMuscle, e.like_count AS likeCount, " +
            "e.favorite_count AS favoriteCount, e.max_intensity_level AS maxIntensityLevel, " +
            "COALESCE(i.liked, 0) AS liked, COALESCE(i.favorited, 0) AS favorited " +
            "FROM exercise e " +
            "LEFT JOIN exercise_interaction i ON e.id = i.exercise_id AND i.account_id = :accountId " +
            "WHERE e.id = :exerciseId LIMIT 1")
    LiveData<ExerciseCard> observeExerciseById(long exerciseId, String accountId);

    @Query("SELECT * FROM exercise WHERE id = :exerciseId LIMIT 1")
    ExerciseEntity getExerciseSync(long exerciseId);

    @Query("UPDATE exercise SET like_count = :likeCount, favorite_count = :favoriteCount WHERE id = :exerciseId")
    void updateCounts(long exerciseId, int likeCount, int favoriteCount);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<ExerciseEntity> exercises);

    @Query("UPDATE exercise SET description = :description WHERE id = :exerciseId")
    void updateDescription(long exerciseId, String description);
}
