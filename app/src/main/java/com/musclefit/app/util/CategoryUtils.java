package com.musclefit.app.util;

import android.content.Context;

import com.musclefit.app.R;

import java.util.Arrays;
import java.util.List;

public final class CategoryUtils {
    public static final String ALL = "ALL";
    public static final String BODYWEIGHT = "BODYWEIGHT";
    public static final String DUMBBELL = "DUMBBELL";
    public static final String BARBELL = "BARBELL";
    public static final String MACHINE = "MACHINE";
    public static final String CABLE = "CABLE";

    private CategoryUtils() {
    }

    public static List<String> allCategories() {
        return Arrays.asList(ALL, BODYWEIGHT, DUMBBELL, BARBELL, MACHINE, CABLE);
    }

    public static List<String> trainingCategories() {
        return Arrays.asList(BODYWEIGHT, DUMBBELL, BARBELL, MACHINE, CABLE);
    }

    public static String displayCategory(Context context, String category) {
        return context.getString(categoryLabelRes(category));
    }

    public static int categoryLabelRes(String category) {
        if (BODYWEIGHT.equals(category)) {
            return R.string.category_bodyweight;
        }
        if (DUMBBELL.equals(category)) {
            return R.string.category_dumbbell;
        }
        if (BARBELL.equals(category)) {
            return R.string.category_barbell;
        }
        if (MACHINE.equals(category)) {
            return R.string.category_machine;
        }
        if (CABLE.equals(category)) {
            return R.string.category_cable;
        }
        return R.string.category_all;
    }

    public static String displayMovement(Context context, String movementType) {
        return context.getString(movementLabelRes(movementType));
    }

    public static int movementLabelRes(String movementType) {
        if ("ISOLATION".equals(movementType)) {
            return R.string.movement_isolation;
        }
        if ("COMPOUND".equals(movementType)) {
            return R.string.movement_compound;
        }
        return R.string.movement_unknown;
    }
}
