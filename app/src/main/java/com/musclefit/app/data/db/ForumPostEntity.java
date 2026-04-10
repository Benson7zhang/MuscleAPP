package com.musclefit.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "forum_post",
        indices = {
                @Index(value = {"author_account_id"}),
                @Index(value = {"updated_at"})
        }
)
public class ForumPostEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "author_account_id")
    public String authorAccountId = "guest";

    @NonNull
    @ColumnInfo(name = "author_nickname")
    public String authorNickname = "";

    @NonNull
    public String title = "";

    @NonNull
    public String content = "";

    @NonNull
    @ColumnInfo(name = "image_uris")
    public String imageUris = "";

    @ColumnInfo(name = "like_count")
    public int likeCount;

    @ColumnInfo(name = "comment_count")
    public int commentCount;

    @ColumnInfo(name = "favorite_count")
    public int favoriteCount;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
