package com.musclefit.app.data.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise")
public class ExerciseEntity {
    @PrimaryKey
    public long id;

    public String name;

    @ColumnInfo(name = "training_category")
    public String trainingCategory;

    @ColumnInfo(name = "movement_type")
    public String movementType;

    public String description;

    @ColumnInfo(name = "grip_type")
    public String gripType;

    @ColumnInfo(name = "category_hint")
    public String categoryHint;

    @ColumnInfo(name = "caution_notes")
    public String cautionNotes;

    @ColumnInfo(name = "primary_muscle")
    public String primaryMuscle;

    @ColumnInfo(name = "like_count")
    public int likeCount;

    @ColumnInfo(name = "favorite_count")
    public int favoriteCount;

    @ColumnInfo(name = "max_intensity_level")
    public int maxIntensityLevel;
}
