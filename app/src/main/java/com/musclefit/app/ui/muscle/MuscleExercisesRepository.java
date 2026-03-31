package com.musclefit.app.ui.muscle;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MuscleExercisesRepository {
    private static final String ASSET_FILE = "muscle_exercises.json";

    private final Context appContext;
    private JSONObject rootCache;

    public MuscleExercisesRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public synchronized MuscleAreaData loadArea(String muscleKey, String fallbackSide) throws Exception {
        if (muscleKey == null || muscleKey.trim().isEmpty()) {
            throw new IllegalArgumentException("missing_muscle_key");
        }
        if (rootCache == null) {
            rootCache = new JSONObject(readAsset());
        }

        JSONObject areaObj = rootCache.optJSONObject(muscleKey);
        if (areaObj == null) {
            throw new IllegalArgumentException("area_not_found");
        }

        String areaName = areaObj.optString("name", muscleKey);
        String view = areaObj.optString("view", fallbackSide == null ? "front" : fallbackSide);

        List<MuscleExerciseItem> exercises = new ArrayList<>();
        JSONArray arr = areaObj.optJSONArray("exercises");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject ex = arr.optJSONObject(i);
                if (ex == null) {
                    continue;
                }
                exercises.add(new MuscleExerciseItem(
                        ex.optString("id", ""),
                        ex.optString("name", ""),
                        ex.optString("image", ""),
                        ex.optString("description", ""),
                        parseTips(ex.optJSONArray("tips"))
                ));
            }
        }

        if (exercises.isEmpty()) {
            throw new IllegalArgumentException("empty_exercises");
        }

        return new MuscleAreaData(areaName, view, exercises);
    }

    private List<String> parseTips(JSONArray tipsArray) {
        List<String> tips = new ArrayList<>();
        if (tipsArray == null) {
            return tips;
        }
        for (int i = 0; i < tipsArray.length(); i++) {
            String tip = tipsArray.optString(i, "").trim();
            if (!tip.isEmpty()) {
                tips.add(tip);
            }
        }
        return tips;
    }

    private String readAsset() throws Exception {
        InputStream input = appContext.getAssets().open(ASSET_FILE);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        input.close();
        return output.toString(StandardCharsets.UTF_8.name());
    }

    public static class MuscleAreaData {
        public final String areaName;
        public final String view;
        public final List<MuscleExerciseItem> exercises;

        public MuscleAreaData(String areaName, String view, List<MuscleExerciseItem> exercises) {
            this.areaName = areaName;
            this.view = view;
            this.exercises = exercises;
        }
    }
}
