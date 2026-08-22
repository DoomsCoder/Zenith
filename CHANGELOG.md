# Changelog

All notable changes to the Zenith project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-beta] - 2026-08-22

### Added
- **Time Debt Engine:** Implemented a high-friction penalty system. Distractions now apply "Time Debt" (2x penalty) that must be worked off.
- **Red Debt Arc:** Custom Canvas visualization for Time Debt. The ring now fills counter-clockwise in Penalty Red when integrity is compromised.
- **Whitelist Manager:** Advanced module-based filtering. Users can now exempt specific productivity apps from triggering focus violations.
- **Roast Manager Intelligence:** Context-aware roasting engine with 100+ unique alerts across Mild, Brutal, and Savage tiers.
- **Sensory Punishment Suite:** Custom haptic engine with 5 distinct vibration patterns and intensity controls (1-100%).
- **Mission Dashboard:** Real-time data readout showing stored missions, violations, and system security status.
- **Data & Privacy Protocol:** Industrial-grade system reset logic with "Safe Purge" protocols to ensure database integrity during factory resets.
- **Smart Break Bank:** Dynamic, percentage-based break allowance (10% standard / 20% relaxed) scaled to mission duration.
- **Zenith Pulse:** Custom Android 12+ Splash Screen with scale-and-fade system initialization animation.
- **Global UI Standardization:** App-wide transition to "Standard Android" professional aesthetics, including Sentence case typography and Material 3 pill components.

### Changed
- Replaced Monospace typography with standard system fonts for better general readability.
- Refined Top App Bar spacing and alignment across all settings screens for a premium "Deep Work" feel.
- Harmonized app-wide color system: Green for Success/Completed, Red for Penalties/Abandoned.
- Updated all action buttons to consistent pill-shaped radii (100.dp).

### Fixed
- Resolved critical database lock crashes occurring during system purges by implementing Engine termination grace periods.
- Fixed mismatched status bar coloring on older Android versions through unified theme configuration.
- Corrected adaptive icon background scaling in the About section.

### Documentation
- Updated mission statement and license declarations to match Apache 2.0 status.
- Added comprehensive technical support telemetry copying tool in the About section.

---

## [Initial Prototype] - 2026-08-15

### Added
- **Gestural Halo Engine:** Custom Canvas-based timer dial with trigonometric touch mapping.
- **Resilient Lifecycle:** High-priority Foreground Service to maintain timer state during backgrounding.
- **Persistence Layer:** Room database implementation for `FocusSession` and `DistractionEvent` telemetry.
- **Navigation:** State-based navigation architecture using Navigation3.
- **Telemetry Integration:** Initial connection to `UsageStatsManager` for monitoring digital distractions.
- **Material 3 Design:** Custom "True Black" theme optimized for OLED displays.
- **Analytical Engine:** Initial implementation of the `StatisticsUIState` for real-time telemetry mapping.
- **Focus Scoring:** Logic for calculating focus depth based on app switches and device pickups.
- **Tier System:** Achievement-based progression logic for user focus sprints.
- **Multi-select Mode:** Bulk deletion and management UX for the Session History screen.
