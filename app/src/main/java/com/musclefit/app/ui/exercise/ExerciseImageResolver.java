package com.musclefit.app.ui.exercise;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.musclefit.app.R;
import com.musclefit.app.data.model.ExerciseCard;

final class ExerciseImageResolver {
    private ExerciseImageResolver() {
    }

    @DrawableRes
    static int resolveCardImage(@NonNull ExerciseCard card) {
        return chooseImage(card, false);
    }

    @DrawableRes
    static int resolveDetailImage(@NonNull ExerciseCard card) {
        return chooseImage(card, true);
    }

    @DrawableRes
    private static int chooseImage(@NonNull ExerciseCard card, boolean detail) {
        String hint = safe(card.name) + " " + safe(card.primaryMuscle) + " " + safe(card.categoryHint);

        if (containsAny(hint, "胸")) {
            return detail ? R.drawable.ex_chest_03 : R.drawable.ex_chest_01;
        }
        if (containsAny(hint, "背", "斜方", "背阔")) {
            if (containsAny(hint, "三头", "肱三头")) {
                return R.drawable.ex_back_triceps_01;
            }
            return R.drawable.ex_back_01;
        }
        if (containsAny(hint, "肩", "三角")) {
            return R.drawable.ex_shoulder_01;
        }
        if (containsAny(hint, "臂", "肱二", "肱三", "前臂", "三头")) {
            return R.drawable.ex_triceps_01;
        }
        if (containsAny(hint, "核心", "腹")) {
            return detail ? R.drawable.ex_core_02 : R.drawable.ex_core_01;
        }
        if (containsAny(hint, "腿", "股", "腘", "小腿", "腓肠", "比目鱼")) {
            return detail ? R.drawable.ex_legs_calf_01 : R.drawable.ex_legs_thigh_01;
        }
        return detail ? R.drawable.ex_fullbody_02 : R.drawable.ex_fullbody_01;
    }

    private static boolean containsAny(String text, String... keywords) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isEmpty() && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
