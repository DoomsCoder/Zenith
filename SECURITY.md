# Security Policy

Zenith is a telemetry-driven focus engine currently in its **active engineering phase**. While it is functional, it is a development-stage prototype intended for personal discipline tracking and is not yet hardened for production environments.

## Current Security Status

The project is currently focused on core engine stability and telemetry accuracy. The following security posture should be noted:

- **UsageStats Data:** Zenith requires `PACKAGE_USAGE_STATS` permission. This data is processed locally and stored in a private Room database. No data is currently transmitted to external servers.
- **Foreground Service:** The session lifecycle is managed by a persistent service. While secure from system memory management, it does not yet include session encryption.
- **Local Persistence:** Data is stored in plain-text SQLite (via Room). 

## Reporting a Vulnerability

We value the security of our users. If you discover a security-sensitive bug or a vulnerability, **please do not open a public GitHub issue.**

Instead, please send a private report to:
**vedantkakade05@gmail.com**

Until a dedicated email is provided, you may reach out privately via LinkedIn or GitHub profile messages.

### What to Report Privately
- Improper handling of `UsageStatsManager` data.
- Vulnerabilities that could allow other apps to intercept Zenith telemetry.
- Insecure storage of focus history.
- Critical dependency vulnerabilities.

### What Can Be Public
- UI/UX bugs and crashes.
- Documentation typos or improvements.
- Suggestions for focus-scoring algorithms.

## Supported Versions

As Zenith is under active development, only the latest commit on the `main` branch is supported.

| Version | Status |
| ------- | ------ |
| main / development | Best effort support |
| < 1.0.0 | Not supported |

## Planned Security Roadmap

As we move toward a stable 1.0.0 release, the following security enhancements are planned:

- **Encrypted Room Database:** Utilizing SQLCipher to protect local focus history.
- **Biometric Lock:** Optional authentication for viewing the Analytics Dashboard.
- **Permission Auditing:** Fine-grained handling of high-privilege system permissions.
- **Privacy-First Logs:** Ensuring no sensitive telemetry is leaked in logcat in production builds.

---
*Zenith is a tool for self-discipline. We strive to keep your data private and local.*
