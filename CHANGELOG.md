# Changelog

All notable changes to the Zenith project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Gestural Halo Engine:** Custom Canvas-based timer dial with trigonometric touch mapping.
- **Resilient Lifecycle:** High-priority Foreground Service to maintain timer state during backgrounding.
- **Persistence Layer:** Room database implementation for `FocusSession` and `DistractionEvent` telemetry.
- **Navigation:** State-based navigation architecture using Navigation3.
- **Telemetry Integration:** Initial connection to `UsageStatsManager` for monitoring digital distractions.
- **Material 3 Design:** Custom "True Black" (`0xFF0A0A0A`) theme optimized for OLED displays.
- **Analytical Engine:** Initial implementation of the `StatisticsUIState` for real-time telemetry mapping.
- **Focus Scoring:** Logic for calculating focus depth based on app switches and device pickups.
- **Tier System:** Achievement-based progression logic for user focus sprints.
- **Multi-select Mode:** Bulk deletion and management UX for the Session History screen.
- **Documentation:** Comprehensive README, Contributing guide, Code of Conduct, Security policy, and Changelog.

### Changed
- Refined UI spacing on the Statistics screen for better information density.
- Optimized Canvas DrawScopes to ensure flawless 60fps rendering.

### Fixed
- Resolved state synchronization issue between the Foreground Service and the FocusViewModel.
- Fixed chart clipping issues in the Vico Cartesian implementation.

### Planned
- SQLCipher integration for encrypted Room persistence.
- Biometric authentication for the Statistics dashboard.
- Custom haptic feedback patterns for the "Halo" gestural dial.
- Export functionality for focus data (CSV/JSON).
