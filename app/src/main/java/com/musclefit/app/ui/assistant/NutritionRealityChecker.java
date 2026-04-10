package com.musclefit.app.ui.assistant;

import java.util.ArrayList;
import java.util.List;

final class NutritionRealityChecker {
    private NutritionRealityChecker() {
    }

    static Result check(
            NutritionCalorieParser.CalorieVisualData calorieData,
            NutritionMacroParser.MacroVisualData macroData
    ) {
        List<String> issues = new ArrayList<>();
        boolean hasData = false;

        if (calorieData != null) {
            if (calorieData.bmr != null) {
                hasData = true;
                if (calorieData.bmr.value < 900 || calorieData.bmr.value > 3200) {
                    issues.add("BMR 超出常见范围");
                }
            }

            if (calorieData.tdee != null) {
                hasData = true;
                if (calorieData.tdee.value < 1200 || calorieData.tdee.value > 5200) {
                    issues.add("TDEE 超出常见范围");
                }
            }

            if (calorieData.target != null) {
                hasData = true;
                if (calorieData.target.value < 1000 || calorieData.target.value > 4500) {
                    issues.add("目标热量偏离常见区间");
                }
                if (calorieData.bmr != null && calorieData.target.value < (int) (calorieData.bmr.value * 0.75f)) {
                    issues.add("目标热量低于基础代谢较多");
                }
            }
        }

        if (macroData != null && macroData.hasVisualMetrics()) {
            hasData = true;
            int sum = macroData.sumPercent();
            if (sum > 0 && (sum < 90 || sum > 110)) {
                issues.add("三大营养素比例总和不是 100% 左右");
            }
            checkMacroMetric(macroData.protein, "蛋白质", issues);
            checkMacroMetric(macroData.carb, "碳水", issues);
            checkMacroMetric(macroData.fat, "脂肪", issues);
        }

        if (!hasData) {
            return new Result(false, false, "");
        }

        if (issues.isEmpty()) {
            return new Result(true, true, "现实性校验：当前数值处于常见建议范围");
        }

        StringBuilder msg = new StringBuilder("现实性校验：建议复核（");
        for (int i = 0; i < issues.size() && i < 2; i++) {
            if (i > 0) {
                msg.append("；");
            }
            msg.append(issues.get(i));
        }
        if (issues.size() > 2) {
            msg.append("；还有 ").append(issues.size() - 2).append(" 项");
        }
        msg.append("）");
        return new Result(true, false, msg.toString());
    }

    private static void checkMacroMetric(NutritionMacroParser.MacroMetric metric, String name, List<String> issues) {
        if (metric == null) {
            return;
        }
        if (metric.percent < 5 || metric.percent > 80) {
            issues.add(name + "比例偏离常见区间");
        }
    }

    static final class Result {
        final boolean hasData;
        final boolean plausible;
        final String message;

        private Result(boolean hasData, boolean plausible, String message) {
            this.hasData = hasData;
            this.plausible = plausible;
            this.message = message == null ? "" : message;
        }
    }
}
