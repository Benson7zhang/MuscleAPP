package com.musclefit.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "assistant_message",
        indices = {
                @Index(value = {"account_id", "assistant_type", "created_at"})
        }
)
public class AssistantMessageEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "account_id")
    public String accountId = "guest";

    @NonNull
    @ColumnInfo(name = "assistant_type")
    public String assistantType = "training";

    @ColumnInfo(name = "sender_type")
    public int senderType;

    @NonNull
    @ColumnInfo(name = "content")
    public String content = "";

    @ColumnInfo(name = "status")
    public int status;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
