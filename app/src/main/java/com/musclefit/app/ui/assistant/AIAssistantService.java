package com.musclefit.app.ui.assistant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.musclefit.app.BuildConfig;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.auth.AuthState;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AIAssistantService {
    public interface Callback {
        void onSuccess(String response);

        void onError(String error);
    }

    private static final String DEFAULT_LONGCAT_BASE_URL = "https://api.longcat.chat";
    private static final String DEFAULT_LONGCAT_MODEL = "LongCat-Flash-Chat";
    private static final long RETRY_DELAY_MS = 1000L;
    private static final String FALLBACK_REPLY = "抱歉，我无法理解这个问题。";
    private static final String TDEE_CALIBRATION_NOTE =
            "实战校准说明：理论公式只是起点，体重反馈才是最终答案。"
                    + "\n可先用 TDEE≈体重(kg)×30~35 做起始估算。"
                    + "\n然后连续观察 14 天晨起空腹体重变化：体重基本不变说明 TDEE 较准确；体重上涨说明当前热量偏高；体重下降说明当前热量偏低。"
                    + "\n根据趋势每次微调 100~200 kcal，再继续观察。";

    private static final int MAX_RETRIES = 2;
    private final OkHttpClient client;
    private final Handler mainHandler;
    private final AuthManager authManager;

    public AIAssistantService() {
        this(null);
    }

    public AIAssistantService(Context context) {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
        if (context == null) {
            authManager = null;
        } else {
            authManager = AuthManager.getInstance(context.getApplicationContext());
        }
    }

    public void sendMessage(String assistantType, String message, Callback callback) {
        sendMessage(assistantType, message, Collections.emptyList(), callback);
    }

    public void sendMessage(String assistantType, String message, List<Message> recentHistory, Callback callback) {
        if (callback == null) {
            return;
        }
        sendMessageWithRetry(assistantType, message, recentHistory, callback, 0);
    }

    private void sendMessageWithRetry(String assistantType, String message, List<Message> recentHistory, Callback callback, int retryCount) {
        String safeMessage = message == null ? "" : message.trim();
        if (safeMessage.isEmpty()) {
            mainHandler.post(() -> callback.onError("输入内容为空"));
            return;
        }

        String apiKey = BuildConfig.LONGCAT_API_KEY == null ? "" : BuildConfig.LONGCAT_API_KEY.trim();
        if (apiKey.isEmpty()) {
            mainHandler.post(() -> callback.onError("未配置 AI API Key（LONGCAT_API_KEY）"));
            return;
        }

        String requestBody;
        try {
            AuthState state = authManager == null ? AuthState.guest() : authManager.getCurrent();
            requestBody = buildRequestBody(getSystemPrompt(assistantType, state), safeMessage, recentHistory);
        } catch (JSONException e) {
            mainHandler.post(() -> callback.onError("请求构建失败"));
            return;
        }

        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(resolveChatCompletionUrl())
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                if (retryCount < MAX_RETRIES) {
                    scheduleRetry(assistantType, safeMessage, recentHistory, callback, retryCount + 1);
                } else {
                    String msg = e.getMessage() == null ? "unknown" : e.getMessage();
                    mainHandler.post(() -> callback.onError("网络错误: " + msg));
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try (Response safeResponse = response) {
                    if (safeResponse.isSuccessful()) {
                        if (safeResponse.body() == null) {
                            mainHandler.post(() -> callback.onError("API返回为空"));
                            return;
                        }
                        String answer = parseResponse(safeResponse.body().string());
                        String finalized = postProcessResponse(assistantType, answer);
                        mainHandler.post(() -> callback.onSuccess(finalized));
                    } else {
                        String errorBody = safeResponse.body() != null ? safeResponse.body().string() : "";
                        int code = safeResponse.code();
                        if (retryCount < MAX_RETRIES && (code == 500 || code == 502 || code == 503 || code == 504)) {
                            scheduleRetry(assistantType, safeMessage, recentHistory, callback, retryCount + 1);
                        } else {
                            String compactErrorBody = errorBody.length() > 120 ? errorBody.substring(0, 120) + "…" : errorBody;
                            mainHandler.post(() -> callback.onError("API错误: " + code + " " + compactErrorBody));
                        }
                    }
                } catch (IOException e) {
                    mainHandler.post(() -> callback.onError("响应解析失败: " + e.getMessage()));
                }
            }
        });
    }

    private void scheduleRetry(String assistantType, String message, List<Message> recentHistory, Callback callback, int nextRetryCount) {
        mainHandler.postDelayed(
                () -> sendMessageWithRetry(assistantType, message, recentHistory, callback, nextRetryCount),
                RETRY_DELAY_MS
        );
    }

    private String getSystemPrompt(String assistantType, AuthState state) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是 智炼引擎 的专业 AI 助手。输出必须专业、可执行、结构清晰，不夸张承诺，不提供医疗诊断。");
        prompt.append("\n\n用户资料（来自本地账号）：\n").append(buildProfileContext(state));
        prompt.append("\n\n资料完整性规则：");
        prompt.append("\n1. 仅可使用当前登录账号资料。");
        prompt.append("\n2. 若身高、体重、年龄、性别任一缺失，先给“基于通用假设”的方案。");
        prompt.append("\n3. 结尾必须提醒用户补充：性别、身高、体重、出生日期、训练目标（减脂、增肌、塑形、体能）。");

        prompt.append("\n\n输出格式要求：");
        prompt.append("\n请使用纯文本，不要使用 Markdown 语法。");
        prompt.append("\n禁止使用：#, *, -, >, ``` , --- 等分隔符号。");
        prompt.append("\n必须按下列标题顺序输出，并使用“标题：”格式：");
        prompt.append("\n资料解读：");
        prompt.append("\n可执行方案：");
        prompt.append("\n依据说明：");
        prompt.append("\n风险与禁忌：");
        prompt.append("\n需补充信息：");

        prompt.append("\n\n通用规范：");
        prompt.append("\n1. 资料解读需明确指出哪些字段未提供。");
        prompt.append("\n2. 可执行方案必须可落地，包含步骤和频率，不要空泛。");
        prompt.append("\n3. 风险与禁忌需给出常见错误和替代建议。");
        prompt.append("\n4. 若用户未明确目标，先给通用方案并提醒选择目标。");
        prompt.append("\n5. 需补充信息最多 5 条，用于下一轮个性化。");
        prompt.append("\n6. 资料缺失时必须明确写出“基于通用假设”。");

        if ("training".equals(assistantType)) {
            prompt.append("\n\n当前角色：训练教练");
            prompt.append("\n请优先给出训练计划，并在可执行方案中覆盖以下要点：");
            prompt.append("\n资料解读、训练目标、7 天训练安排（动作、组数、次数、休息时长）、动作要点与常见错误、风险与禁忌、需补充信息。");
            prompt.append("\n当用户未给出明确目标时，默认提供“全身基础训练 + 心肺 + 恢复”的通用周计划。");
            return prompt.toString();
        }

        if ("nutrition".equals(assistantType)) {
            prompt.append("\n\n当前角色：营养师");
            prompt.append("\n基础代谢与热量目标参考：\n").append(buildMetabolicContext(state));
            prompt.append("\n请优先给出饮食与恢复建议，并在可执行方案中覆盖以下要点：");
            prompt.append("\n资料解读、基础代谢(BMR)与维持热量(TDEE)估算、按目标给出每日热量范围、热量与三大营养素建议、一日饮食示例、训练日前后补给与补水建议、风险与禁忌、需补充信息。");
            prompt.append("\n涉及热量时尽量给出明确数值或范围，单位统一为 kcal/天。");
            prompt.append("\n请尽量给出三大营养素比例（蛋白质/碳水/脂肪）的百分比，并让总和接近 100%。");
            prompt.append("\n输出前请进行现实性自检：避免极端热量建议，避免比例明显失衡。若数据不足请明确标注为通用估算。");
            prompt.append("\n必须补充实战校准说明：");
            prompt.append("\n1) 理论值先算：TDEE≈体重(kg)×30~35（并可同时给 BMR/TDEE 公式估算）。");
            prompt.append("\n2) 连续观察 14 天体重变化做校准：体重不变=当前 TDEE 基本准确；体重上涨=热量偏高；体重下降=热量偏低。");
            prompt.append("\n3) 明确写出：公式只是起点，体重反馈才是最终答案。");
            return prompt.toString();
        }

        prompt.append("\n\n当前角色：健身与营养顾问");
        prompt.append("\n请综合训练与饮食给出结构化建议，保持标题清晰和可执行性。");
        return prompt.toString();
    }

    private String buildProfileContext(AuthState state) {
        if (state == null || !state.loggedIn) {
            return "- 登录状态: 未登录\n"
                    + "- 性别: 未提供\n"
                    + "- 年龄: 未提供\n"
                    + "- 身高(cm): 未提供\n"
                    + "- 体重(kg): 未提供";
        }

        Integer age = parseAge(state.birthDate);
        return "- 登录状态: 已登录\n"
                + "- 性别: " + safeProfileValue(state.gender) + "\n"
                + "- 年龄: " + (age == null ? "未提供" : age) + "\n"
                + "- 身高(cm): " + safeProfileValue(state.heightCm) + "\n"
                + "- 体重(kg): " + safeProfileValue(state.weightKg);
    }

    private String safeProfileValue(String value) {
        if (value == null) {
            return "未提供";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "未提供" : trimmed;
    }

    private String buildMetabolicContext(AuthState state) {
        if (state == null || !state.loggedIn) {
            return "- 用户未登录，无法使用账号资料估算。\n"
                    + "- 仅可提供通用饮食建议。";
        }

        Double weight = parsePositiveNumber(state.weightKg);
        Double height = parsePositiveNumber(state.heightCm);
        Integer age = parseAge(state.birthDate);
        Integer genderType = parseGenderType(state.gender); // 1 male, 2 female, 0 unknown

        if (weight == null || height == null || age == null || genderType == 0) {
            return "- BMR估算：资料不足（需性别、身高、体重、出生日期）。\n"
                    + "- 可先给通用饮食建议，并提醒补全资料与目标。";
        }

        double bmr = (10d * weight) + (6.25d * height) - (5d * age) + (genderType == 1 ? 5d : -161d);
        double tdeeLow = bmr * 1.30d;
        double tdeeHigh = bmr * 1.60d;

        double fatLossLow = Math.max(1200d, tdeeLow - 500d);
        double fatLossHigh = Math.max(fatLossLow, tdeeHigh - 300d);
        double gainLow = tdeeLow + 200d;
        double gainHigh = tdeeHigh + 350d;
        double shapeLow = tdeeLow - 100d;
        double shapeHigh = tdeeHigh + 100d;
        double performanceLow = tdeeLow;
        double performanceHigh = tdeeHigh + 200d;

        return String.format(
                Locale.getDefault(),
                "- 预估BMR（Mifflin-St Jeor）≈ %.0f kcal/天。\n"
                        + "- 预估维持热量TDEE范围 ≈ %.0f ~ %.0f kcal/天（轻-中等活动假设）。\n"
                        + "- 减脂目标建议热量范围 ≈ %.0f ~ %.0f kcal/天。\n"
                        + "- 增肌目标建议热量范围 ≈ %.0f ~ %.0f kcal/天。\n"
                        + "- 塑形目标建议热量范围 ≈ %.0f ~ %.0f kcal/天。\n"
                        + "- 体能目标建议热量范围 ≈ %.0f ~ %.0f kcal/天。",
                bmr,
                tdeeLow, tdeeHigh,
                fatLossLow, fatLossHigh,
                gainLow, gainHigh,
                shapeLow, shapeHigh,
                performanceLow, performanceHigh
        );
    }

    private Double parsePositiveNumber(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(normalized);
            return value > 0d ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Integer parseAge(String birthDate) {
        if (birthDate == null || birthDate.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate birth = LocalDate.parse(birthDate.trim());
            int years = Period.between(birth, LocalDate.now()).getYears();
            if (years <= 0 || years > 100) {
                return null;
            }
            return years;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseGenderType(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return 0;
        }
        String value = gender.trim().toLowerCase(Locale.ROOT);
        if (value.contains("男") || value.contains("male")) {
            return 1;
        }
        if (value.contains("女") || value.contains("female")) {
            return 2;
        }
        return 0;
    }

    private String buildRequestBody(String systemPrompt, String userMessage, List<Message> recentHistory) throws JSONException {
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));

        appendHistoryMessages(messages, recentHistory);

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));

        JSONObject payload = new JSONObject();
        payload.put("model", resolveModel());
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        return payload.toString();
    }

    private String resolveModel() {
        String configured = BuildConfig.LONGCAT_MODEL == null ? "" : BuildConfig.LONGCAT_MODEL.trim();
        return configured.isEmpty() ? DEFAULT_LONGCAT_MODEL : configured;
    }

    private String resolveChatCompletionUrl() {
        String configuredBase = BuildConfig.LONGCAT_BASE_URL == null ? "" : BuildConfig.LONGCAT_BASE_URL.trim();
        String base = configuredBase.isEmpty() ? DEFAULT_LONGCAT_BASE_URL : configuredBase;
        if (base.endsWith("/chat/completions")) {
            return base;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.endsWith("/openai/v1")) {
            return base + "/chat/completions";
        }
        if (base.endsWith("/openai")) {
            return base + "/v1/chat/completions";
        }
        if (base.endsWith("/v1")) {
            return base + "/chat/completions";
        }
        if (base.contains("longcat.chat")) {
            return base + "/openai/v1/chat/completions";
        }
        return base + "/v1/chat/completions";
    }

    private void appendHistoryMessages(JSONArray messages, List<Message> recentHistory) throws JSONException {
        if (recentHistory == null || recentHistory.isEmpty()) {
            return;
        }
        int start = Math.max(0, recentHistory.size() - 10);
        for (int i = start; i < recentHistory.size(); i++) {
            Message msg = recentHistory.get(i);
            if (msg == null) {
                continue;
            }
            if (msg.getStatus() != Message.STATUS_SENT) {
                continue;
            }
            String content = msg.getContent() == null ? "" : msg.getContent().trim();
            if (content.isEmpty()) {
                continue;
            }
            String role = msg.getSenderType() == Message.SENDER_AI ? "assistant" : "user";
            messages.put(new JSONObject()
                    .put("role", role)
                    .put("content", content));
        }
    }

    private String parseResponse(String responseBody) {
        try {
            JSONObject root = new JSONObject(responseBody);
            JSONArray choices = root.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                return FALLBACK_REPLY;
            }
            JSONObject choice0 = choices.optJSONObject(0);
            if (choice0 == null) {
                return FALLBACK_REPLY;
            }
            JSONObject message = choice0.optJSONObject("message");
            if (message == null) {
                return FALLBACK_REPLY;
            }
            String content = message.optString("content", "").trim();
            return content.isEmpty() ? FALLBACK_REPLY : content;
        } catch (JSONException e) {
            return FALLBACK_REPLY;
        }
    }

    private String postProcessResponse(String assistantType, String content) {
        String safeContent = content == null ? "" : content.trim();
        if (safeContent.isEmpty()) {
            return FALLBACK_REPLY;
        }
        if (!"nutrition".equals(assistantType)) {
            return safeContent;
        }

        String finalized = safeContent;
        NutritionCalorieParser.CalorieVisualData calorieData = NutritionCalorieParser.parse(safeContent);
        NutritionMacroParser.MacroVisualData macroData = NutritionMacroParser.parse(safeContent);
        NutritionRealityChecker.Result result = NutritionRealityChecker.check(calorieData, macroData);
        if (result.hasData && !result.plausible && !result.message.isEmpty() && !safeContent.contains("现实性校验")) {
            String hint = result.message.replace("现实性校验：", "").trim();
            finalized = finalized + "\n现实性校验提示：" + hint;
        }

        if (isCalorieTopic(safeContent) && !containsTdeeCalibrationHint(safeContent)) {
            finalized = finalized + "\n" + TDEE_CALIBRATION_NOTE;
        }
        return finalized;
    }

    private boolean isCalorieTopic(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        String lowered = content.toLowerCase(Locale.ROOT);
        return lowered.contains("tdee")
                || lowered.contains("bmr")
                || lowered.contains("热量")
                || lowered.contains("基础代谢")
                || lowered.contains("卡路里")
                || lowered.contains("kcal")
                || lowered.contains("千卡")
                || lowered.contains("大卡")
                || lowered.contains("摄入");
    }

    private boolean containsTdeeCalibrationHint(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        String lowered = content.toLowerCase(Locale.ROOT);
        boolean has14d = lowered.contains("14天") || lowered.contains("两周") || lowered.contains("14 日");
        boolean hasWeightFeedback = lowered.contains("体重") && (lowered.contains("变化") || lowered.contains("反馈") || lowered.contains("校准"));
        boolean hasFormulaIsStart = lowered.contains("公式只是起点") || lowered.contains("体重反馈才是最终答案");
        return (has14d && hasWeightFeedback) || hasFormulaIsStart;
    }
}
