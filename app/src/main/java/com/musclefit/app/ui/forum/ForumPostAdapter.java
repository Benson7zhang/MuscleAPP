package com.musclefit.app.ui.forum;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.musclefit.app.R;
import com.musclefit.app.data.model.ForumPostCard;
import com.musclefit.app.databinding.ItemForumPostBinding;
import com.musclefit.app.repo.ForumImageCodec;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForumPostAdapter extends ListAdapter<ForumPostCard, ForumPostAdapter.PostHolder> {
    public interface Listener {
        void onPostClick(ForumPostCard post);

        void onLikeClick(ForumPostCard post);

        void onFavoriteClick(ForumPostCard post);

        void onCommentClick(ForumPostCard post);
    }

    private final Listener listener;

    public ForumPostAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<ForumPostCard>() {
            @Override
            public boolean areItemsTheSame(@NonNull ForumPostCard oldItem, @NonNull ForumPostCard newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ForumPostCard oldItem, @NonNull ForumPostCard newItem) {
                return oldItem.likeCount == newItem.likeCount
                        && oldItem.commentCount == newItem.commentCount
                        && oldItem.favoriteCount == newItem.favoriteCount
                        && oldItem.likedByMe == newItem.likedByMe
                        && oldItem.favoritedByMe == newItem.favoritedByMe
                        && textEq(oldItem.title, newItem.title)
                        && textEq(oldItem.content, newItem.content)
                        && textEq(oldItem.imageUris, newItem.imageUris)
                        && textEq(oldItem.authorAccountId, newItem.authorAccountId)
                        && textEq(oldItem.authorNickname, newItem.authorNickname)
                        && oldItem.updatedAt == newItem.updatedAt;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemForumPostBinding binding = ItemForumPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class PostHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private static final int[] COVER_HEIGHT_DP = new int[]{152, 168, 186, 204, 176, 194};

        private final ItemForumPostBinding binding;
        private final Listener listener;

        PostHolder(ItemForumPostBinding binding, Listener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(ForumPostCard post) {
            binding.tvForumPostTitle.setText(post.title);
            binding.tvForumPostContent.setText(post.content);
            List<String> imageUris = ForumImageCodec.decode(post.imageUris);
            if (imageUris.isEmpty()) {
                binding.ivForumCoverImage.setVisibility(View.GONE);
                binding.viewForumCover.setVisibility(View.VISIBLE);
                binding.viewForumCover.setBackgroundResource(coverBackground(post.id));
            } else {
                binding.ivForumCoverImage.setVisibility(View.VISIBLE);
                binding.viewForumCover.setVisibility(View.GONE);
                ForumImageLoader.load(binding.ivForumCoverImage, imageUris.get(0), coverBackground(post.id));
            }
            binding.tvForumPostMeta.setText(itemView.getContext().getString(
                    R.string.forum_post_meta_brief,
                    formatTime(post.updatedAt)
            ));

            int coverHeight = dpToPx(COVER_HEIGHT_DP[(int) (Math.abs(post.id) % COVER_HEIGHT_DP.length)]);
            ViewGroup.LayoutParams coverLayoutParams = binding.layoutForumCover.getLayoutParams();
            if (coverLayoutParams.height != coverHeight) {
                coverLayoutParams.height = coverHeight;
                binding.layoutForumCover.setLayoutParams(coverLayoutParams);
            }

            int likeLabel = post.likedByMe ? R.string.forum_liked_count : R.string.forum_like_count;
            binding.btnForumLike.setText(itemView.getContext().getString(likeLabel, post.likeCount));
            int favoriteLabel = post.favoritedByMe ? R.string.forum_favorited_count : R.string.forum_favorite_count;
            binding.btnForumFavorite.setText(itemView.getContext().getString(favoriteLabel, post.favoriteCount));
            binding.btnForumComment.setText(itemView.getContext().getString(R.string.forum_comment_count, post.commentCount));

            int activeColor = itemView.getContext().getColor(R.color.mf_primary);
            int normalColor = itemView.getContext().getColor(R.color.mf_muted);
            binding.btnForumLike.setTextColor(post.likedByMe ? activeColor : normalColor);
            binding.btnForumFavorite.setTextColor(post.favoritedByMe ? activeColor : normalColor);

            View.OnClickListener openDetail = v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            };
            itemView.setOnClickListener(openDetail);
            binding.tvForumPostContent.setOnClickListener(openDetail);

            binding.btnForumLike.setOnClickListener(v -> {
                v.setEnabled(false);
                v.postDelayed(() -> v.setEnabled(true), 500L);
                if (listener != null) {
                    listener.onLikeClick(post);
                }
            });

            binding.btnForumFavorite.setOnClickListener(v -> {
                v.setEnabled(false);
                v.postDelayed(() -> v.setEnabled(true), 500L);
                if (listener != null) {
                    listener.onFavoriteClick(post);
                }
            });

            binding.btnForumComment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(post);
                }
            });
        }

        private static int coverBackground(long postId) {
            int[] backgrounds = new int[]{
                    R.drawable.bg_forum_cover_1,
                    R.drawable.bg_forum_cover_2,
                    R.drawable.bg_forum_cover_3,
                    R.drawable.bg_forum_cover_4,
                    R.drawable.bg_forum_cover_5,
                    R.drawable.bg_forum_cover_6
            };
            int index = (int) (Math.abs(postId) % backgrounds.length);
            return backgrounds[index];
        }

        private int dpToPx(int dp) {
            return Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    itemView.getResources().getDisplayMetrics()
            ));
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
