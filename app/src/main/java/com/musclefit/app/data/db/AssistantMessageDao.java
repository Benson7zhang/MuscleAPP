package com.musclefit.app.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AssistantMessageDao {
    @Query("SELECT * FROM assistant_message " +
            "WHERE account_id = :accountId AND assistant_type = :assistantType " +
            "ORDER BY created_at ASC, id ASC")
    List<AssistantMessageEntity> listMessages(String accountId, String assistantType);

    @Insert
    long insert(AssistantMessageEntity message);

    @Query("DELETE FROM assistant_message " +
            "WHERE account_id = :accountId AND assistant_type = :assistantType")
    void clearConversation(String accountId, String assistantType);

    @Query("DELETE FROM assistant_message " +
            "WHERE account_id = :accountId AND assistant_type = :assistantType " +
            "AND id NOT IN (" +
            "SELECT id FROM assistant_message " +
            "WHERE account_id = :accountId AND assistant_type = :assistantType " +
            "ORDER BY created_at DESC, id DESC LIMIT :maxKeep)")
    void pruneConversation(String accountId, String assistantType, int maxKeep);
}
