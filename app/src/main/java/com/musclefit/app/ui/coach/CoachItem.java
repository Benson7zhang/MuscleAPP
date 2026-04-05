package com.musclefit.app.ui.coach;

public class CoachItem {
    public final long id;
    public final String name;
    public final String specialty;
    public final String courseType;

    public CoachItem(long id, String name, String specialty, String courseType) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.courseType = courseType;
    }
}
