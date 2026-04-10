package com.musclefit.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "forum_post_favorite",
        primaryKeys = {"post_id", "account_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = ForumPostEntity.class,
                        parentColumns = "id",
                        childColumns = "post_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"account_id"}),
                @Index(value = {"post_id"})
        }
)
public class ForumPostFavoriteEntity {
    @ColumnInfo(name = "post_id")
    public long postId;

    @NonNull
    @ColumnInfo(name = "account_id")
    public String accountId = "guest";

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
