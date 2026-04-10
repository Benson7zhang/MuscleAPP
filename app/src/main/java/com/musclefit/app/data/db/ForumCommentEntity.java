package com.musclefit.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "forum_comment",
        foreignKeys = {
                @ForeignKey(
                        entity = ForumPostEntity.class,
                        parentColumns = "id",
                        childColumns = "post_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"post_id"}),
                @Index(value = {"author_account_id"}),
                @Index(value = {"created_at"})
        }
)
public class ForumCommentEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "post_id")
    public long postId;

    @NonNull
    @ColumnInfo(name = "author_account_id")
    public String authorAccountId = "guest";

    @NonNull
    @ColumnInfo(name = "author_nickname")
    public String authorNickname = "";

    @NonNull
    public String content = "";

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
