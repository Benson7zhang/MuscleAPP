package com.musclefit.app.ui.assistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NutritionistAssistantFragment extends BaseAssistantFragment {

    @Override
    protected String getAssistantType() {
        return "nutrition";
    }

    @Override
    protected String getWelcomeMessage() {
        return "你好！我是你的营养师助手，有什么可以帮助你的吗？你可以问我关于饮食计划、营养补充、卡路里计算等问题。";
    }

    @Override
    protected void initPresetOptions() {
        addPresetOption("饮食计划", "请为我制定一个增肌的饮食计划");
        addPresetOption("营养补充", "增肌需要哪些营养补充剂？");
        addPresetOption("卡路里计算", "如何计算每天需要的卡路里？");
        addPresetOption("食物搭配", "健康的食物搭配建议");
        addPresetOption("特殊饮食", "素食者如何合理搭配饮食？");
        addPresetOption("饮食时间", "一日三餐的最佳进食时间是什么时候？");
    }
}