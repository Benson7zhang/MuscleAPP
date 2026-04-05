package com.musclefit.app.data.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.annotation.NonNull;

@Entity(
        tableName = "exercise_muscle_intensity",
        primaryKeys = {"exercise_id", "muscle_name"},
        foreignKeys = @ForeignKey(
                entity = ExerciseEntity.class,
                parentColumns = "id",
                childColumns = "exercise_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class ExerciseMuscleIntensityEntity {
    @ColumnInfo(name = "exercise_id")
    public long exerciseId;

    @ColumnInfo(name = "muscle_name")
    @NonNull
    public String muscleName;

    @ColumnInfo(name = "intensity_level")
    public int intensityLevel;

    public String role;
}
