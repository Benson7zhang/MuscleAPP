package com.musclefit.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "forum_report",
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
                @Index(value = {"reporter_account_id"}),
                @Index(value = {"created_at"})
        }
)
public class ForumReportEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "post_id")
    public long postId;

    @NonNull
    @ColumnInfo(name = "reporter_account_id")
    public String reporterAccountId = "guest";

    @NonNull
    public String reason = "";

    @NonNull
    public String status = "OPEN";

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
