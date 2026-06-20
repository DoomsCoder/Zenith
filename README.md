# Zenith
**Zenith** is a context-aware telemetry focus tracker designed for high-performance deep work. Unlike aggressive app blockers, Zenith monitors physical habits and device interactions to measure the true quality of focus and introduces real-time gamified friction to prevent infinite scrolling loops.

## 🚀 Key Features
*   **Deep Work Engine:** A high-precision timer backed by an Android Foreground Service to survive system memory management and maintain session integrity.
*   **Context-Aware Tracking:** Integrates `UsageStatsManager` to monitor app switches and device interactions, calculating a precise percentage-based focus score without blindly blocking access.
*   **Strict Bio-Breaks:** Enforces real discipline with a fixed-limit bio-break countdown timer that automatically resumes focus states.
*   **Gestural Interactions:** Features a custom Canvas "Halo" dial for intuitive interactions, including a Hold-to-Abandon mechanic.
*   **Advanced Analytics:** A premium dashboard featuring a rolling 7-day focus window built with dynamic, native Cartesian charts and an All-Time Telemetry grid.

## 🛠 Tech Stack
*   **UI Framework:** Jetpack Compose, Vico Cartesian Charting Library, Custom Canvas components.
*   **Architecture:** Unidirectional Data Flow (UDF) / MVVM with StateFlow & SharedFlow.
*   **Local Database:** Room (Persistent storage for `FocusSession` and `DistractionEvent` telemetry).
*   **System Integration:** Android Foreground Services and `UsageStatsManager` API.
*   **Testing:** JUnit 4 for validating focus point algorithms and streak calculation math.

## 🏗 Project Structure
- `ui.screens.focus`: Core Focus experience featuring custom Canvas dials, timers, and gestural overlays.
- `ui.screens.statistics`: Analytics dashboard housing the `ThisWeeksFocusChart` and telemetry grids.
- `service`: `FocusService` implementation handling session lifecycle, strict breaks, and hardware monitoring.
- `data`: Room persistence layer with automated tracking DAOs for session intents and completion statuses.

## 🔧 Installation & Setup
1. Clone the repository.
2. Open in Android Studio (Ladybug or newer).
3. Sync Gradle and run on a physical device with API 26 (Android 8.0) or higher. *(Note: Physical device recommended for accurate UsageStats telemetry).*

---
*Developed for professionals and students who demand total discipline.*