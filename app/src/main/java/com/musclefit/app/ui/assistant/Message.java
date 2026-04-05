package com.musclefit.app.ui.assistant;

public class Message {
    public static final int SENDER_USER = 0;
    public static final int SENDER_AI = 1;
    public static final int STATUS_SENT = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_ERROR = 2;

    private String content;
    private int senderType;
    private long timestamp;
    private int status;

    public Message(String content, int senderType, long timestamp) {
        this(content, senderType, timestamp, STATUS_SENT);
    }

    public Message(String content, int senderType, long timestamp, int status) {
        this.content = content;
        this.senderType = senderType;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public int getSenderType() {
        return senderType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}