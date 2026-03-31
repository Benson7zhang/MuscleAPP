package com.musclefit.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ExerciseDao _exerciseDao;

  private volatile ExerciseInteractionDao _exerciseInteractionDao;

  private volatile ExerciseMuscleIntensityDao _exerciseMuscleIntensityDao;

  private volatile CoachBookingDao _coachBookingDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `exercise` (`id` INTEGER NOT NULL, `name` TEXT, `training_category` TEXT, `movement_type` TEXT, `description` TEXT, `grip_type` TEXT, `category_hint` TEXT, `caution_notes` TEXT, `primary_muscle` TEXT, `like_count` INTEGER NOT NULL, `favorite_count` INTEGER NOT NULL, `max_intensity_level` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `exercise_interaction` (`exercise_id` INTEGER NOT NULL, `account_id` TEXT NOT NULL, `liked` INTEGER NOT NULL, `favorited` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`exercise_id`, `account_id`), FOREIGN KEY(`exercise_id`) REFERENCES `exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_interaction_exercise_id_account_id` ON `exercise_interaction` (`exercise_id`, `account_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_interaction_account_id` ON `exercise_interaction` (`account_id`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `exercise_muscle_intensity` (`exercise_id` INTEGER NOT NULL, `muscle_name` TEXT NOT NULL, `intensity_level` INTEGER NOT NULL, `role` TEXT, PRIMARY KEY(`exercise_id`, `muscle_name`), FOREIGN KEY(`exercise_id`) REFERENCES `exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `coach_booking` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `user_name` TEXT, `coach_id` INTEGER NOT NULL, `coach_name` TEXT, `coach_specialty` TEXT, `course_type` TEXT, `status` TEXT, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coach_booking_status` ON `coach_booking` (`status`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coach_booking_user_name` ON `coach_booking` (`user_name`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9750ef58741442e4c06f4c31e5062037')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `exercise`");
        db.execSQL("DROP TABLE IF EXISTS `exercise_interaction`");
        db.execSQL("DROP TABLE IF EXISTS `exercise_muscle_intensity`");
        db.execSQL("DROP TABLE IF EXISTS `coach_booking`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsExercise = new HashMap<String, TableInfo.Column>(12);
        _columnsExercise.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("training_category", new TableInfo.Column("training_category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("movement_type", new TableInfo.Column("movement_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("grip_type", new TableInfo.Column("grip_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("category_hint", new TableInfo.Column("category_hint", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("caution_notes", new TableInfo.Column("caution_notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("primary_muscle", new TableInfo.Column("primary_muscle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("like_count", new TableInfo.Column("like_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("favorite_count", new TableInfo.Column("favorite_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExercise.put("max_intensity_level", new TableInfo.Column("max_intensity_level", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExercise = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExercise = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExercise = new TableInfo("exercise", _columnsExercise, _foreignKeysExercise, _indicesExercise);
        final TableInfo _existingExercise = TableInfo.read(db, "exercise");
        if (!_infoExercise.equals(_existingExercise)) {
          return new RoomOpenHelper.ValidationResult(false, "exercise(com.musclefit.app.data.db.ExerciseEntity).\n"
                  + " Expected:\n" + _infoExercise + "\n"
                  + " Found:\n" + _existingExercise);
        }
        final HashMap<String, TableInfo.Column> _columnsExerciseInteraction = new HashMap<String, TableInfo.Column>(5);
        _columnsExerciseInteraction.put("exercise_id", new TableInfo.Column("exercise_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseInteraction.put("account_id", new TableInfo.Column("account_id", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseInteraction.put("liked", new TableInfo.Column("liked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseInteraction.put("favorited", new TableInfo.Column("favorited", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseInteraction.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExerciseInteraction = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysExerciseInteraction.add(new TableInfo.ForeignKey("exercise", "CASCADE", "NO ACTION", Arrays.asList("exercise_id"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesExerciseInteraction = new HashSet<TableInfo.Index>(2);
        _indicesExerciseInteraction.add(new TableInfo.Index("index_exercise_interaction_exercise_id_account_id", true, Arrays.asList("exercise_id", "account_id"), Arrays.asList("ASC", "ASC")));
        _indicesExerciseInteraction.add(new TableInfo.Index("index_exercise_interaction_account_id", false, Arrays.asList("account_id"), Arrays.asList("ASC")));
        final TableInfo _infoExerciseInteraction = new TableInfo("exercise_interaction", _columnsExerciseInteraction, _foreignKeysExerciseInteraction, _indicesExerciseInteraction);
        final TableInfo _existingExerciseInteraction = TableInfo.read(db, "exercise_interaction");
        if (!_infoExerciseInteraction.equals(_existingExerciseInteraction)) {
          return new RoomOpenHelper.ValidationResult(false, "exercise_interaction(com.musclefit.app.data.db.ExerciseInteractionEntity).\n"
                  + " Expected:\n" + _infoExerciseInteraction + "\n"
                  + " Found:\n" + _existingExerciseInteraction);
        }
        final HashMap<String, TableInfo.Column> _columnsExerciseMuscleIntensity = new HashMap<String, TableInfo.Column>(4);
        _columnsExerciseMuscleIntensity.put("exercise_id", new TableInfo.Column("exercise_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseMuscleIntensity.put("muscle_name", new TableInfo.Column("muscle_name", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseMuscleIntensity.put("intensity_level", new TableInfo.Column("intensity_level", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseMuscleIntensity.put("role", new TableInfo.Column("role", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExerciseMuscleIntensity = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysExerciseMuscleIntensity.add(new TableInfo.ForeignKey("exercise", "CASCADE", "NO ACTION", Arrays.asList("exercise_id"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesExerciseMuscleIntensity = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExerciseMuscleIntensity = new TableInfo("exercise_muscle_intensity", _columnsExerciseMuscleIntensity, _foreignKeysExerciseMuscleIntensity, _indicesExerciseMuscleIntensity);
        final TableInfo _existingExerciseMuscleIntensity = TableInfo.read(db, "exercise_muscle_intensity");
        if (!_infoExerciseMuscleIntensity.equals(_existingExerciseMuscleIntensity)) {
          return new RoomOpenHelper.ValidationResult(false, "exercise_muscle_intensity(com.musclefit.app.data.db.ExerciseMuscleIntensityEntity).\n"
                  + " Expected:\n" + _infoExerciseMuscleIntensity + "\n"
                  + " Found:\n" + _existingExerciseMuscleIntensity);
        }
        final HashMap<String, TableInfo.Column> _columnsCoachBooking = new HashMap<String, TableInfo.Column>(9);
        _columnsCoachBooking.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCoachBooking.put("user_name", new TableInfo.Column("user_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCoachBooking.put("coach_id", new TableInfo.Column("coach_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCoachBooking.put("coach_name", new TableInfo.Column("coach_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCoachBooking.put("coach_specialty", new TableInfo.Column("coach_specialty", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCoachBooking.put("course_type", new TableInfo.Column("course_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCoachBooking.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCoachBooking.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCoachBooking.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCoachBooking = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCoachBooking = new HashSet<TableInfo.Index>(2);
        _indicesCoachBooking.add(new TableInfo.Index("index_coach_booking_status", false, Arrays.asList("status"), Arrays.asList("ASC")));
        _indicesCoachBooking.add(new TableInfo.Index("index_coach_booking_user_name", false, Arrays.asList("user_name"), Arrays.asList("ASC")));
        final TableInfo _infoCoachBooking = new TableInfo("coach_booking", _columnsCoachBooking, _foreignKeysCoachBooking, _indicesCoachBooking);
        final TableInfo _existingCoachBooking = TableInfo.read(db, "coach_booking");
        if (!_infoCoachBooking.equals(_existingCoachBooking)) {
          return new RoomOpenHelper.ValidationResult(false, "coach_booking(com.musclefit.app.data.db.CoachBookingEntity).\n"
                  + " Expected:\n" + _infoCoachBooking + "\n"
                  + " Found:\n" + _existingCoachBooking);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "9750ef58741442e4c06f4c31e5062037", "3fc2d66b13b18a1dda631cc8c72c3801");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "exercise","exercise_interaction","exercise_muscle_intensity","coach_booking");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `exercise`");
      _db.execSQL("DELETE FROM `exercise_interaction`");
      _db.execSQL("DELETE FROM `exercise_muscle_intensity`");
      _db.execSQL("DELETE FROM `coach_booking`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ExerciseDao.class, ExerciseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExerciseInteractionDao.class, ExerciseInteractionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExerciseMuscleIntensityDao.class, ExerciseMuscleIntensityDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CoachBookingDao.class, CoachBookingDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ExerciseDao exerciseDao() {
    if (_exerciseDao != null) {
      return _exerciseDao;
    } else {
      synchronized(this) {
        if(_exerciseDao == null) {
          _exerciseDao = new ExerciseDao_Impl(this);
        }
        return _exerciseDao;
      }
    }
  }

  @Override
  public ExerciseInteractionDao interactionDao() {
    if (_exerciseInteractionDao != null) {
      return _exerciseInteractionDao;
    } else {
      synchronized(this) {
        if(_exerciseInteractionDao == null) {
          _exerciseInteractionDao = new ExerciseInteractionDao_Impl(this);
        }
        return _exerciseInteractionDao;
      }
    }
  }

  @Override
  public ExerciseMuscleIntensityDao intensityDao() {
    if (_exerciseMuscleIntensityDao != null) {
      return _exerciseMuscleIntensityDao;
    } else {
      synchronized(this) {
        if(_exerciseMuscleIntensityDao == null) {
          _exerciseMuscleIntensityDao = new ExerciseMuscleIntensityDao_Impl(this);
        }
        return _exerciseMuscleIntensityDao;
      }
    }
  }

  @Override
  public CoachBookingDao coachBookingDao() {
    if (_coachBookingDao != null) {
      return _coachBookingDao;
    } else {
      synchronized(this) {
        if(_coachBookingDao == null) {
          _coachBookingDao = new CoachBookingDao_Impl(this);
        }
        return _coachBookingDao;
      }
    }
  }
}
