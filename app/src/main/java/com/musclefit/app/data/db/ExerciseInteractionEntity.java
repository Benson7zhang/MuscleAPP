package com.musclefit.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "exercise_interaction",
        primaryKeys = {"exercise_id", "account_id"},
        foreignKeys = @ForeignKey(
                entity = ExerciseEntity.class,
                parentColumns = "id",
                childColumns = "exercise_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = {"exercise_id", "account_id"}, unique = true),
                @Index(value = {"account_id"})
        }
)
public class ExerciseInteractionEntity {
    @ColumnInfo(name = "exercise_id")
    public long exerciseId;

    @NonNull
    @ColumnInfo(name = "account_id")
    public String accountId = "";

    public boolean liked;

    public boolean favorited;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
