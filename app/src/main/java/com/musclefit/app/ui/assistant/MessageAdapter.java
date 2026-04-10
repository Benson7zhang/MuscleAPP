package com.musclefit.app.ui.assistant;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.musclefit.app.R;

public class MessageAdapter extends ListAdapter<Message, MessageAdapter.MessageViewHolder> {
    private final String assistantType;

    public MessageAdapter() {
        this("");
    }

    public MessageAdapter(String assistantType) {
        super(new DiffUtil.ItemCallback<Message>() {
            @Override
            public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                return oldItem.getTimestamp() == newItem.getTimestamp()
                        && oldItem.getSenderType() == newItem.getSenderType()
                        && oldItem.getStatus() == newItem.getStatus()
                        && oldItem.getContent().equals(newItem.getContent());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                return oldItem.getContent().equals(newItem.getContent())
                        && oldItem.getSenderType() == newItem.getSenderType()
                        && oldItem.getStatus() == newItem.getStatus();
            }
        });
        this.assistantType = assistantType == null ? "" : assistantType.trim();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.SENDER_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_ai, parent, false);
        return new AIMessageViewHolder(view, "nutrition".equals(assistantType));
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getSenderType();
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewHolder holder) {
        if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).clearLoadingAnimation();
        }
        super.onViewRecycled(holder);
    }

    abstract static class MessageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bind(Message message);
    }

    static class UserMessageViewHolder extends MessageViewHolder {
        private final TextView tvMessage;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
        }

        @Override
        void bind(Message message) {
            tvMessage.setText(message.getContent());
        }
    }

    static class AIMessageViewHolder extends MessageViewHolder {
        private final boolean nutritionMode;

        private final ImageButton btnCopyMessage;
        private final TextView tvMessage;
        private final TextView tvEvidence;
        private final MaterialCardView cardEvidence;

        private final MaterialCardView cardCalorieVisual;
        private final TextView tvCalorieBmrValue;
        private final TextView tvCalorieTdeeValue;
        private final TextView tvCalorieTargetValue;
        private final LinearProgressIndicator progressCalorieBmr;
        private final LinearProgressIndicator progressCalorieTdee;
        private final LinearProgressIndicator progressCalorieTarget;

        private final MaterialCardView cardRatioVisual;
        private final TextView tvRatioProteinValue;
        private final TextView tvRatioCarbValue;
        private final TextView tvRatioFatValue;
        private final LinearProgressIndicator progressRatioProtein;
        private final LinearProgressIndicator progressRatioCarb;
        private final LinearProgressIndicator progressRatioFat;

        private final TextView tvNutritionRealityCheck;

        private final android.os.Handler handler;
        private int dotCount = 0;
        private Runnable loadingRunnable;

        AIMessageViewHolder(@NonNull View itemView, boolean nutritionMode) {
            super(itemView);
            this.nutritionMode = nutritionMode;

            btnCopyMessage = itemView.findViewById(R.id.btn_copy_message);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvEvidence = itemView.findViewById(R.id.tv_message_evidence);
            cardEvidence = itemView.findViewById(R.id.card_evidence);

            cardCalorieVisual = itemView.findViewById(R.id.card_calorie_visual);
            tvCalorieBmrValue = itemView.findViewById(R.id.tv_calorie_bmr_value);
            tvCalorieTdeeValue = itemView.findViewById(R.id.tv_calorie_tdee_value);
            tvCalorieTargetValue = itemView.findViewById(R.id.tv_calorie_target_value);
            progressCalorieBmr = itemView.findViewById(R.id.progress_calorie_bmr);
            progressCalorieTdee = itemView.findViewById(R.id.progress_calorie_tdee);
            progressCalorieTarget = itemView.findViewById(R.id.progress_calorie_target);

            cardRatioVisual = itemView.findViewById(R.id.card_ratio_visual);
            tvRatioProteinValue = itemView.findViewById(R.id.tv_ratio_protein_value);
            tvRatioCarbValue = itemView.findViewById(R.id.tv_ratio_carb_value);
            tvRatioFatValue = itemView.findViewById(R.id.tv_ratio_fat_value);
            progressRatioProtein = itemView.findViewById(R.id.progress_ratio_protein);
            progressRatioCarb = itemView.findViewById(R.id.progress_ratio_carb);
            progressRatioFat = itemView.findViewById(R.id.progress_ratio_fat);

            tvNutritionRealityCheck = itemView.findViewById(R.id.tv_nutrition_reality_check);

            handler = new android.os.Handler(android.os.Looper.getMainLooper());
        }

        @Override
        void bind(Message message) {
            clearLoadingAnimation();
            btnCopyMessage.setOnClickListener(null);

            if (message.getStatus() == Message.STATUS_LOADING) {
                btnCopyMessage.setVisibility(View.GONE);
                cardEvidence.setVisibility(View.GONE);
                cardCalorieVisual.setVisibility(View.GONE);
                cardRatioVisual.setVisibility(View.GONE);
                tvNutritionRealityCheck.setVisibility(View.GONE);
                dotCount = 0;
                loadingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        dotCount = (dotCount + 1) % 4;
                        StringBuilder sb = new StringBuilder(itemView.getContext().getString(R.string.assistant_thinking));
                        for (int i = 0; i < dotCount; i++) {
                            sb.append('.');
                        }
                        tvMessage.setText(sb.toString());
                        handler.postDelayed(this, 500);
                    }
                };
                handler.post(loadingRunnable);
                return;
            }

            AiResponseCardParser.ParsedAiResponse parsed = AiResponseCardParser.parse(message.getContent());
            tvMessage.setText(parsed.primaryText);
            bindEvidence(parsed.evidenceText);
            bindNutritionVisual(parsed.rawText);
            bindCopyAction(parsed.rawText);
        }

        private void bindNutritionVisual(String rawText) {
            if (!nutritionMode) {
                cardCalorieVisual.setVisibility(View.GONE);
                cardRatioVisual.setVisibility(View.GONE);
                tvNutritionRealityCheck.setVisibility(View.GONE);
                return;
            }

            NutritionCalorieParser.CalorieVisualData calorieData = NutritionCalorieParser.parse(rawText);
            NutritionMacroParser.MacroVisualData ratioData = NutritionMacroParser.parse(rawText);

            bindCalorieCard(calorieData);
            bindRatioCard(ratioData);
            bindRealityCheck(calorieData, ratioData);
        }

        private void bindCalorieCard(NutritionCalorieParser.CalorieVisualData data) {
            if (data == null || !data.hasVisualMetrics()) {
                cardCalorieVisual.setVisibility(View.GONE);
                return;
            }

            cardCalorieVisual.setVisibility(View.VISIBLE);
            bindCalorieMetric(data.bmr, tvCalorieBmrValue, progressCalorieBmr, data.scaleMax);
            bindCalorieMetric(data.tdee, tvCalorieTdeeValue, progressCalorieTdee, data.scaleMax);
            bindCalorieMetric(data.target, tvCalorieTargetValue, progressCalorieTarget, data.scaleMax);
        }

        private void bindCalorieMetric(
                NutritionCalorieParser.Metric metric,
                TextView valueView,
                LinearProgressIndicator indicator,
                int max
        ) {
            if (metric == null) {
                valueView.setText("--");
                indicator.setMax(max);
                indicator.setProgress(0);
                return;
            }
            valueView.setText(metric.displayText());
            indicator.setMax(max);
            indicator.setProgress(Math.min(metric.value, max));
        }

        private void bindRatioCard(NutritionMacroParser.MacroVisualData data) {
            if (data == null || !data.hasVisualMetrics()) {
                cardRatioVisual.setVisibility(View.GONE);
                return;
            }

            cardRatioVisual.setVisibility(View.VISIBLE);
            bindRatioMetric(data.protein, tvRatioProteinValue, progressRatioProtein);
            bindRatioMetric(data.carb, tvRatioCarbValue, progressRatioCarb);
            bindRatioMetric(data.fat, tvRatioFatValue, progressRatioFat);
        }

        private void bindRatioMetric(
                NutritionMacroParser.MacroMetric metric,
                TextView valueView,
                LinearProgressIndicator indicator
        ) {
            indicator.setMax(100);
            if (metric == null) {
                valueView.setText("--");
                indicator.setProgress(0);
                return;
            }
            valueView.setText(metric.displayText());
            indicator.setProgress(Math.max(0, Math.min(metric.percent, 100)));
        }

        private void bindRealityCheck(
                NutritionCalorieParser.CalorieVisualData calorieData,
                NutritionMacroParser.MacroVisualData ratioData
        ) {
            NutritionRealityChecker.Result result = NutritionRealityChecker.check(calorieData, ratioData);
            if (result == null || !result.hasData || result.message.trim().isEmpty()) {
                tvNutritionRealityCheck.setVisibility(View.GONE);
                return;
            }

            tvNutritionRealityCheck.setVisibility(View.VISIBLE);
            tvNutritionRealityCheck.setText(result.message);
            int color = ContextCompat.getColor(
                    itemView.getContext(),
                    result.plausible ? R.color.mf_muted : R.color.mf_secondary
            );
            tvNutritionRealityCheck.setTextColor(color);
        }

        private void bindCopyAction(String rawText) {
            Context context = itemView.getContext();
            String content = rawText == null ? "" : rawText.trim();
            btnCopyMessage.setOnClickListener(v -> {
                if (content.isEmpty()) {
                    return;
                }
                ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (manager != null) {
                    manager.setPrimaryClip(ClipData.newPlainText("ai_message", content));
                    Toast.makeText(context, R.string.assistant_copy_success, Toast.LENGTH_SHORT).show();
                }
            });
            btnCopyMessage.setVisibility(content.isEmpty() ? View.GONE : View.VISIBLE);
        }

        private void bindEvidence(String evidence) {
            if (evidence == null || evidence.trim().isEmpty()) {
                cardEvidence.setVisibility(View.GONE);
                tvEvidence.setText("");
                return;
            }
            cardEvidence.setVisibility(View.VISIBLE);
            tvEvidence.setText(evidence.trim());
        }

        void clearLoadingAnimation() {
            if (loadingRunnable == null) {
                return;
            }
            handler.removeCallbacks(loadingRunnable);
            loadingRunnable = null;
        }
    }
}
