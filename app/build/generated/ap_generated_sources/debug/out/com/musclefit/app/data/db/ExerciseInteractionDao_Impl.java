package com.musclefit.app.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ExerciseInteractionDao_Impl implements ExerciseInteractionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExerciseInteractionEntity> __insertionAdapterOfExerciseInteractionEntity;

  public ExerciseInteractionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExerciseInteractionEntity = new EntityInsertionAdapter<ExerciseInteractionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `exercise_interaction` (`exercise_id`,`account_id`,`liked`,`favorited`,`updated_at`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ExerciseInteractionEntity entity) {
        statement.bindLong(1, entity.exerciseId);
        if (entity.accountId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.accountId);
        }
        final int _tmp = entity.liked ? 1 : 0;
        statement.bindLong(3, _tmp);
        final int _tmp_1 = entity.favorited ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        statement.bindLong(5, entity.updatedAt);
      }
    };
  }

  @Override
  public void upsert(final ExerciseInteractionEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfExerciseInteractionEntity.insert(entity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public ExerciseInteractionEntity getByExerciseIdSync(final long exerciseId,
      final String accountId) {
    final String _sql = "SELECT * FROM exercise_interaction WHERE exercise_id = ? AND account_id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, exerciseId);
    _argIndex = 2;
    if (accountId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, accountId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfExerciseId = CursorUtil.getColumnIndexOrThrow(_cursor, "exercise_id");
      final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "account_id");
      final int _cursorIndexOfLiked = CursorUtil.getColumnIndexOrThrow(_cursor, "liked");
      final int _cursorIndexOfFavorited = CursorUtil.getColumnIndexOrThrow(_cursor, "favorited");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
      final ExerciseInteractionEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new ExerciseInteractionEntity();
        _result.exerciseId = _cursor.getLong(_cursorIndexOfExerciseId);
        if (_cursor.isNull(_cursorIndexOfAccountId)) {
          _result.accountId = null;
        } else {
          _result.accountId = _cursor.getString(_cursorIndexOfAccountId);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfLiked);
        _result.liked = _tmp != 0;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfFavorited);
        _result.favorited = _tmp_1 != 0;
        _result.updatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
