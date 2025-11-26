# Changelog

All notable changes to the AeroJudge App will be documented in this file.

---

## [1.2] - 2025-11-26

### Added
- Freestyle caller audio files (call1-5.mp3)
- Favicon for browser tab
- CHANGELOG.md for tracking changes

### Changed
- Updated freestyle figure audio files (fig1-5.mp3)
- Updated instructions audio

### Fixed
- **Code Cleanup**: Fixed all deprecated API warnings
  - Replaced `new URL(String)` with `URI.create(String).toURL()` (Java 20+ compliant)
  - Replaced `Runtime.exec(String)` with `ProcessBuilder` in InfoCollectorService
- Commented out unused imports with `REVIEWED-UNUSED 2025-11 DPG` tags for traceability
- Commented out unused fields (SettingUtils.logger, RootController.sequenceService)

### Technical Notes
- Project targets Java 17 (compatible with Java 17-21)
- All deprecated API warnings resolved
- Build produces clean compile with no warnings

---

## [1.1] - Previous Release

### Added
- Per-type round numbering (KNOWN, UNKNOWN, FREESTYLE each have independent counters)
- Admin pages for competition and device settings
- Freestyle figure display improvements

### Fixed
- Battery warning timing and validation flow
- Incomplete round modal and UI improvements
- Pilot selection and judging page UI refinements
