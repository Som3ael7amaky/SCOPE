# SCOPE Project Architecture Ledger

## Executive Summary
SCOPE (Student OS) is a modern Android application built using Jetpack Compose, Material Design 3, Obsidian Dark Frosted Glass UI aesthetics, Room local database, Firebase Authentication, and Firestore persistent cloud database.

---

## Phase 1: Authentication & Identity Engine Architecture
- **Refactored Auth Pipeline:** Standardized `AuthRepository` with dual-tier Google Sign-In via `Jetpack Credential Manager` (`GetSignInWithGoogleOption` / `GetGoogleIdOption`) with automatic activity-launcher fallback for devices without Play Services credential provider support.
- **Arabic Exception Parsing:** Exception codes mapped to clear Arabic error messages.
- **Super Developer Auto-Elevation:** Users with email `ismailhamaky@gmail.com` automatically receive `role = "developer"`, `isDeveloper = true`, and unlock developer crown avatar badges.
- **State Flow & Screens:** Refactored `LoginViewModel`, `SignInScreen`, `SignUpScreen`, and `AuthFlowNavigator` with `collectAsStateWithLifecycle()` for non-blocking UI navigation.

---

## Phase 2: Time & Routine Engine Overhaul
- **7-Day Vertical Timeline:** Upgraded schedule view to full 7-day layout (Sunday through Saturday) with time-ordered vertical timeline cards.
- **Categorization & Color Coding:** Event categories (Academic, Work, Self-Development, Personal) with dedicated color badges and custom MP3 alarm audio support.
- **Room Database v2:** Added `ScheduleEventEntity` and `ScheduleEventDao` with `Flow<List<ScheduleEventEntity>>` reactive streams.
- **Exact Alarms:** Integrated `ScopeAlarmScheduler` and `ScopeAlarmReceiver` with Android `AlarmManager.setExactAndAllowWhileIdle`.

---

## Phase 3: Academic Hub, Focus Studio Integration & Global Sola Avatar Engine
- **Relational Room Schema (Database v3):**
  - `CourseEntity`: Table `courses` storing course metadata (id, name, code, creditHours, targetGrade, currentGrade, colorHex).
  - `CourseNoteEntity`: Table `course_notes` linked to `courses` via `ForeignKey(onDelete = ForeignKey.CASCADE)` with index on `courseId`.
  - `CourseWithNotes`: `@Relation` data class supporting 1-to-N reactive queries.
  - `CourseDao`: Full CRUD operations returning reactive `Flow<List<CourseWithNotes>>`.
- **Focus Studio Integration (`FocusStudioScreen.kt`):**
  - Merged Pomodoro state machine (Start, Pause, Resume, Skip Break, Reset) with smooth `AnimatedContent` transitions.
  - Multi-track ambient sound mixer leveraging `ScopeSoundStudio` (Rain, Academic Cafe, Deep Brown Noise, Alpha Binaural Waves, Mechanical Typing).
  - Audio ducking and completion chime sound effects.
- **Sola Motion & Animation Registry (`SolaAnimationRegistry.kt`):**
  - High-precision Spring physics animatables for breathing, scale-bounce, celebration bursts, and tutorial overlay highlights.
- **Academic Hub Refactor (`StudyScreen.kt`):**
  - Integrated GPA and Grade Estimator, Course Notebook, and Centralized Library modal with file upload pipelines.
