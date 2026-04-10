package com.musclefit.app.ui.assistant;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class NutritionCalorieParser {
    private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d{3,4})\\s*(?:~|～|-|—|–|到|至)\\s*(\\d{3,4})\\s*(?:kcal|千卡|大卡)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SINGLE_PATTERN = Pattern.compile("(\\d{3,4})\\s*(?:kcal|千卡|大卡)?", Pattern.CASE_INSENSITIVE);
    private static final int BMR_MIN = 900;
    private static final int BMR_MAX = 2800;
    private static final int TDEE_MIN = 1200;
    private static final int TDEE_MAX = 4500;
    private static final int TARGET_MIN = 1100;
    private static final int TARGET_MAX = 4200;

    private NutritionCalorieParser() {
    }

    static CalorieVisualData parse(String rawText) {
        String content = rawText == null ? "" : rawText;
        Metric bmr = extractMetric(content, new String[]{"BMR", "基础代谢", "基础代谢率"});
        Metric tdee = extractMetric(content, new String[]{"TDEE", "维持热量", "总消耗", "总能量消耗", "日总消耗"});
        Metric target = extractMetric(content, new String[]{"每日热量范围", "每日热量", "目标热量", "建议热量", "摄入热量", "热量范围"});

        if (target == null) {
            target = firstRange(content);
        }

        Metric normalizedBmr = normalizeMetric(bmr, BMR_MIN, BMR_MAX);
        Metric normalizedTdee = normalizeMetric(tdee, TDEE_MIN, TDEE_MAX);
        Metric normalizedTarget = normalizeMetric(target, TARGET_MIN, TARGET_MAX);

        if (normalizedBmr != null && normalizedTdee != null) {
            int minTdee = clamp((int) Math.round(normalizedBmr.value * 1.20d), TDEE_MIN, TDEE_MAX);
            int maxTdee = clamp((int) Math.round(normalizedBmr.value * 2.05d), TDEE_MIN, TDEE_MAX);
            normalizedTdee = clampMetricToBand(normalizedTdee, minTdee, maxTdee);
        }

        if (normalizedTarget != null && normalizedTdee != null) {
            int minTargetFromTdee = clamp((int) Math.round(normalizedTdee.value * 0.70d), TARGET_MIN, TARGET_MAX);
            int maxTargetFromTdee = clamp((int) Math.round(normalizedTdee.value * 1.20d), TARGET_MIN, TARGET_MAX);
            normalizedTarget = clampMetricToBand(normalizedTarget, minTargetFromTdee, maxTargetFromTdee);
        }

        if (normalizedTarget != null && normalizedBmr != null) {
            int minTargetFromBmr = clamp((int) Math.round(normalizedBmr.value * 0.90d), TARGET_MIN, TARGET_MAX);
            normalizedTarget = clampMetricToBand(normalizedTarget, minTargetFromBmr, TARGET_MAX);
        }

        int max = 0;
        if (normalizedBmr != null) {
            max = Math.max(max, normalizedBmr.value);
        }
        if (normalizedTdee != null) {
            max = Math.max(max, normalizedTdee.value);
        }
        if (normalizedTarget != null) {
            max = Math.max(max, normalizedTarget.value);
        }

        int scaleMax = toScale(max);
        return new CalorieVisualData(normalizedBmr, normalizedTdee, normalizedTarget, scaleMax);
    }

    private static Metric normalizeMetric(Metric metric, int min, int max) {
        if (metric == null) {
            return null;
        }
        if (!metric.isRange()) {
            return Metric.single(clamp(metric.value, min, max));
        }
        int low = clamp(metric.low, min, max);
        int high = clamp(metric.high, min, max);
        if (low > high) {
            int temp = low;
            low = high;
            high = temp;
        }
        int value = clamp(metric.value, low, high);
        return Metric.range(value, low, high);
    }

    private static Metric clampMetricToBand(Metric metric, int bandMin, int bandMax) {
        if (metric == null) {
            return null;
        }
        int safeMin = Math.min(bandMin, bandMax);
        int safeMax = Math.max(bandMin, bandMax);
        if (!metric.isRange()) {
            return Metric.single(clamp(metric.value, safeMin, safeMax));
        }
        int low = clamp(metric.low, safeMin, safeMax);
        int high = clamp(metric.high, safeMin, safeMax);
        if (low > high) {
            int temp = low;
            low = high;
            high = temp;
        }
        int value = clamp(metric.value, low, high);
        return Metric.range(value, low, high);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Metric extractMetric(String content, String[] keywords) {
        if (content == null || content.trim().isEmpty() || keywords == null) {
            return null;
        }
        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            Metric metric = extractNearKeyword(content, keyword);
            if (metric != null) {
                return metric;
            }
        }
        return null;
    }

    private static Metric extractNearKeyword(String content, String keyword) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String safeLine = line.trim();
            if (safeLine.isEmpty()) {
                continue;
            }
            String lowered = safeLine.toLowerCase(Locale.ROOT);
            if (!lowered.contains(keyword.toLowerCase(Locale.ROOT))) {
                continue;
            }
            Metric metric = parseMetricFromText(safeLine);
            if (metric != null) {
                return metric;
            }
        }

        Pattern nearbyPattern = Pattern.compile(Pattern.quote(keyword) + "[^\\n\\d]{0,20}(\\d{3,4})(?:\\s*(?:~|～|-|—|–|到|至)\\s*(\\d{3,4}))?", Pattern.CASE_INSENSITIVE);
        Matcher nearbyMatcher = nearbyPattern.matcher(content);
        if (nearbyMatcher.find()) {
            int left = parseIntSafe(nearbyMatcher.group(1));
            int right = parseIntSafe(nearbyMatcher.group(2));
            if (left > 0 && right > 0) {
                int low = Math.min(left, right);
                int high = Math.max(left, right);
                return Metric.range((low + high) / 2, low, high);
            }
            if (left > 0) {
                return Metric.single(left);
            }
        }
        return null;
    }

    private static Metric parseMetricFromText(String line) {
        Matcher rangeMatcher = RANGE_PATTERN.matcher(line);
        if (rangeMatcher.find()) {
            int left = parseIntSafe(rangeMatcher.group(1));
            int right = parseIntSafe(rangeMatcher.group(2));
            if (left > 0 && right > 0) {
                int low = Math.min(left, right);
                int high = Math.max(left, right);
                return Metric.range((low + high) / 2, low, high);
            }
        }

        Matcher singleMatcher = SINGLE_PATTERN.matcher(line);
        if (singleMatcher.find()) {
            int value = parseIntSafe(singleMatcher.group(1));
            if (value > 0) {
                return Metric.single(value);
            }
        }
        return null;
    }

    private static Metric firstRange(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        Matcher rangeMatcher = RANGE_PATTERN.matcher(content);
        if (!rangeMatcher.find()) {
            return null;
        }
        int left = parseIntSafe(rangeMatcher.group(1));
        int right = parseIntSafe(rangeMatcher.group(2));
        if (left <= 0 || right <= 0) {
            return null;
        }
        int low = Math.min(left, right);
        int high = Math.max(left, right);
        return Metric.range((low + high) / 2, low, high);
    }

    private static int parseIntSafe(String raw) {
        if (raw == null) {
            return -1;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static int toScale(int maxValue) {
        if (maxValue <= 0) {
            return 2600;
        }
        int rounded = ((maxValue + 199) / 200) * 200;
        return Math.max(rounded, 1800);
    }

    static final class Metric {
        final int value;
        final int low;
        final int high;

        private Metric(int value, int low, int high) {
            this.value = value;
            this.low = low;
            this.high = high;
        }

        static Metric single(int value) {
            return new Metric(value, 0, 0);
        }

        static Metric range(int value, int low, int high) {
            return new Metric(value, low, high);
        }

        boolean isRange() {
            return low > 0 && high > 0;
        }

        String displayText() {
            if (isRange()) {
                return low + "-" + high + " kcal";
            }
            return value + " kcal";
        }
    }

    static final class CalorieVisualData {
        final Metric bmr;
        final Metric tdee;
        final Metric target;
        final int scaleMax;

        private CalorieVisualData(Metric bmr, Metric tdee, Metric target, int scaleMax) {
            this.bmr = bmr;
            this.tdee = tdee;
            this.target = target;
            this.scaleMax = scaleMax;
        }

        boolean hasVisualMetrics() {
            return bmr != null || tdee != null || target != null;
        }
    }
}
