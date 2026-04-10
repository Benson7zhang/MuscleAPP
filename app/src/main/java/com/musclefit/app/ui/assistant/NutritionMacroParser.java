package com.musclefit.app.ui.assistant;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class NutritionMacroParser {
    private static final String[] PROTEIN_KEYS = {"蛋白", "蛋白质", "protein"};
    private static final String[] CARB_KEYS = {"碳水", "碳水化合物", "carb", "carbohydrate"};
    private static final String[] FAT_KEYS = {"脂肪", "fat"};

    private static final Pattern RANGE_PERCENT_PATTERN = Pattern.compile("(\\d{1,2}(?:\\.\\d+)?)\\s*(?:~|～|-|—|–|到|至)\\s*(\\d{1,2}(?:\\.\\d+)?)\\s*%", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERCENT_PATTERN = Pattern.compile("(\\d{1,2}(?:\\.\\d+)?)\\s*%", Pattern.CASE_INSENSITIVE);
    private static final Pattern RATIO_PATTERN = Pattern.compile("(\\d{1,2}(?:\\.\\d+)?)\\s*[:：]\\s*(\\d{1,2}(?:\\.\\d+)?)\\s*[:：]\\s*(\\d{1,2}(?:\\.\\d+)?)");
    private static final int PROTEIN_MIN = 15;
    private static final int PROTEIN_MAX = 45;
    private static final int CARB_MIN = 20;
    private static final int CARB_MAX = 65;
    private static final int FAT_MIN = 15;
    private static final int FAT_MAX = 40;

    private NutritionMacroParser() {
    }

    static MacroVisualData parse(String rawText) {
        String content = rawText == null ? "" : rawText;
        MacroMetric protein = extractMetricByKeyword(content, PROTEIN_KEYS);
        MacroMetric carb = extractMetricByKeyword(content, CARB_KEYS);
        MacroMetric fat = extractMetricByKeyword(content, FAT_KEYS);

        if (protein == null || carb == null || fat == null) {
            RatioTriplet ratio = extractRatioTriplet(content);
            if (ratio != null) {
                MacroMetric ratioProtein = metricFromRatio(ratio.protein, ratio.sum);
                MacroMetric ratioCarb = metricFromRatio(ratio.carb, ratio.sum);
                MacroMetric ratioFat = metricFromRatio(ratio.fat, ratio.sum);
                if (protein == null) {
                    protein = ratioProtein;
                }
                if (carb == null) {
                    carb = ratioCarb;
                }
                if (fat == null) {
                    fat = ratioFat;
                }
            }
        }

        MacroMetric normalizedProtein = normalizeMetric(protein, PROTEIN_MIN, PROTEIN_MAX);
        MacroMetric normalizedCarb = normalizeMetric(carb, CARB_MIN, CARB_MAX);
        MacroMetric normalizedFat = normalizeMetric(fat, FAT_MIN, FAT_MAX);

        if (normalizedProtein != null && normalizedCarb != null && normalizedFat != null) {
            int proteinPercent = normalizedProtein.percent;
            int carbPercent = normalizedCarb.percent;
            int fatPercent = normalizedFat.percent;
            int[] balanced = rebalanceToHundred(proteinPercent, carbPercent, fatPercent);
            if (balanced != null && balanced.length == 3) {
                normalizedProtein = MacroMetric.single(balanced[0]);
                normalizedCarb = MacroMetric.single(balanced[1]);
                normalizedFat = MacroMetric.single(balanced[2]);
            }
        }

        return new MacroVisualData(normalizedProtein, normalizedCarb, normalizedFat);
    }

    private static MacroMetric normalizeMetric(MacroMetric metric, int min, int max) {
        if (metric == null) {
            return null;
        }
        if (!metric.isRange()) {
            return MacroMetric.single(clampPercentBand(metric.percent, min, max));
        }
        int low = clampPercentBand(metric.low, min, max);
        int high = clampPercentBand(metric.high, min, max);
        if (low > high) {
            int temp = low;
            low = high;
            high = temp;
        }
        int value = clampPercentBand(metric.percent, low, high);
        return MacroMetric.range(value, low, high);
    }

    private static int clampPercentBand(int value, int min, int max) {
        int safeMin = Math.max(0, Math.min(100, min));
        int safeMax = Math.max(0, Math.min(100, max));
        if (safeMin > safeMax) {
            int temp = safeMin;
            safeMin = safeMax;
            safeMax = temp;
        }
        return Math.max(safeMin, Math.min(safeMax, value));
    }

    private static int[] rebalanceToHundred(int protein, int carb, int fat) {
        int[] values = new int[]{protein, carb, fat};
        int[] mins = new int[]{PROTEIN_MIN, CARB_MIN, FAT_MIN};
        int[] maxs = new int[]{PROTEIN_MAX, CARB_MAX, FAT_MAX};

        int sum = values[0] + values[1] + values[2];
        if (sum == 100) {
            return values;
        }

        if (sum > 100) {
            int overflow = sum - 100;
            overflow = consume(values, new int[]{1, 2, 0}, mins, overflow, false);
        } else {
            int gap = 100 - sum;
            gap = consume(values, new int[]{1, 0, 2}, maxs, gap, true);
        }

        // Due to band ranges, 100 is always reachable. This is a final safety pass.
        int drift = 100 - (values[0] + values[1] + values[2]);
        if (drift != 0) {
            int idx = 1; // Prefer carb for tiny drift adjustments.
            values[idx] = clampPercentBand(values[idx] + drift, mins[idx], maxs[idx]);
        }
        return values;
    }

    private static int consume(int[] values, int[] order, int[] bounds, int amount, boolean increase) {
        int remain = Math.max(0, amount);
        for (int idx : order) {
            if (remain <= 0) {
                break;
            }
            int room;
            if (increase) {
                room = Math.max(0, bounds[idx] - values[idx]);
                int delta = Math.min(room, remain);
                values[idx] += delta;
                remain -= delta;
            } else {
                room = Math.max(0, values[idx] - bounds[idx]);
                int delta = Math.min(room, remain);
                values[idx] -= delta;
                remain -= delta;
            }
        }
        return remain;
    }

    private static MacroMetric extractMetricByKeyword(String content, String[] keywords) {
        if (content == null || content.trim().isEmpty() || keywords == null) {
            return null;
        }

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String safeLine = line == null ? "" : line.trim();
            if (safeLine.isEmpty()) {
                continue;
            }

            String lower = safeLine.toLowerCase(Locale.ROOT);
            boolean keywordMatched = false;
            for (String keyword : keywords) {
                if (keyword != null && lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                    keywordMatched = true;
                    break;
                }
            }
            if (!keywordMatched) {
                continue;
            }

            MacroMetric metric = parseMetricFromLine(safeLine);
            if (metric != null) {
                return metric;
            }
        }

        return null;
    }

    private static MacroMetric parseMetricFromLine(String line) {
        Matcher rangeMatcher = RANGE_PERCENT_PATTERN.matcher(line);
        if (rangeMatcher.find()) {
            double left = parseDoubleSafe(rangeMatcher.group(1));
            double right = parseDoubleSafe(rangeMatcher.group(2));
            if (left >= 0d && right >= 0d) {
                int low = (int) Math.round(Math.min(left, right));
                int high = (int) Math.round(Math.max(left, right));
                int value = (int) Math.round((low + high) / 2.0);
                return MacroMetric.range(clampPercent(value), clampPercent(low), clampPercent(high));
            }
        }

        Matcher percentMatcher = PERCENT_PATTERN.matcher(line);
        if (percentMatcher.find()) {
            double value = parseDoubleSafe(percentMatcher.group(1));
            if (value >= 0d) {
                return MacroMetric.single(clampPercent((int) Math.round(value)));
            }
        }

        return null;
    }

    private static RatioTriplet extractRatioTriplet(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String safeLine = line == null ? "" : line.trim();
            if (safeLine.isEmpty()) {
                continue;
            }
            String lower = safeLine.toLowerCase(Locale.ROOT);
            if (!(lower.contains("蛋白") && lower.contains("碳水") && lower.contains("脂肪"))
                    && !lower.contains("protein")
                    && !lower.contains("carb")
                    && !lower.contains("fat")) {
                continue;
            }
            Matcher matcher = RATIO_PATTERN.matcher(safeLine);
            if (matcher.find()) {
                double p = parseDoubleSafe(matcher.group(1));
                double c = parseDoubleSafe(matcher.group(2));
                double f = parseDoubleSafe(matcher.group(3));
                double sum = p + c + f;
                if (p > 0d && c > 0d && f > 0d && sum > 0d) {
                    return new RatioTriplet(p, c, f, sum);
                }
            }
        }
        return null;
    }

    private static MacroMetric metricFromRatio(double part, double sum) {
        if (part <= 0d || sum <= 0d) {
            return null;
        }
        int percent = clampPercent((int) Math.round((part / sum) * 100d));
        return MacroMetric.single(percent);
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static double parseDoubleSafe(String raw) {
        if (raw == null) {
            return -1d;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ignored) {
            return -1d;
        }
    }

    private static final class RatioTriplet {
        final double protein;
        final double carb;
        final double fat;
        final double sum;

        private RatioTriplet(double protein, double carb, double fat, double sum) {
            this.protein = protein;
            this.carb = carb;
            this.fat = fat;
            this.sum = sum;
        }
    }

    static final class MacroMetric {
        final int percent;
        final int low;
        final int high;

        private MacroMetric(int percent, int low, int high) {
            this.percent = percent;
            this.low = low;
            this.high = high;
        }

        static MacroMetric single(int percent) {
            return new MacroMetric(percent, -1, -1);
        }

        static MacroMetric range(int percent, int low, int high) {
            return new MacroMetric(percent, low, high);
        }

        boolean isRange() {
            return low >= 0 && high >= 0;
        }

        String displayText() {
            if (isRange()) {
                return low + "-" + high + "%";
            }
            return percent + "%";
        }
    }

    static final class MacroVisualData {
        final MacroMetric protein;
        final MacroMetric carb;
        final MacroMetric fat;

        private MacroVisualData(MacroMetric protein, MacroMetric carb, MacroMetric fat) {
            this.protein = protein;
            this.carb = carb;
            this.fat = fat;
        }

        boolean hasVisualMetrics() {
            return protein != null || carb != null || fat != null;
        }

        int sumPercent() {
            int sum = 0;
            if (protein != null) {
                sum += protein.percent;
            }
            if (carb != null) {
                sum += carb.percent;
            }
            if (fat != null) {
                sum += fat.percent;
            }
            return sum;
        }
    }
}
