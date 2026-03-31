package com.musclefit.app.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {ExerciseEntity.class, ExerciseInteractionEntity.class, ExerciseMuscleIntensityEntity.class, CoachBookingEntity.class},
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExerciseDao exerciseDao();

    public abstract ExerciseInteractionDao interactionDao();

    public abstract ExerciseMuscleIntensityDao intensityDao();

    public abstract CoachBookingDao coachBookingDao();

    private static volatile AppDatabase INSTANCE;
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `coach_booking` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`user_name` TEXT, " +
                    "`coach_id` INTEGER NOT NULL, " +
                    "`coach_name` TEXT, " +
                    "`coach_specialty` TEXT, " +
                    "`course_type` TEXT, " +
                    "`status` TEXT, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_coach_booking_status` ON `coach_booking` (`status`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_coach_booking_user_name` ON `coach_booking` (`user_name`)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_coach_booking_status` ON `coach_booking` (`status`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_coach_booking_user_name` ON `coach_booking` (`user_name`)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `exercise_interaction_new` (" +
                    "`exercise_id` INTEGER NOT NULL, " +
                    "`account_id` TEXT NOT NULL DEFAULT '', " +
                    "`liked` INTEGER NOT NULL, " +
                    "`favorited` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`exercise_id`, `account_id`), " +
                    "FOREIGN KEY(`exercise_id`) REFERENCES `exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("INSERT INTO `exercise_interaction_new` (`exercise_id`, `account_id`, `liked`, `favorited`, `updated_at`) " +
                    "SELECT `exercise_id`, 'guest', `liked`, `favorited`, `updated_at` FROM `exercise_interaction`");
            db.execSQL("DROP TABLE `exercise_interaction`");
            db.execSQL("ALTER TABLE `exercise_interaction_new` RENAME TO `exercise_interaction`");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_interaction_exercise_id_account_id` " +
                    "ON `exercise_interaction` (`exercise_id`, `account_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_interaction_account_id` " +
                    "ON `exercise_interaction` (`account_id`)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "musclefit.db")
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    seedAsync();
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    seedAsync();
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase instance = INSTANCE;
            if (instance != null) {
                SeedData.populate(instance);
            }
            executor.shutdown();
        });
    }
}
