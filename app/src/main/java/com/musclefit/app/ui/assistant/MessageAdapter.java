package com.musclefit.app.ui.assistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.musclefit.app.R;

import java.util.List;

public class MessageAdapter extends ListAdapter<Message, MessageAdapter.MessageViewHolder> {

    public MessageAdapter() {
        super(new DiffUtil.ItemCallback<Message>() {
            @Override
            public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                return oldItem.getTimestamp() == newItem.getTimestamp();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                return oldItem.getContent().equals(newItem.getContent()) &&
                        oldItem.getSenderType() == newItem.getSenderType() &&
                        oldItem.getStatus() == newItem.getStatus();
            }
        });
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.SENDER_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_ai, parent, false);
            return new AIMessageViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getSenderType();
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = getItem(position);
        holder.bind(message);
    }

    abstract static class MessageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bind(Message message);
    }

    static class UserMessageViewHolder extends MessageViewHolder {
        private TextView tvMessage;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
        }

        @Override
        void bind(Message message) {
            tvMessage.setText(message.getContent());
        }
    }

    static class AIMessageViewHolder extends MessageViewHolder {
        private TextView tvMessage;
        private android.os.Handler handler;
        private int dotCount = 0;
        private Runnable loadingRunnable;

        public AIMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            handler = new android.os.Handler();
        }

        @Override
        void bind(Message message) {
            // 清除之前的动画
            if (loadingRunnable != null) {
                handler.removeCallbacks(loadingRunnable);
            }

            if (message.getStatus() == Message.STATUS_LOADING) {
                // 启动加载动画
                dotCount = 0;
                loadingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        dotCount = (dotCount + 1) % 4;
                        StringBuilder sb = new StringBuilder("正在思考");
                        for (int i = 0; i < dotCount; i++) {
                            sb.append(".");
                        }
                        tvMessage.setText(sb.toString());
                        handler.postDelayed(this, 500);
                    }
                };
                handler.post(loadingRunnable);
            } else {
                // 显示正常消息
                tvMessage.setText(message.getContent());
            }
        }
    }
}