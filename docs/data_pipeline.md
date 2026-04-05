# MySQL to APK Data Pipeline

1. Maintain source data in Navicat tables: exercise and exercise_muscle_intensity.
2. Export table rows as JSON (UTF-8).
3. Convert exported rows into Room seed input.
4. Package seed data into the APK and import it on first app launch.
5. Runtime reads only local Room data, so offline installs show the same result.
