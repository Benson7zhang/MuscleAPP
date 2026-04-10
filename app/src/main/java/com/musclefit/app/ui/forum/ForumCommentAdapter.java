package com.musclefit.app.ui.forum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.musclefit.app.R;
import com.musclefit.app.data.model.ForumCommentItem;
import com.musclefit.app.databinding.ItemForumCommentBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForumCommentAdapter extends ListAdapter<ForumCommentItem, ForumCommentAdapter.CommentHolder> {
    public interface Listener {
        boolean canDelete(ForumCommentItem comment);

        void onDeleteClick(ForumCommentItem comment);
    }

    private final Listener listener;

    public ForumCommentAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<ForumCommentItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ForumCommentItem oldItem, @NonNull ForumCommentItem newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ForumCommentItem oldItem, @NonNull ForumCommentItem newItem) {
                return textEq(oldItem.content, newItem.content)
                        && textEq(oldItem.authorAccountId, newItem.authorAccountId)
                        && textEq(oldItem.authorNickname, newItem.authorNickname)
                        && oldItem.createdAt == newItem.createdAt;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemForumCommentBinding binding = ItemForumCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class CommentHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private final ItemForumCommentBinding binding;
        private final Listener listener;

        CommentHolder(ItemForumCommentBinding binding, Listener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(ForumCommentItem comment) {
            String nickname = comment.authorNickname == null || comment.authorNickname.trim().isEmpty()
                    ? comment.authorAccountId
                    : comment.authorNickname;
            binding.tvForumCommentMeta.setText(itemView.getContext().getString(
                    R.string.forum_comment_meta,
                    nickname,
                    comment.authorAccountId,
                    formatTime(comment.createdAt)
            ));
            binding.tvForumCommentContent.setText(comment.content);

            boolean canDelete = listener != null && listener.canDelete(comment);
            binding.btnForumCommentDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
            binding.btnForumCommentDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(comment);
                }
            });
        }

        private static String formatTime(long epochMs) {
            return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date(epochMs));
        }
    }

    private static boolean textEq(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
