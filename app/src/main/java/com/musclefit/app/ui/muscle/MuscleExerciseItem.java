package com.musclefit.app.ui.muscle;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MuscleExerciseItem {
    public final String id;
    public final String name;
    public final String image;
    public final String description;
    public final List<String> tips;

    public MuscleExerciseItem(String id, String name, String image, String description, List<String> tips) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
        this.image = image == null ? "" : image;
        this.description = description == null ? "" : description;
        this.tips = tips == null ? Collections.emptyList() : tips;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MuscleExerciseItem)) {
            return false;
        }
        MuscleExerciseItem that = (MuscleExerciseItem) other;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(image, that.image)
                && Objects.equals(description, that.description)
                && Objects.equals(tips, that.tips);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, image, description, tips);
    }
}
