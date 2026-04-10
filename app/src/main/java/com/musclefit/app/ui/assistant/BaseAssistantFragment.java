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

import com.musclefit.app.R;
import com.musclefit.app.auth.AuthManager;
import com.musclefit.app.databinding.FragmentAssistantBaseBinding;
import com.musclefit.app.repo.AssistantHistoryRepository;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAssistantFragment extends Fragment {
    protected FragmentAssistantBaseBinding binding;
    protected MessageAdapter messageAdapter;
    protected List<Message> messages = new ArrayList<>();
    protected AIAssistantService aiService;
    protected AssistantHistoryRepository historyRepository;
    protected AuthManager authManager;
    private boolean requestInFlight;
    private String currentScopeAccountId = "guest";

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
        historyRepository = AssistantHistoryRepository.getInstance(requireContext());
        authManager = AuthManager.getInstance(requireContext());
        currentScopeAccountId = historyRepository.currentAccountId();
        loadConversationForCurrentScope();

        authManager.observe().observe(getViewLifecycleOwner(), state -> {
            String nextScope = historyRepository.currentAccountId();
            if (nextScope.equals(currentScopeAccountId)) {
                return;
            }
            currentScopeAccountId = nextScope;
            setRequestInFlight(false);
            loadConversationForCurrentScope();
        });
    }

    private void initMessageList() {
        messageAdapter = new MessageAdapter(getAssistantType());
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void initInputArea() {
        binding.btnSend.setOnClickListener(v -> {
            if (requestInFlight) {
                Toast.makeText(requireContext(), R.string.action_processing, Toast.LENGTH_SHORT).show();
                return;
            }
            String message = binding.etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                binding.etMessage.setText("");
            }
        });
    }

    protected void sendMessage(String content) {
        if (binding == null) {
            return;
        }
        if (requestInFlight) {
            Toast.makeText(requireContext(), R.string.action_processing, Toast.LENGTH_SHORT).show();
            return;
        }
        String safeContent = content == null ? "" : content.trim();
        if (safeContent.isEmpty()) {
            return;
        }
        List<Message> contextMessages = buildConversationContext();
        String requestScopeAccountId = currentScopeAccountId;

        Message userMessage = new Message(safeContent, Message.SENDER_USER, System.currentTimeMillis());
        messages.add(userMessage);
        messageAdapter.submitList(new ArrayList<>(messages));
        scrollToBottom();
        historyRepository.appendMessage(getAssistantType(), userMessage);

        Message loadingMessage = new Message(
                getString(R.string.assistant_thinking),
                Message.SENDER_AI,
                System.currentTimeMillis(),
                Message.STATUS_LOADING
        );
        messages.add(loadingMessage);
        messageAdapter.submitList(new ArrayList<>(messages));
        scrollToBottom();
        setRequestInFlight(true);

        aiService.sendMessage(getAssistantType(), safeContent, contextMessages, new AIAssistantService.Callback() {
            @Override
            public void onSuccess(String response) {
                if (binding == null) {
                    return;
                }
                if (!requestScopeAccountId.equals(currentScopeAccountId)) {
                    setRequestInFlight(false);
                    return;
                }
                messages.remove(loadingMessage);
                Message aiMessage = new Message(response, Message.SENDER_AI, System.currentTimeMillis());
                messages.add(aiMessage);
                messageAdapter.submitList(new ArrayList<>(messages));
                scrollToBottom();
                historyRepository.appendMessage(getAssistantType(), aiMessage);
                setRequestInFlight(false);
            }

            @Override
            public void onError(String error) {
                if (binding == null) {
                    return;
                }
                if (!requestScopeAccountId.equals(currentScopeAccountId)) {
                    setRequestInFlight(false);
                    return;
                }
                messages.remove(loadingMessage);
                Message errorMessage = new Message(
                        getString(R.string.assistant_error_fallback),
                        Message.SENDER_AI,
                        System.currentTimeMillis(),
                        Message.STATUS_ERROR
                );
                messages.add(errorMessage);
                messageAdapter.submitList(new ArrayList<>(messages));
                scrollToBottom();
                historyRepository.appendMessage(getAssistantType(), errorMessage);
                setRequestInFlight(false);

                String hint = (error == null || error.trim().isEmpty())
                        ? getString(R.string.action_failed)
                        : error;
                Toast.makeText(requireContext(), hint, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void addWelcomeMessage() {
        Message welcomeMessage = new Message(getWelcomeMessage(), Message.SENDER_AI, System.currentTimeMillis());
        messages.add(welcomeMessage);
        messageAdapter.submitList(new ArrayList<>(messages));
        historyRepository.appendMessage(getAssistantType(), welcomeMessage);
    }

    private void loadConversationForCurrentScope() {
        if (historyRepository == null) {
            return;
        }
        historyRepository.loadConversation(getAssistantType(), loaded -> {
            if (binding == null) {
                return;
            }
            messages.clear();
            if (loaded == null || loaded.isEmpty()) {
                addWelcomeMessage();
            } else {
                messages.addAll(loaded);
                messageAdapter.submitList(new ArrayList<>(messages));
            }
            scrollToBottom();
        });
    }

    private List<Message> buildConversationContext() {
        List<Message> context = new ArrayList<>();
        for (Message message : messages) {
            if (message == null) {
                continue;
            }
            if (message.getStatus() == Message.STATUS_LOADING) {
                continue;
            }
            context.add(message);
        }
        return context;
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

    private void setRequestInFlight(boolean inFlight) {
        requestInFlight = inFlight;
        if (binding == null) {
            return;
        }
        binding.btnSend.setEnabled(!inFlight);
        binding.etMessage.setEnabled(!inFlight);
    }

    private void scrollToBottom() {
        if (binding == null || messages.isEmpty()) {
            return;
        }
        binding.rvMessages.scrollToPosition(messages.size() - 1);
    }

    @Override
    public void onDestroyView() {
        requestInFlight = false;
        if (binding != null) {
            binding.rvMessages.setAdapter(null);
        }
        super.onDestroyView();
        binding = null;
    }
}
