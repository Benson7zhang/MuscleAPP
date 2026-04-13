package com.musclefit.app.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.musclefit.app.data.model.ForumCommentItem;
import com.musclefit.app.data.model.ForumPostCard;

import java.util.List;

@Dao
public interface ForumDao {
    @Query("SELECT p.id AS id, p.author_account_id AS authorAccountId, p.author_nickname AS authorNickname, " +
            "p.title AS title, p.content AS content, p.image_uris AS imageUris, " +
            "p.like_count AS likeCount, p.comment_count AS commentCount, " +
            "p.favorite_count AS favoriteCount, " +
            "p.created_at AS createdAt, p.updated_at AS updatedAt, " +
            "CASE WHEN l.post_id IS NULL THEN 0 ELSE 1 END AS likedByMe, " +
            "CASE WHEN f.post_id IS NULL THEN 0 ELSE 1 END AS favoritedByMe " +
            "FROM forum_post p " +
            "LEFT JOIN forum_post_like l ON p.id = l.post_id AND l.account_id = :accountId " +
            "LEFT JOIN forum_post_favorite f ON p.id = f.post_id AND f.account_id = :accountId " +
            "ORDER BY p.updated_at DESC, p.id DESC")
    LiveData<List<ForumPostCard>> observePosts(String accountId);

    @Query("SELECT p.id AS id, p.author_account_id AS authorAccountId, p.author_nickname AS authorNickname, " +
            "p.title AS title, p.content AS content, p.image_uris AS imageUris, " +
            "p.like_count AS likeCount, p.comment_count AS commentCount, " +
            "p.favorite_count AS favoriteCount, " +
            "p.created_at AS createdAt, p.updated_at AS updatedAt, " +
            "CASE WHEN l.post_id IS NULL THEN 0 ELSE 1 END AS likedByMe, " +
            "CASE WHEN f.post_id IS NULL THEN 0 ELSE 1 END AS favoritedByMe " +
            "FROM forum_post p " +
            "LEFT JOIN forum_post_like l ON p.id = l.post_id AND l.account_id = :accountId " +
            "LEFT JOIN forum_post_favorite f ON p.id = f.post_id AND f.account_id = :accountId " +
            "WHERE p.id = :postId LIMIT 1")
    LiveData<ForumPostCard> observePost(long postId, String accountId);

    @Query("SELECT * FROM forum_post WHERE id = :postId LIMIT 1")
    ForumPostEntity getPostByIdSync(long postId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPost(ForumPostEntity post);

    @Query("SELECT COUNT(*) FROM forum_post WHERE author_account_id = :authorAccountId AND title = :title")
    int countPostsByAuthorAndTitle(String authorAccountId, String title);

    @Query("UPDATE forum_post SET title = :title, content = :content, updated_at = :updatedAt WHERE id = :postId")
    int updatePostContent(long postId, String title, String content, long updatedAt);

    @Query("DELETE FROM forum_post WHERE id = :postId")
    int deletePostById(long postId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertComment(ForumCommentEntity comment);

    @Query("SELECT id AS id, post_id AS postId, author_account_id AS authorAccountId, " +
            "author_nickname AS authorNickname, content AS content, created_at AS createdAt " +
            "FROM forum_comment WHERE post_id = :postId ORDER BY created_at ASC, id ASC")
    LiveData<List<ForumCommentItem>> observeComments(long postId);

    @Query("SELECT * FROM forum_comment WHERE id = :commentId LIMIT 1")
    ForumCommentEntity getCommentByIdSync(long commentId);

    @Query("DELETE FROM forum_comment WHERE id = :commentId")
    int deleteCommentById(long commentId);

    @Query("SELECT COUNT(*) FROM forum_comment WHERE post_id = :postId")
    int countCommentsByPostId(long postId);

    @Query("SELECT * FROM forum_post_like WHERE post_id = :postId AND account_id = :accountId LIMIT 1")
    ForumPostLikeEntity getLikeSync(long postId, String accountId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertLike(ForumPostLikeEntity likeEntity);

    @Query("DELETE FROM forum_post_like WHERE post_id = :postId AND account_id = :accountId")
    int deleteLike(long postId, String accountId);

    @Query("UPDATE forum_post SET like_count = :likeCount WHERE id = :postId")
    void updatePostLikeCount(long postId, int likeCount);

    @Query("SELECT * FROM forum_post_favorite WHERE post_id = :postId AND account_id = :accountId LIMIT 1")
    ForumPostFavoriteEntity getFavoriteSync(long postId, String accountId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertFavorite(ForumPostFavoriteEntity favoriteEntity);

    @Query("DELETE FROM forum_post_favorite WHERE post_id = :postId AND account_id = :accountId")
    int deleteFavorite(long postId, String accountId);

    @Query("UPDATE forum_post SET favorite_count = :favoriteCount WHERE id = :postId")
    void updatePostFavoriteCount(long postId, int favoriteCount);

    @Query("UPDATE forum_post SET comment_count = :commentCount, updated_at = :updatedAt WHERE id = :postId")
    void updatePostCommentCount(long postId, int commentCount, long updatedAt);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReport(ForumReportEntity report);
}
