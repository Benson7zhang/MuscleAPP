package com.musclefit.app.data.model;

import java.util.Objects;

public class ExerciseCard {
    public long id;
    public String name;
    public String trainingCategory;
    public String movementType;
    public String description;
    public String gripType;
    public String categoryHint;
    public String cautionNotes;
    public String primaryMuscle;
    public int likeCount;
    public int favoriteCount;
    public int maxIntensityLevel;
    public boolean liked;
    public boolean favorited;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExerciseCard that = (ExerciseCard) o;
        return id == that.id
                && likeCount == that.likeCount
                && favoriteCount == that.favoriteCount
                && maxIntensityLevel == that.maxIntensityLevel
                && liked == that.liked
                && favorited == that.favorited
                && Objects.equals(name, that.name)
                && Objects.equals(trainingCategory, that.trainingCategory)
                && Objects.equals(movementType, that.movementType)
                && Objects.equals(description, that.description)
                && Objects.equals(gripType, that.gripType)
                && Objects.equals(categoryHint, that.categoryHint)
                && Objects.equals(cautionNotes, that.cautionNotes)
                && Objects.equals(primaryMuscle, that.primaryMuscle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                trainingCategory,
                movementType,
                description,
                gripType,
                categoryHint,
                cautionNotes,
                primaryMuscle,
                likeCount,
                favoriteCount,
                maxIntensityLevel,
                liked,
                favorited
        );
    }
}
