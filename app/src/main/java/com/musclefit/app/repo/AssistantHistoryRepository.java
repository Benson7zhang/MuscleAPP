package com.musclefit.app.repo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthState;
import com.musclefit.app.data.db.AppDatabase;
import com.musclefit.app.data.db.AssistantMessageDao;
import com.musclefit.app.data.db.AssistantMessageEntity;
import com.musclefit.app.ui.assistant.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssistantHistoryRepository {
    public interface LoadCallback {
        void onLoaded(List<Message> messages);
    }

    private static final int MAX_STORED_MESSAGES = 120;

    private static volatile AssistantHistoryRepository INSTANCE;

    private final AssistantMessageDao messageDao;
    private final ExecutorService ioExecutor;
    private final Handler mainHandler;
    private final AuthManager authManager;

    private AssistantHistoryRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        messageDao = db.assistantMessageDao();
        ioExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        authManager = AuthManager.getInstance(context);
    }

    public static AssistantHistoryRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AssistantHistoryRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AssistantHistoryRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void loadConversation(String assistantType, LoadCallback callback) {
        if (callback == null) {
            return;
        }
        String scopeAccountId = currentAccountId();
        String scopeAssistantType = normalizeAssistantType(assistantType);

        ioExecutor.execute(() -> {
            List<AssistantMessageEntity> rows = messageDao.listMessages(scopeAccountId, scopeAssistantType);
            List<Message> mapped = new ArrayList<>(rows.size());
            for (AssistantMessageEntity row : rows) {
                mapped.add(new Message(row.content, row.senderType, row.createdAt, row.status));
            }
            mainHandler.post(() -> callback.onLoaded(mapped));
        });
    }

    public void appendMessage(String assistantType, Message message) {
        if (message == null) {
            return;
        }
        String scopeAccountId = currentAccountId();
        String scopeAssistantType = normalizeAssistantType(assistantType);

        ioExecutor.execute(() -> {
            AssistantMessageEntity row = new AssistantMessageEntity();
            row.accountId = scopeAccountId;
            row.assistantType = scopeAssistantType;
            row.senderType = message.getSenderType();
            row.content = message.getContent() == null ? "" : message.getContent();
            row.status = message.getStatus();
            row.createdAt = message.getTimestamp();
            messageDao.insert(row);
            messageDao.pruneConversation(scopeAccountId, scopeAssistantType, MAX_STORED_MESSAGES);
        });
    }

    public void clearConversation(String assistantType) {
        String scopeAccountId = currentAccountId();
        String scopeAssistantType = normalizeAssistantType(assistantType);
        ioExecutor.execute(() -> messageDao.clearConversation(scopeAccountId, scopeAssistantType));
    }

    public String currentAccountId() {
        AuthState state = authManager.getCurrent();
        if (state == null || !state.loggedIn) {
            return "guest";
        }
        if (state.accountId == null || state.accountId.trim().isEmpty()) {
            return "guest";
        }
        return state.accountId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeAssistantType(String assistantType) {
        if (assistantType == null || assistantType.trim().isEmpty()) {
            return "training";
        }
        return assistantType.trim().toLowerCase(Locale.ROOT);
    }
}
