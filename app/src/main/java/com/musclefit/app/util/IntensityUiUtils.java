package com.musclefit.app.util;

import androidx.annotation.ColorRes;

import com.musclefit.app.R;

import java.util.List;
import java.util.StringJoiner;

public final class IntensityUiUtils {
    private IntensityUiUtils() {
    }

    @ColorRes
    public static int levelColorRes(int level) {
        switch (level) {
            case 1:
                return R.color.intensity_l1;
            case 2:
                return R.color.intensity_l2;
            case 3:
                return R.color.intensity_l3;
            case 4:
                return R.color.intensity_l4;
            case 5:
            default:
                return R.color.intensity_l5;
        }
    }

    public static String levelText(int level) {
        return "L" + Math.max(1, Math.min(5, level));
    }

    public static String formatIntensityNotes(List<String> notes) {
        StringJoiner joiner = new StringJoiner("\n");
        for (String line : notes) {
            joiner.add(line);
        }
        return joiner.toString();
    }
}
