package com.musclefit.app.ui.assistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TrainingAssistantFragment extends BaseAssistantFragment {

    @Override
    protected String getAssistantType() {
        return "training";
    }

    @Override
    protected String getWelcomeMessage() {
        return "你好！我是你的训练助手，有什么可以帮助你的吗？你可以问我关于训练计划、动作指导、训练频率等问题。";
    }

    @Override
    protected void initPresetOptions() {
        addPresetOption("训练计划", "请为我制定一个每周4天的训练计划");
        addPresetOption("动作指导", "如何正确做深蹲？");
        addPresetOption("训练频率", "每周应该训练几次？");
        addPresetOption("训练强度", "如何判断训练强度是否合适？");
        addPresetOption("恢复建议", "训练后如何有效恢复？");
        addPresetOption("训练装备", "新手需要哪些基本训练装备？");
    }
}