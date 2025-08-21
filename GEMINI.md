# Repository Guidelines

## Project Goal & Context
- App name: SpeedCore — a lightweight GPS speedometer.
- Goal: show current speed, max speed, and trip distance with large, readable UI and 1s updates; low battery impact; offline-friendly.
- Current features: large speed display, max-speed tracking, distance tracking, reset controls, MPH/KMH toggle.
- Target users: drivers, cyclists, runners needing speed/distance without maps.
- Non-goals (for now): maps/navigation, background auto-logging, cloud sync.

## Project Structure & Modules
- Build: Gradle wrapper (`gradlew`, `gradlew.bat`); main module `app/`.
- Package: `com.example.speedcore`; rename cautiously if refactoring.
- Code: `app/src/main/java/com/example/speedcore/` (e.g., `MainActivity.java`).
- Resources: `app/src/main/res/` (`layout/`, `values/`, `mipmap-*/`); app label in `values/strings.xml`.
- Manifest: `app/src/main/AndroidManifest.xml`.
- Tests: unit `app/src/test/java/...`, instrumented `app/src/androidTest/java/...`.

## Build, Test, and Development Commands
- Build debug: `./gradlew assembleDebug`; install: `./gradlew installDebug`.
- Unit tests: `./gradlew test`; instrumented: `./gradlew connectedAndroidTest` (device/emulator).
- Lint/checks: `./gradlew lint` and `./gradlew check`; clean: `./gradlew clean`.

## Coding Style & Naming Conventions
- Java 8, AndroidX; 4-space indent; braces on same line.
- Classes PascalCase, methods/fields lowerCamelCase, constants UPPER_SNAKE_CASE.
- Resources: layouts `activity_<screen>.xml`; view IDs `snake_case` (e.g., `speed_text_view`).

## Testing Guidelines
- Frameworks: JUnit 4, AndroidX Test, Espresso.
- Place tests mirroring source packages; name `<ClassName>Test`.
- Cover speed conversion, unit toggling, and location update handling.

## Commit & Pull Request Guidelines
- Commits: imperative present (e.g., “Add speed conversion”); Conventional Commits welcome (`feat:`, `fix:`, `test:`...).
- PRs: purpose, summary, manual test notes, UI screenshots, linked issues; keep scope focused and update docs.

## Security & Configuration Tips
- Runtime location permissions required; verify prompts and failure paths.
- Keep SDK path in `local.properties`; avoid committing secrets or local-only files.
