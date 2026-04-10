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
        entities = {
                ExerciseEntity.class,
                ExerciseInteractionEntity.class,
                ExerciseMuscleIntensityEntity.class,
                AssistantMessageEntity.class,
                ForumPostEntity.class,
                ForumCommentEntity.class,
                ForumPostLikeEntity.class,
                ForumPostFavoriteEntity.class,
                ForumReportEntity.class
        },
        version = 10,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExerciseDao exerciseDao();

    public abstract ExerciseInteractionDao interactionDao();

    public abstract ExerciseMuscleIntensityDao intensityDao();

    public abstract AssistantMessageDao assistantMessageDao();

    public abstract ForumDao forumDao();

    private static volatile AppDatabase INSTANCE;
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // no-op
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // no-op
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

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `assistant_message` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`account_id` TEXT NOT NULL, " +
                    "`assistant_type` TEXT NOT NULL, " +
                    "`sender_type` INTEGER NOT NULL, " +
                    "`content` TEXT NOT NULL, " +
                    "`status` INTEGER NOT NULL, " +
                    "`created_at` INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_assistant_message_account_id_assistant_type_created_at` " +
                    "ON `assistant_message` (`account_id`, `assistant_type`, `created_at`)");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `forum_post` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`author_account_id` TEXT NOT NULL, " +
                    "`author_nickname` TEXT NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`content` TEXT NOT NULL, " +
                    "`like_count` INTEGER NOT NULL, " +
                    "`comment_count` INTEGER NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_post_author_account_id` ON `forum_post` (`author_account_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_post_updated_at` ON `forum_post` (`updated_at`)");

            db.execSQL("CREATE TABLE IF NOT EXISTS `forum_comment` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`post_id` INTEGER NOT NULL, " +
                    "`author_account_id` TEXT NOT NULL, " +
                    "`author_nickname` TEXT NOT NULL, " +
                    "`content` TEXT NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`post_id`) REFERENCES `forum_post`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_comment_post_id` ON `forum_comment` (`post_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_comment_author_account_id` ON `forum_comment` (`author_account_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_comment_created_at` ON `forum_comment` (`created_at`)");

            db.execSQL("CREATE TABLE IF NOT EXISTS `forum_post_like` (" +
                    "`post_id` INTEGER NOT NULL, " +
                    "`account_id` TEXT NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`post_id`, `account_id`), " +
                    "FOREIGN KEY(`post_id`) REFERENCES `forum_post`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_post_like_account_id` ON `forum_post_like` (`account_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_post_like_post_id` ON `forum_post_like` (`post_id`)");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `forum_report` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`post_id` INTEGER NOT NULL, " +
                    "`reporter_account_id` TEXT NOT NULL, " +
                    "`reason` TEXT NOT NULL, " +
                    "`status` TEXT NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`post_id`) REFERENCES `forum_post`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_report_post_id` ON `forum_report` (`post_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_report_reporter_account_id` ON `forum_report` (`reporter_account_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_report_created_at` ON `forum_report` (`created_at`)");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `forum_post` ADD COLUMN `favorite_count` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("CREATE TABLE IF NOT EXISTS `forum_post_favorite` (" +
                    "`post_id` INTEGER NOT NULL, " +
                    "`account_id` TEXT NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`post_id`, `account_id`), " +
                    "FOREIGN KEY(`post_id`) REFERENCES `forum_post`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_post_favorite_account_id` ON `forum_post_favorite` (`account_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_forum_post_favorite_post_id` ON `forum_post_favorite` (`post_id`)");
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS `coach_booking`");
        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `forum_post` ADD COLUMN `image_uris` TEXT NOT NULL DEFAULT ''");
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
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
                            .addMigrations(MIGRATION_7_8)
                            .addMigrations(MIGRATION_8_9)
                            .addMigrations(MIGRATION_9_10)
                            .fallbackToDestructiveMigration()
                            .fallbackToDestructiveMigrationOnDowngrade()
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
