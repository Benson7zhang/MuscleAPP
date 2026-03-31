package com.musclefit.app.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.musclefit.app.data.model.ExerciseIntensityNote;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ExerciseMuscleIntensityDao_Impl implements ExerciseMuscleIntensityDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExerciseMuscleIntensityEntity> __insertionAdapterOfExerciseMuscleIntensityEntity;

  public ExerciseMuscleIntensityDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExerciseMuscleIntensityEntity = new EntityInsertionAdapter<ExerciseMuscleIntensityEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `exercise_muscle_intensity` (`exercise_id`,`muscle_name`,`intensity_level`,`role`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ExerciseMuscleIntensityEntity entity) {
        statement.bindLong(1, entity.exerciseId);
        if (entity.muscleName == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.muscleName);
        }
        statement.bindLong(3, entity.intensityLevel);
        if (entity.role == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.role);
        }
      }
    };
  }

  @Override
  public void insertAll(final List<ExerciseMuscleIntensityEntity> notes) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfExerciseMuscleIntensityEntity.insert(notes);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<ExerciseIntensityNote>> observeNotes(final long exerciseId) {
    final String _sql = "SELECT muscle_name AS muscleName, intensity_level AS intensityLevel, role AS role FROM exercise_muscle_intensity WHERE exercise_id = ? ORDER BY intensity_level DESC, muscle_name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, exerciseId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"exercise_muscle_intensity"}, false, new Callable<List<ExerciseIntensityNote>>() {
      @Override
      @Nullable
      public List<ExerciseIntensityNote> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMuscleName = 0;
          final int _cursorIndexOfIntensityLevel = 1;
          final int _cursorIndexOfRole = 2;
          final List<ExerciseIntensityNote> _result = new ArrayList<ExerciseIntensityNote>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseIntensityNote _item;
            _item = new ExerciseIntensityNote();
            if (_cursor.isNull(_cursorIndexOfMuscleName)) {
              _item.muscleName = null;
            } else {
              _item.muscleName = _cursor.getString(_cursorIndexOfMuscleName);
            }
            _item.intensityLevel = _cursor.getInt(_cursorIndexOfIntensityLevel);
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _item.role = null;
            } else {
              _item.role = _cursor.getString(_cursorIndexOfRole);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
