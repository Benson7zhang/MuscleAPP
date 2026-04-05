package com.musclefit.app.data.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "coach_booking",
        indices = {
                @Index(value = {"status"}, name = "index_coach_booking_status"),
                @Index(value = {"user_name"}, name = "index_coach_booking_user_name")
        }
)
public class CoachBookingEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "user_name")
    public String userName;

    @ColumnInfo(name = "coach_id")
    public long coachId;

    @ColumnInfo(name = "coach_name")
    public String coachName;

    @ColumnInfo(name = "coach_specialty")
    public String coachSpecialty;

    @ColumnInfo(name = "course_type")
    public String courseType;

    public String status;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
