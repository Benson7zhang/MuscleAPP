package com.musclefit.app.ui.assistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.musclefit.app.databinding.FragmentAssistantBaseBinding;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAssistantFragment extends Fragment {
    protected FragmentAssistantBaseBinding binding;
    protected MessageAdapter messageAdapter;
    protected List<Message> messages = new ArrayList<>();
    protected AIAssistantService aiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAssistantBaseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMessageList();
        initInputArea();
        initPresetOptions();
        aiService = new AIAssistantService(requireContext());
        addWelcomeMessage();
    }

    private void initMessageList() {
        messageAdapter = new MessageAdapter();
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void initInputArea() {
        binding.btnSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                binding.etMessage.setText("");
            }
        });
    }

    protected void sendMessage(String content) {
        Message userMessage = new Message(content, Message.SENDER_USER, System.currentTimeMillis());
        messages.add(userMessage);
        messageAdapter.submitList(new ArrayList<>(messages));
        binding.rvMessages.scrollToPosition(messages.size() - 1);

        // 显示加载状态
        Message loadingMessage = new Message("正在思考...", Message.SENDER_AI, System.currentTimeMillis(), Message.STATUS_LOADING);
        messages.add(loadingMessage);
        messageAdapter.submitList(new ArrayList<>(messages));
        binding.rvMessages.scrollToPosition(messages.size() - 1);

        // 调用AI服务
        aiService.sendMessage(getAssistantType(), content, new AIAssistantService.Callback() {
            @Override
            public void onSuccess(String response) {
                if (binding == null) {
                    return;
                }
                // 移除加载消息
                messages.remove(loadingMessage);
                // 添加AI回复
                Message aiMessage = new Message(response, Message.SENDER_AI, System.currentTimeMillis());
                messages.add(aiMessage);
                messageAdapter.submitList(new ArrayList<>(messages));
                binding.rvMessages.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onError(String error) {
                if (binding == null) {
                    return;
                }
                // 移除加载消息
                messages.remove(loadingMessage);
                // 添加错误消息
                Message errorMessage = new Message("抱歉，我暂时无法回答这个问题。请稍后再试。", Message.SENDER_AI, System.currentTimeMillis());
                messages.add(errorMessage);
                messageAdapter.submitList(new ArrayList<>(messages));
                binding.rvMessages.scrollToPosition(messages.size() - 1);
                Toast.makeText(requireContext(), "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void addWelcomeMessage() {
        Message welcomeMessage = new Message(getWelcomeMessage(), Message.SENDER_AI, System.currentTimeMillis());
        messages.add(welcomeMessage);
        messageAdapter.submitList(new ArrayList<>(messages));
    }

    protected abstract String getAssistantType();
    protected abstract String getWelcomeMessage();
    protected abstract void initPresetOptions();

    protected void addPresetOption(String text, String question) {
        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
        chip.setText(text);
        chip.setOnClickListener(v -> {
            sendMessage(question);
        });
        binding.chipGroupPreset.addView(chip);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}