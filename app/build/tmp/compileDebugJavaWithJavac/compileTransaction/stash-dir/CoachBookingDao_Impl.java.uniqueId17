package com.musclefit.app.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
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
public final class CoachBookingDao_Impl implements CoachBookingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CoachBookingEntity> __insertionAdapterOfCoachBookingEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  public CoachBookingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCoachBookingEntity = new EntityInsertionAdapter<CoachBookingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `coach_booking` (`id`,`user_name`,`coach_id`,`coach_name`,`coach_specialty`,`course_type`,`status`,`created_at`,`updated_at`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final CoachBookingEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.userName == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.userName);
        }
        statement.bindLong(3, entity.coachId);
        if (entity.coachName == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.coachName);
        }
        if (entity.coachSpecialty == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.coachSpecialty);
        }
        if (entity.courseType == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.courseType);
        }
        if (entity.status == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.status);
        }
        statement.bindLong(8, entity.createdAt);
        statement.bindLong(9, entity.updatedAt);
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE coach_booking SET status = ?, updated_at = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insert(final CoachBookingEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfCoachBookingEntity.insertAndReturnId(entity);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int updateStatus(final long bookingId, final String status, final long updatedAt) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
    int _argIndex = 1;
    if (status == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, status);
    }
    _argIndex = 2;
    _stmt.bindLong(_argIndex, updatedAt);
    _argIndex = 3;
    _stmt.bindLong(_argIndex, bookingId);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateStatus.release(_stmt);
    }
  }

  @Override
  public LiveData<List<CoachBookingEntity>> observeByUser(final String userName) {
    final String _sql = "SELECT * FROM coach_booking WHERE user_name = ? ORDER BY updated_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userName);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"coach_booking"}, false, new Callable<List<CoachBookingEntity>>() {
      @Override
      @Nullable
      public List<CoachBookingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "user_name");
          final int _cursorIndexOfCoachId = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_id");
          final int _cursorIndexOfCoachName = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_name");
          final int _cursorIndexOfCoachSpecialty = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_specialty");
          final int _cursorIndexOfCourseType = CursorUtil.getColumnIndexOrThrow(_cursor, "course_type");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<CoachBookingEntity> _result = new ArrayList<CoachBookingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CoachBookingEntity _item;
            _item = new CoachBookingEntity();
            _item.id = _cursor.getLong(_cursorIndexOfId);
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _item.userName = null;
            } else {
              _item.userName = _cursor.getString(_cursorIndexOfUserName);
            }
            _item.coachId = _cursor.getLong(_cursorIndexOfCoachId);
            if (_cursor.isNull(_cursorIndexOfCoachName)) {
              _item.coachName = null;
            } else {
              _item.coachName = _cursor.getString(_cursorIndexOfCoachName);
            }
            if (_cursor.isNull(_cursorIndexOfCoachSpecialty)) {
              _item.coachSpecialty = null;
            } else {
              _item.coachSpecialty = _cursor.getString(_cursorIndexOfCoachSpecialty);
            }
            if (_cursor.isNull(_cursorIndexOfCourseType)) {
              _item.courseType = null;
            } else {
              _item.courseType = _cursor.getString(_cursorIndexOfCourseType);
            }
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _item.status = null;
            } else {
              _item.status = _cursor.getString(_cursorIndexOfStatus);
            }
            _item.createdAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item.updatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
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

  @Override
  public LiveData<List<CoachBookingEntity>> observeByStatus(final String status) {
    final String _sql = "SELECT * FROM coach_booking WHERE status = ? ORDER BY created_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (status == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, status);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"coach_booking"}, false, new Callable<List<CoachBookingEntity>>() {
      @Override
      @Nullable
      public List<CoachBookingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "user_name");
          final int _cursorIndexOfCoachId = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_id");
          final int _cursorIndexOfCoachName = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_name");
          final int _cursorIndexOfCoachSpecialty = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_specialty");
          final int _cursorIndexOfCourseType = CursorUtil.getColumnIndexOrThrow(_cursor, "course_type");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<CoachBookingEntity> _result = new ArrayList<CoachBookingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CoachBookingEntity _item;
            _item = new CoachBookingEntity();
            _item.id = _cursor.getLong(_cursorIndexOfId);
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _item.userName = null;
            } else {
              _item.userName = _cursor.getString(_cursorIndexOfUserName);
            }
            _item.coachId = _cursor.getLong(_cursorIndexOfCoachId);
            if (_cursor.isNull(_cursorIndexOfCoachName)) {
              _item.coachName = null;
            } else {
              _item.coachName = _cursor.getString(_cursorIndexOfCoachName);
            }
            if (_cursor.isNull(_cursorIndexOfCoachSpecialty)) {
              _item.coachSpecialty = null;
            } else {
              _item.coachSpecialty = _cursor.getString(_cursorIndexOfCoachSpecialty);
            }
            if (_cursor.isNull(_cursorIndexOfCourseType)) {
              _item.courseType = null;
            } else {
              _item.courseType = _cursor.getString(_cursorIndexOfCourseType);
            }
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _item.status = null;
            } else {
              _item.status = _cursor.getString(_cursorIndexOfStatus);
            }
            _item.createdAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item.updatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
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

  @Override
  public LiveData<List<CoachBookingEntity>> observeByStatusAndCoach(final String status,
      final String coachName) {
    final String _sql = "SELECT * FROM coach_booking WHERE status = ? AND coach_name = ? ORDER BY created_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (status == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, status);
    }
    _argIndex = 2;
    if (coachName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, coachName);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"coach_booking"}, false, new Callable<List<CoachBookingEntity>>() {
      @Override
      @Nullable
      public List<CoachBookingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "user_name");
          final int _cursorIndexOfCoachId = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_id");
          final int _cursorIndexOfCoachName = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_name");
          final int _cursorIndexOfCoachSpecialty = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_specialty");
          final int _cursorIndexOfCourseType = CursorUtil.getColumnIndexOrThrow(_cursor, "course_type");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<CoachBookingEntity> _result = new ArrayList<CoachBookingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CoachBookingEntity _item;
            _item = new CoachBookingEntity();
            _item.id = _cursor.getLong(_cursorIndexOfId);
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _item.userName = null;
            } else {
              _item.userName = _cursor.getString(_cursorIndexOfUserName);
            }
            _item.coachId = _cursor.getLong(_cursorIndexOfCoachId);
            if (_cursor.isNull(_cursorIndexOfCoachName)) {
              _item.coachName = null;
            } else {
              _item.coachName = _cursor.getString(_cursorIndexOfCoachName);
            }
            if (_cursor.isNull(_cursorIndexOfCoachSpecialty)) {
              _item.coachSpecialty = null;
            } else {
              _item.coachSpecialty = _cursor.getString(_cursorIndexOfCoachSpecialty);
            }
            if (_cursor.isNull(_cursorIndexOfCourseType)) {
              _item.courseType = null;
            } else {
              _item.courseType = _cursor.getString(_cursorIndexOfCourseType);
            }
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _item.status = null;
            } else {
              _item.status = _cursor.getString(_cursorIndexOfStatus);
            }
            _item.createdAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item.updatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
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

  @Override
  public LiveData<List<CoachBookingEntity>> observeByStatuses(final String firstStatus,
      final String secondStatus) {
    final String _sql = "SELECT * FROM coach_booking WHERE status IN (?, ?) ORDER BY updated_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (firstStatus == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, firstStatus);
    }
    _argIndex = 2;
    if (secondStatus == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, secondStatus);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"coach_booking"}, false, new Callable<List<CoachBookingEntity>>() {
      @Override
      @Nullable
      public List<CoachBookingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "user_name");
          final int _cursorIndexOfCoachId = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_id");
          final int _cursorIndexOfCoachName = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_name");
          final int _cursorIndexOfCoachSpecialty = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_specialty");
          final int _cursorIndexOfCourseType = CursorUtil.getColumnIndexOrThrow(_cursor, "course_type");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<CoachBookingEntity> _result = new ArrayList<CoachBookingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CoachBookingEntity _item;
            _item = new CoachBookingEntity();
            _item.id = _cursor.getLong(_cursorIndexOfId);
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _item.userName = null;
            } else {
              _item.userName = _cursor.getString(_cursorIndexOfUserName);
            }
            _item.coachId = _cursor.getLong(_cursorIndexOfCoachId);
            if (_cursor.isNull(_cursorIndexOfCoachName)) {
              _item.coachName = null;
            } else {
              _item.coachName = _cursor.getString(_cursorIndexOfCoachName);
            }
            if (_cursor.isNull(_cursorIndexOfCoachSpecialty)) {
              _item.coachSpecialty = null;
            } else {
              _item.coachSpecialty = _cursor.getString(_cursorIndexOfCoachSpecialty);
            }
            if (_cursor.isNull(_cursorIndexOfCourseType)) {
              _item.courseType = null;
            } else {
              _item.courseType = _cursor.getString(_cursorIndexOfCourseType);
            }
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _item.status = null;
            } else {
              _item.status = _cursor.getString(_cursorIndexOfStatus);
            }
            _item.createdAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item.updatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
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

  @Override
  public LiveData<List<CoachBookingEntity>> observeByStatusesAndCoach(final String firstStatus,
      final String secondStatus, final String coachName) {
    final String _sql = "SELECT * FROM coach_booking WHERE status IN (?, ?) AND coach_name = ? ORDER BY updated_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (firstStatus == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, firstStatus);
    }
    _argIndex = 2;
    if (secondStatus == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, secondStatus);
    }
    _argIndex = 3;
    if (coachName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, coachName);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"coach_booking"}, false, new Callable<List<CoachBookingEntity>>() {
      @Override
      @Nullable
      public List<CoachBookingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "user_name");
          final int _cursorIndexOfCoachId = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_id");
          final int _cursorIndexOfCoachName = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_name");
          final int _cursorIndexOfCoachSpecialty = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_specialty");
          final int _cursorIndexOfCourseType = CursorUtil.getColumnIndexOrThrow(_cursor, "course_type");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<CoachBookingEntity> _result = new ArrayList<CoachBookingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CoachBookingEntity _item;
            _item = new CoachBookingEntity();
            _item.id = _cursor.getLong(_cursorIndexOfId);
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _item.userName = null;
            } else {
              _item.userName = _cursor.getString(_cursorIndexOfUserName);
            }
            _item.coachId = _cursor.getLong(_cursorIndexOfCoachId);
            if (_cursor.isNull(_cursorIndexOfCoachName)) {
              _item.coachName = null;
            } else {
              _item.coachName = _cursor.getString(_cursorIndexOfCoachName);
            }
            if (_cursor.isNull(_cursorIndexOfCoachSpecialty)) {
              _item.coachSpecialty = null;
            } else {
              _item.coachSpecialty = _cursor.getString(_cursorIndexOfCoachSpecialty);
            }
            if (_cursor.isNull(_cursorIndexOfCourseType)) {
              _item.courseType = null;
            } else {
              _item.courseType = _cursor.getString(_cursorIndexOfCourseType);
            }
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _item.status = null;
            } else {
              _item.status = _cursor.getString(_cursorIndexOfStatus);
            }
            _item.createdAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item.updatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
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

  @Override
  public CoachBookingEntity getByIdSync(final long bookingId) {
    final String _sql = "SELECT * FROM coach_booking WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, bookingId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "user_name");
      final int _cursorIndexOfCoachId = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_id");
      final int _cursorIndexOfCoachName = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_name");
      final int _cursorIndexOfCoachSpecialty = CursorUtil.getColumnIndexOrThrow(_cursor, "coach_specialty");
      final int _cursorIndexOfCourseType = CursorUtil.getColumnIndexOrThrow(_cursor, "course_type");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
      final CoachBookingEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new CoachBookingEntity();
        _result.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfUserName)) {
          _result.userName = null;
        } else {
          _result.userName = _cursor.getString(_cursorIndexOfUserName);
        }
        _result.coachId = _cursor.getLong(_cursorIndexOfCoachId);
        if (_cursor.isNull(_cursorIndexOfCoachName)) {
          _result.coachName = null;
        } else {
          _result.coachName = _cursor.getString(_cursorIndexOfCoachName);
        }
        if (_cursor.isNull(_cursorIndexOfCoachSpecialty)) {
          _result.coachSpecialty = null;
        } else {
          _result.coachSpecialty = _cursor.getString(_cursorIndexOfCoachSpecialty);
        }
        if (_cursor.isNull(_cursorIndexOfCourseType)) {
          _result.courseType = null;
        } else {
          _result.courseType = _cursor.getString(_cursorIndexOfCourseType);
        }
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _result.status = null;
        } else {
          _result.status = _cursor.getString(_cursorIndexOfStatus);
        }
        _result.createdAt = _cursor.getLong(_cursorIndexOfCreatedAt);
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
