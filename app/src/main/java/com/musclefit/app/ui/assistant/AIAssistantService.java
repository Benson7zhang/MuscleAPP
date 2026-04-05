package com.musclefit.app.ui.assistant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIAssistantService {
    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }

    private static final String BASE_URL = "https://api.longcat.chat";
    private static final String API_KEY = "ak_2pO0gh5B493x5sy0sG0BH2O94pI7e"; // 请替换为实际的API密钥
    private static final String MODEL = "LongCat-Flash-Chat";

    private OkHttpClient client;
    private Handler mainHandler;
    private static final int MAX_RETRIES = 2;

    public AIAssistantService(Context context) {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void sendMessage(String assistantType, String message, Callback callback) {
        sendMessageWithRetry(assistantType, message, callback, 0);
    }

    private void sendMessageWithRetry(String assistantType, String message, Callback callback, int retryCount) {
        String systemPrompt = getSystemPrompt(assistantType);
        String requestBody = buildRequestBody(systemPrompt, message);

        RequestBody body = RequestBody.create(
                requestBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                if (retryCount < MAX_RETRIES) {
                    System.out.println("请求失败，正在重试... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                    // 延迟1秒后重试
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        sendMessageWithRetry(assistantType, message, callback, retryCount + 1);
                    }, 1000);
                } else {
                    mainHandler.post(() -> callback.onError("网络错误: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("API响应: " + responseBody);
                    String answer = parseResponse(responseBody);
                    mainHandler.post(() -> callback.onSuccess(answer));
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";  
                    System.out.println("API错误: " + response.code() + " " + errorBody);
                    // 对于某些错误码，也可以重试
                    if (retryCount < MAX_RETRIES && (response.code() == 500 || response.code() == 502 || response.code() == 503 || response.code() == 504)) {
                        System.out.println("API错误，正在重试... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            sendMessageWithRetry(assistantType, message, callback, retryCount + 1);
                        }, 1000);
                    } else {
                        mainHandler.post(() -> callback.onError("API错误: " + response.code() + " " + errorBody));
                    }
                }
            }
        });
    }

    private String getSystemPrompt(String assistantType) {
        if ("training".equals(assistantType)) {
            return "你是一位专业的健身教练，帮助用户解答训练相关问题。请提供专业、科学、实用的健身建议。";
        } else if ("nutrition".equals(assistantType)) {
            return "你是一位专业的营养师，帮助用户解答营养相关问题。请提供科学、健康、实用的营养建议。";
        } else {
            return "你是一位专业的健身和营养顾问，帮助用户解答健身和营养相关问题。";
        }
    }

    private String buildRequestBody(String systemPrompt, String userMessage) {
        List<MessageItem> messages = new ArrayList<>();
        messages.add(new MessageItem("system", systemPrompt));
        messages.add(new MessageItem("user", userMessage));

        // 构建正确的JSON格式
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\": \"").append(MODEL).append("\",");
        sb.append("\"messages\": [");
        for (int i = 0; i < messages.size(); i++) {
            sb.append(messages.get(i).toString());
            if (i < messages.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("],");
        sb.append("\"temperature\": 0.7");
        sb.append("}");
        return sb.toString();
    }

    private String parseResponse(String responseBody) {
        // 改进的解析方法，处理包含特殊字符的响应
        try {
            // 查找choices数组中的第一个元素
            int choicesStart = responseBody.indexOf("\"choices\":[");
            if (choicesStart == -1) {
                return "抱歉，我无法理解这个问题。";
            }
            
            // 查找message字段
            int messageStart = responseBody.indexOf("\"message\":", choicesStart);
            if (messageStart == -1) {
                return "抱歉，我无法理解这个问题。";
            }
            
            // 查找content字段
            int contentStart = responseBody.indexOf("\"content\":\"", messageStart);
            if (contentStart == -1) {
                return "抱歉，我无法理解这个问题。";
            }
            
            contentStart += "\"content\":\"" .length();
            
            // 查找content字段的结束位置，考虑转义字符
            int contentEnd = contentStart;
            boolean inQuotes = true;
            boolean escaped = false;
            
            while (contentEnd < responseBody.length()) {
                char c = responseBody.charAt(contentEnd);
                
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"' && !escaped) {
                    break;
                }
                
                contentEnd++;
            }
            
            if (contentEnd >= responseBody.length()) {
                return "抱歉，我无法理解这个问题。";
            }
            
            String content = responseBody.substring(contentStart, contentEnd);
            return content.replace("\\n", "\n").replace("\\t", "\t").replace("\\\"", "\"");
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，我无法理解这个问题。";
        }
    }

    private static class MessageItem {
        private String role;
        private String content;

        public MessageItem(String role, String content) {
            this.role = role;
            this.content = content;
        }

        @Override
        public String toString() {
            return "{\"role\":\"" + role + "\",\"content\":\"" + content.replace("\"", "\\\"") + "\"}";
        }
    }
}