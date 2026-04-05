-- MuscleFit content-management schema (for MySQL + Navicat)
-- Runtime app uses local Room DB seeded from exported data.

CREATE TABLE IF NOT EXISTS exercise (
  id BIGINT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  training_category ENUM('BODYWEIGHT', 'DUMBBELL', 'BARBELL', 'MACHINE', 'CABLE') NOT NULL,
  movement_type ENUM('ISOLATION', 'COMPOUND') NOT NULL,
  description TEXT NOT NULL,
  grip_type VARCHAR(50) NULL,
  category_hint VARCHAR(50) NULL,
  caution_notes TEXT NOT NULL,
  primary_muscle VARCHAR(50) NOT NULL,
  like_count INT NOT NULL DEFAULT 0,
  favorite_count INT NOT NULL DEFAULT 0,
  max_intensity_level TINYINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exercise_muscle_intensity (
  exercise_id BIGINT NOT NULL,
  muscle_name VARCHAR(50) NOT NULL,
  intensity_level TINYINT NOT NULL,
  role ENUM('MAIN', 'ASSIST') NOT NULL,
  PRIMARY KEY (exercise_id, muscle_name),
  CONSTRAINT fk_intensity_exercise FOREIGN KEY (exercise_id)
    REFERENCES exercise(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS exercise_interaction (
  exercise_id BIGINT PRIMARY KEY,
  liked TINYINT(1) NOT NULL DEFAULT 0,
  favorited TINYINT(1) NOT NULL DEFAULT 0,
  updated_at BIGINT NOT NULL,
  CONSTRAINT fk_interaction_exercise FOREIGN KEY (exercise_id)
    REFERENCES exercise(id) ON DELETE CASCADE
);
